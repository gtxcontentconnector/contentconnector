package com.gentics.cr.lucene.facets.search;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.LabelAndValue;
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;

import com.gentics.cr.CRConfig;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.facets.taxonomy.TaxonomyMapping;
import com.gentics.cr.lucene.facets.taxonomy.taxonomyaccessor.TaxonomyAccessor;
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
	private boolean listnullvalues = false;
	private int facetnumbercategories = DEFAULT_FACET_NUMBER_OF_CATEGORIES;

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
				
				facetnumbercategories = subconf.getInteger(
						FACET_NUMBER_OF_CATEGORIES_KEY,
						DEFAULT_FACET_NUMBER_OF_CATEGORIES);
				
				listnullvalues = subconf.getBoolean(LIST_NULL_VALUES_KEY, listnullvalues);
			}
			log.debug("Facets enabled");
		} else {
			log.debug("Facets not enabled");
		}
	}

	/**
	 * gets the results from the {@link FacetsCollector} and returns a object
	 * which can be stored in the {@link CRMetaResolvableBean}
	 * 
	 * @param facetsCollector the facet collector that was used for searching
	 * @param taAccessor the TaxonomyAccessor of the used IndexLocation
	 * @return an Object that can be stored in the {@link CRMetaResolvableBean}
	 * @throws IOException
	 * @author Sebastian Vogel <s.vogel@gentics.com>
	 */
	public Object getFacetsResults(FacetsCollector facetsCollector, TaxonomyAccessor taAccessor)
			throws IOException {
		// Retrieve results
	    
		Map<String, Object> facetResultNodes = new HashMap<String, Object>();
	    TaxonomyReader taReader = taAccessor.getTaxonomyReader();
	    try {
		    FacetsConfig config = new FacetsConfig();
		    
			int i = 0;
		    Collection<TaxonomyMapping> mappings = taAccessor.getTaxonomyMappings();
			for (TaxonomyMapping mapping:mappings) {
				config.setIndexFieldName(mapping.getCategory(), mapping.getAttribute());
				Facets tFacet = new FastTaxonomyFacetCounts(mapping.getAttribute(),taReader,config,facetsCollector);
				FacetResult result = tFacet.getTopChildren(facetnumbercategories, mapping.getCategory());
				if (result != null || listnullvalues) {
					facetResultNodes.put(String.valueOf(i),
								buildFacetsResultTree(mapping, result));
					i++;
				}
			}
	    }
	    finally {
	    	taAccessor.release(taReader);
	    }
		
		return facetResultNodes;
	}
	
	/**
	 * Recursive method iterates over all {@link FacetResultNode} in the Result
	 * the maximum number of facet result nodes per query is defined via the
	 * properties or via the DEFAULT_FACET_NUMBER_OF_CATEGORIES variable
	 * 
	 * @param mapping The mapping for the current facet
	 * 
	 * @param facetResultNodes
	 *            a list of facet result nodes
	 * @return a tree-like map of categories including their sub categories and
	 *         the number of results to each category (and sub category)
	 * @author Sebastian Vogel <s.vogel@gentics.com>
	 */
	private Map<String, Object> buildFacetsResultTree(TaxonomyMapping mapping, FacetResult facetNode) {
		Map<String, Object> facetsResultNode = new HashMap<String, Object>();
		if (facetNode != null) {
			String[] path = facetNode.path;
			String categoryName = facetNode.dim;
			
			facetsResultNode.put(RESULT_FACETS_CATEGORY_NAME_KEY, categoryName);
			facetsResultNode.put(RESULT_FACETS_TOTAL_COUNT_KEY,
					String.valueOf(facetNode.value));
	
			if (facetdisplaypath) {
				facetsResultNode.put(RESULT_FACETS_PATH_KEY, path);
			}
			LabelAndValue[] lavs = facetNode.labelValues;
			if (facetNode.childCount > 0) {
				Map<String, Number> subnodes = new HashMap<String,Number>();
				for (int i = 0; i<lavs.length; i++) {
					subnodes.put(lavs[i].label, lavs[i].value);
				}
				if (subnodes.size() > 0) {
					facetsResultNode.put(RESULT_FACETS_SUBNODES_KEY, subnodes);
				}
			}
		} else if (listnullvalues){
			facetsResultNode.put(RESULT_FACETS_TOTAL_COUNT_KEY,
					String.valueOf(0));
			facetsResultNode.put(RESULT_FACETS_CATEGORY_NAME_KEY,
					mapping.getCategory());
		}
		String label = mapping.getLabel();
		if (label != null && !"".equals(label)) {
			facetsResultNode.put(LABEL_KEY,
					label);
		}
		return facetsResultNode;
	}

	/**
	 * <p>
	 * Create a new {@link FacetsCollector}
	 * </p>
	 * @author Sebastian Vogel <s.vogel@gentics.com>
	 * @return FacetsCollector returns a new FacetsCollector instance.
	 */
	public FacetsCollector createFacetsCollector() {
		FacetsCollector facetsCollector = new FacetsCollector();
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
