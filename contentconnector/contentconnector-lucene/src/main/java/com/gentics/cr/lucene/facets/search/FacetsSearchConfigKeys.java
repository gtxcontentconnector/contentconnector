package com.gentics.cr.lucene.facets.search;


/**
 * contains all config keys needed for facets search
 * 
 * @author Sebastian Vogel <s.vogel@gentics.com>
 *
 */
public interface FacetsSearchConfigKeys {
	/**
	 * Indicates if Facets are enabled
	 */
	public static final String FACETS_ENABLED_KEY = "facet";
	/**
	 * The key under which the facet configuration is stored in the properties
	 */
	public static final String FACETS_CONFIG_KEY = "facetconfig";
	/**
	 * The key under which the taxonomy path is stored in the properties
	 */
	public static final String FACETS_TAXONOMY_PATH_KEY = "taxonomyPath";

	/**
	 * property key: indicates if the ordinal of a facet category should be
	 * included in the results
	 */
	public static final String FACETS_DISPLAY_ORDINAL_KEY = "displayordinal";
	/**
	 * property key: indicates if the path of a facet category should be
	 * included in the results
	 */
	public static final String FACETS_DISPLAY_PATH_KEY = "displaypath";
	/**
	 * property key: defines the path delimiter of a facet category
	 */
	public static final String FACETS_PATH_DELIMITER_KEY = "pathdelimiter";
	/**
	 * property key: defines the the maximum number of (top)categories which
	 * will be retrieved per query
	 */
	public static final String FACET_NUMBER_OF_CATEGORIES_KEY = "numbercategories";

	/**
	 * Key to put the result of the facet query into the result
	 */
	public static final String RESULT_FACETS_LIST_KEY = "facetsList";
	/**
	 * Key for the result count of a category in the facetsList
	 */
	public static final String RESULT_FACETS_TOTAL_COUNT_KEY = "count";
	/**
	 * Key for the path of a category in the facetsList
	 */
	public static final String RESULT_FACETS_PATH_KEY = "path";
	/**
	 * Key for the ordinal of a category in the facetsList
	 */
	public static final String RESULT_FACETS_ORDINAL_KEY = "ordinal";
	/**
	 * Key for the name/label of a category in the facetsList
	 */
	public static final String RESULT_FACETS_CATEGORY_NAME_KEY = "name";
	/**
	 * Key for the subnodes of a category in the facetsList
	 */
	public static final String RESULT_FACETS_SUBNODES_KEY = "subnodes";

	/**
	 * Default number of categories which will retrieved per query
	 */
	public static final int DEFAULT_FACET_NUMBER_OF_CATEGORIES = 10;
	/**
	 * Default maximum category path depth of facet searches
	 */
	public static final char DEFAULT_FACET_PATH_DELIMITER = '/';


}
