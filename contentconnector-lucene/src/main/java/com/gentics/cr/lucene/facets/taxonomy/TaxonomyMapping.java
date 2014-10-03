package com.gentics.cr.lucene.facets.taxonomy;

import java.util.Collection;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.gentics.cr.CRConfig;
import com.gentics.cr.configuration.GenericConfiguration;

/**
 * A Taxonomy Mapping maps an attribute of a ContentRepository-Object to a
 * taxonomy category
 * 
 * $Date$
 * 
 * @version $Revision$
 * @author Sebastian Vogel <s.vogel@gentics.com>
 */
public class TaxonomyMapping implements TaxonomyConfigKeys {

	private static final String LABLE_KEY = "label";
	
	private String category;
	private String attribute;
	private String label;

	/**
	 * Log4j logger for error and debug messages.
	 */
	private static final Logger LOGGER = Logger
			.getLogger(TaxonomyMapping.class);

	/**
	 * Constructor
	 * 
	 * @param category
	 * @param attribute
	 * @param label
	 */
	public TaxonomyMapping(String category, String attribute, String label) {
		this.category = category;
		this.attribute = attribute;
		this.label = label;
	}
	
	/**
	 * returns the label for the mapping
	 * @return label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * returns the category for the mapping
	 * 
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * returns the attribute for the mapping
	 * 
	 * @return the attribute
	 */
	public String getAttribute() {
		return attribute;
	}

	/**
	 * retrieves the mapping from the {@link CRConfig} and returns a
	 * {@link Collection} of the retrieved {@link TaxonomyMapping}
	 * 
	 * @param config
	 *            the config
	 * @return {@link Collection} of {@link TaxonomyMapping}
	 */
	public static Collection<TaxonomyMapping> mapTaxonomies(
			final CRConfig config) {
		Vector<TaxonomyMapping> mappings = new Vector<TaxonomyMapping>();

		GenericConfiguration mappingsConf = (GenericConfiguration) config
				.get(FACET_CONFIG_KEY.concat(".").concat(
						FACET_CONFIG_MAPPINGS_KEY));

		if (mappingsConf != null) {
			Map<String, GenericConfiguration> mappingsMap = mappingsConf
					.getSortedSubconfigs();
			if (mappingsMap != null) {
				for (GenericConfiguration mapConf : mappingsMap.values()) {
					String category = mapConf.getString(
							FACET_CONFIG_MAPPINGS_CATEGORY_KEY, "");
					String attribute = mapConf.getString(
							FACET_CONFIG_MAPPINGS_ATTRIBUTE_KEY, "");
					String label = mapConf.getString(LABLE_KEY, "");
					if ((category != null && !"".equals(category)) && (attribute != null && !"".equals(attribute))) {
						mappings.add(new TaxonomyMapping(category, attribute, label));
						LOGGER.debug("Added new Taxnonomy Mapping for the category: "
								+ category + " and the attribute: " + attribute);
					}
				}
			}
		}
		return mappings;
	}
}
