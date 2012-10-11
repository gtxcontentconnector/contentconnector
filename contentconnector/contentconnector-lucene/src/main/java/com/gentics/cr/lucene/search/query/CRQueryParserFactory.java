package com.gentics.cr.lucene.search.query;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MultiTermQuery;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRRequest;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.LuceneVersion;
import com.gentics.cr.util.generics.Instanciator;

/**
 * Parser factory.
 * @author Christopher
 *
 */
public final class CRQueryParserFactory {

	/**
	 * Log4j logger.
	 */
	private static Logger logger = Logger.getLogger(CRQueryParserFactory.class);

	/**
	 * Default constructor to prevent instantiation of utility class.
	 */
	private CRQueryParserFactory() {
	}

	/**
	 * max clause key.
	 */
	private static final String MAX_CLAUSES_KEY = "maxqueryclauses";

	/**
	 * configuration key for lower case expanded terms.
	 * configures if the wildcardqueries should be automatically converted 
	 * to lowercase by lucene.
	 */
	private static final String LOWER_CASE_EXPANDED_TERMS_KEY = "lowercaseexpandedterms";

	/**
	 * Query parser class key.
	 */
	private static final String QUERY_PARSER_CLASS = "class";
	/**
	 * queryparser key.
	 */
	private static final String QUERY_PARSER_CONFIG = "queryparser";
	
	/***
	 * Generates a prepared and configured QueryParser.
	 * @param searchedAttributes attributes-
	 * @param analyzer analyzer
	 * @param request request
	 * @param config config
	 * @return query parser
	 */
	public static QueryParser getConfiguredParser(final String[] searchedAttributes, final Analyzer analyzer,
			final CRRequest request, final CRConfig config) {
		

		Object subconfig = config.get(QUERY_PARSER_CONFIG);
		
		return getParser(searchedAttributes, analyzer, request, config, subconfig);
	}

	public static QueryParser getConfiguredHighlightParser(final String[] searchedAttributes, final Analyzer analyzer,
			final CRRequest request, final CRConfig config, final Object subconfig) {
		//QueryParser parser = null;

		return getParser(searchedAttributes, analyzer, request, config, subconfig);
	}
	
	private static QueryParser getParser(final String[] searchedAttributes, final Analyzer analyzer,
			final CRRequest request, final CRConfig config, final Object subconfig) {
		QueryParser parser = null;
		
		if (subconfig != null && subconfig instanceof GenericConfiguration) {
			GenericConfiguration pconfig = (GenericConfiguration) subconfig;

			String parserClass = pconfig.getString(QUERY_PARSER_CLASS);
			if (parserClass != null) {
				parser = (QueryParser) Instanciator.getInstance(parserClass, new Object[][] {
						new Object[] {
						pconfig, LuceneVersion.getVersion(), searchedAttributes, analyzer, request },
						new Object[] {
						LuceneVersion.getVersion(), searchedAttributes, analyzer, request },
						new Object[] {
								LuceneVersion.getVersion(), searchedAttributes[0], analyzer }});
				
				if (parser == null) {
					logger.warn(String.format(
						"Configured %s '%s' of CRConfig %s was not initialized",
						QUERY_PARSER_CLASS,
						parserClass,
						config.getName()));
				}

			}

		}

		if (parser == null) {
			//USE DEFAULT QUERY PARSER
			parser = new QueryParser(LuceneVersion.getVersion(), searchedAttributes[0], analyzer);
		}

		//CONFIGURE MAX CLAUSES
		BooleanQuery.setMaxClauseCount(config.getInteger(
			QUERY_PARSER_CONFIG + "." + MAX_CLAUSES_KEY,
			BooleanQuery.getMaxClauseCount()));

		//CONFIGURE LOWER CASE EXPANDED TERMS (useful for WhitespaceAnalyzer)
		parser.setLowercaseExpandedTerms(config.getBoolean(
			QUERY_PARSER_CONFIG + "." + LOWER_CASE_EXPANDED_TERMS_KEY,
			true));

		//ADD SUPPORT FOR LEADING WILDCARDS
		parser.setAllowLeadingWildcard(true);
		parser.setMultiTermRewriteMethod(MultiTermQuery.SCORING_BOOLEAN_QUERY_REWRITE);
		return parser;
	}
}
