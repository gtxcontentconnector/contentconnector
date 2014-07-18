package com.gentics.cr.lucene.facets.taxonomy;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.facet.FacetField;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;

import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.cr.lucene.facets.taxonomy.taxonomyaccessor.TaxonomyAccessor;
import com.gentics.cr.lucene.indexer.index.LuceneIndexLocation;
/**
 * This class handles the creation of the facet index
 * @author christopher
 *
 */
public class TaxonomyDocumentBuilder {
	
	private LuceneIndexLocation loc;
	
	private TaxonomyAccessor taxonomyAccessor = null;
	private TaxonomyWriter taxonomyWriter = null;
	
	private Map<String, TaxonomyMapping> taxoMap = null;
	
	private final FacetsConfig config = new FacetsConfig();
	/**
	 * Constructor
	 * @param loc IndexLocation
	 * @throws IOException 
	 */
	public TaxonomyDocumentBuilder(LuceneIndexLocation loc) throws IOException {
		this.loc = loc;
		if (useFacets()) {
			taxonomyAccessor = this.loc.getTaxonomyAccessor();
			this.taxonomyWriter = taxonomyAccessor.getTaxonomyWriter();
			Collection<TaxonomyMapping> maps = this.taxonomyAccessor.getTaxonomyMappings();
			this.taxoMap = new HashMap<String, TaxonomyMapping>();
			for (TaxonomyMapping map : maps) {
				config.setIndexFieldName(map.getCategory(), map.getAttribute());
				this.taxoMap.put(map.getAttribute(), map);
			}
		}
	}
	
	/**
	 * Create a lucene facet field from the taxonomy information
	 * @param bean Resolvable
	 * @param attributeKey attribute name
	 * @return facet field
	 */
	public FacetField buildFacetField(Resolvable bean, String attributeKey) {
		Object attribute = bean.get(attributeKey);
		FacetField field = null;
		TaxonomyMapping mapping = this.taxoMap.get(attributeKey);
		// if bean does not have the attribute don't create a category path
		if (attribute != null) {
			Class<?> type = attribute.getClass();
			if (attribute instanceof Collection<?>) {
				field = new FacetField(mapping.getCategory(), ((Collection<?>) attribute).toArray(new String[]{}));
			} else if (type.isArray()) {
				Class<?> dataType = type.getComponentType();
				if (dataType.equals((new String()).getClass())) {
					field = new FacetField(mapping.getCategory(), (String[]) attribute);
				}
			} else {
				field = new FacetField(mapping.getCategory(), attribute.toString());
			}
		} else {
			return null;
		}
		return field;
	}
	
	/**
	 * Build the facet/taxonomy information and write it to the facet index.
	 * @param doc Document
	 * @return built document
	 * @throws IOException
	 */
	public Document buildDocument(Document doc) throws IOException {
		return config.build(taxonomyWriter, doc);
	}
	
	/**
	 * Check if an attribute is a taxonomyAttribute
	 * @param name
	 * @return
	 */
	public boolean isTaxonomyAttribute(String name) {
		if (this.taxoMap != null) {
			return this.taxoMap.containsKey(name);
		}
		return false;
	}
	
	/**
	 * Check if we have to create a facet index.
	 * @return
	 */
	public boolean useFacets() {
		return this.loc.useFacets();
	}
	
	/**
	 * Finalize and clean up
	 */
	public void close() {
		if (taxonomyAccessor != null && taxonomyWriter != null) {
			this.taxonomyAccessor.release(taxonomyWriter);
		}
	}
}
