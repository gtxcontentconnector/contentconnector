package com.gentics.cr.lucene.facets.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.facet.search.FacetsCollector;
import org.apache.lucene.facet.search.params.CountFacetRequest;
import org.apache.lucene.facet.search.params.FacetSearchParams;
import org.apache.lucene.facet.search.results.FacetResult;
import org.apache.lucene.facet.search.results.FacetResultNode;
import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.index.IndexReader;

import com.gentics.cr.CRConfig;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.facets.taxonomy.TaxonomyMapping;
import com.gentics.cr.lucene.facets.taxonomy.taxonomyaccessor.TaxonomyAccessor;
import com.gentics.cr.lucene.indexer.index.LuceneIndexLocation;
import com.gentics.cr.lucene.search.CRMetaResolvableBean;

/**
 * This class contains all the methods needed to perform a faceted query request
 * 
 * @author Sebastian Vogel <s.vogel@gentics.com>
 * 
 */
public class FacetsSearch implements FacetsSearchConfigKeys {

	private static Logger log = Logger.getLogger(FacetsSearch.class);

	private boolean facetdisplayordinal = false;
	private boolean usefacets = false;
	private boolean facetdisplaypath = false;
	private int facetnumbercategories = DEFAULT_FACET_NUMBER_OF_CATEGORIES;
	private char facetpathdelimiter = DEFAULT_FACET_PATH_DELIMITER;

	/**
	 * Reads the config for facet related entries and initializes the relevant variables
	 * 
	 * @param config
	 */
	public FacetsSearch(CRConfig config) throws IllegalStateException {
		usefacets = config.getBoolean(FACETS_ENABLED_KEY, usefacets);
		if (usefacets) {
			GenericConfiguration subconf = (GenericConfiguration) config
					.get(FACETS_CONFIG_KEY);
			if (subconf != null) {
				facetdisplayordinal = subconf.getBoolean(
						FACETS_DISPLAY_ORDINAL_KEY, facetdisplayordinal);
				facetdisplaypath = subconf.getBoolean(FACETS_DISPLAY_PATH_KEY,
						facetdisplaypath);
				String delimiter = subconf.getString(FACETS_PATH_DELIMITER_KEY,
						"");
				facetpathdelimiter = (delimiter != null && !"".equals(delimiter)) ? delimiter.charAt(0)
						: DEFAULT_FACET_PATH_DELIMITER;
				facetnumbercategories = subconf.getInteger(
						FACET_NUMBER_OF_CATEGORIES_KEY,
						DEFAULT_FACET_NUMBER_OF_CATEGORIES);
			}
			log.debug("Facets enabled");
		} else {
			log.debug("Facets not enabled");
		}
	}

	/**
	 * Maps the categories defined in the mappings to {@link FacetSearchParams}
	 * TODO: implement categories selection via request-parameters
	 * 
	 * @param taAccessor
	 *            the {@link TaxonomyAccessor} as stored in the
	 *            {@link LuceneIndexLocation}
	 * @return the mapped {@link FacetSearchParams}
	 * @author Sebastian Vogel <s.vogel@gentics.com>
	 */
	private FacetSearchParams getFacetSearchParams(TaxonomyAccessor taAccessor) {
		FacetSearchParams params = new FacetSearchParams();

		for (TaxonomyMapping map : taAccessor.getTaxonomyMappings()) {
			CountFacetRequest req = new CountFacetRequest(new CategoryPath(
					map.getCategory()), facetnumbercategories);
			params.addFacetRequest(req);
			if (log.isDebugEnabled()) {
				log.debug("Added Category Path " + map.getCategory().toString()
						+ " to the Facet Search Params");
			}
		}

		return params;
	}

	/**
	 * gets the results from the {@link FacetsCollector} and returns a object
	 * which can be stored in the {@link CRMetaResolvableBean}
	 * 
	 * @param facetsCollector
	 * @return an Object that can be stored in the {@link CRMetaResolvableBean}
	 * @throws IOException
	 * @author Sebastian Vogel <s.vogel@gentics.com>
	 */
	public Object getFacetsResults(FacetsCollector facetsCollector)
			throws IOException {
		List<FacetResult> facetResults = facetsCollector.getFacetResults();

		Map<String, Object> facetsResultsRootNode = new HashMap<String, Object>();
		int i = 0;
		for (FacetResult facetResult : facetResults) {
			facetsResultsRootNode.put(String.valueOf(i),
					buildFacetsResultTree(facetResult.getFacetResultNode()));
			i++;
		}

		return facetsResultsRootNode;
	}

	/**
	 * Recursive method iterates over all {@link FacetResultNode} in the Result
	 * the maximum number of facet result nodes per query is defined via the
	 * properties or via the DEFAULT_FACET_NUMBER_OF_CATEGORIES variable
	 * 
	 * @param facetResultNodes
	 *            a list of facet result nodes
	 * @return a tree-like map of categories including their sub categories and
	 *         the number of results to each category (and sub category)
	 * @author Sebastian Vogel <s.vogel@gentics.com>
	 */
	private Map<String, Object> buildFacetsResultTree(FacetResultNode facetNode) {
		Map<String, Object> facetsResultNode = new HashMap<String, Object>();
		String path = facetNode.getLabel().toString(facetpathdelimiter);
		String categoryName = path.substring(path
				.lastIndexOf(facetpathdelimiter) + 1);
		facetsResultNode.put(RESULT_FACETS_CATEGORY_NAME_KEY, categoryName);
		facetsResultNode.put(RESULT_FACETS_TOTAL_COUNT_KEY,
				String.valueOf((int) facetNode.getValue()));

		if (facetdisplayordinal) {
			facetsResultNode.put(RESULT_FACETS_ORDINAL_KEY,
					String.valueOf(facetNode.getOrdinal()));
		}

		if (facetdisplaypath) {
			facetsResultNode.put(RESULT_FACETS_PATH_KEY, path);
		}

		if (facetNode.getNumSubResults() > 0) {
			List<Map<String, Object>> subnodes = new ArrayList<Map<String, Object>>();
			for (FacetResultNode resultNode : facetNode.getSubResults()) {
				subnodes.add(buildFacetsResultTree(resultNode));
			}
			if (subnodes.size() > 0) {
				facetsResultNode.put(RESULT_FACETS_SUBNODES_KEY, subnodes);
			}
		}

		return facetsResultNode;
	}

	/**
	 * <p>
	 * Create a new {@link FacetsCollector}
	 * </p>
	 * 
	 * @param indexReader
	 * @param taAccessor
	 * @param taReader
	 * @return
	 * @author Sebastian Vogel <s.vogel@gentics.com>
	 */
	public FacetsCollector createFacetsCollector(IndexReader indexReader,
			TaxonomyAccessor taAccessor, TaxonomyReader taReader) {
		FacetSearchParams facetSearchParams = getFacetSearchParams(taAccessor);
		FacetsCollector facetsCollector = new FacetsCollector(
				facetSearchParams, indexReader, taReader);
		return facetsCollector;
	}

	/**
	 * indicates if facets are used
	 * 
	 * @return true if facets are used for this searcher
	 */
	public boolean useFacets() {
		return usefacets;
	}

}
