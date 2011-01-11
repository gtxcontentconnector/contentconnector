package com.gentics.cr.lucene.search.query;

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
	 * Default constructor to prevent instantiation of utility class.
	 */
	private CRQueryParserFactory() { }
	
	/**
	 * max clause key.
	 */
	private static final String MAX_CLAUSES_KEY = "maxqueryclauses";
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
	public static QueryParser getConfiguredParser(
			final String[] searchedAttributes, final Analyzer analyzer,
			final CRRequest request, final CRConfig config) {
		  QueryParser parser = null;
		  
		  
		  Object subconfig = config.get(QUERY_PARSER_CONFIG);
		  if (subconfig != null && subconfig instanceof GenericConfiguration) {
			  GenericConfiguration pconfig = (GenericConfiguration) subconfig;
			  
			  String parserClass = pconfig.getString(QUERY_PARSER_CLASS);
			  if (parserClass != null) {
				parser = (QueryParser) Instanciator.getInstance(parserClass,
						new Object[][]{new Object[]{LuceneVersion.getVersion(),
								searchedAttributes, analyzer, request}});	
			  }
		  }
		  
		  if (parser == null) {
			  //USE DEFAULT QUERY PARSER
			  parser = new QueryParser(LuceneVersion.getVersion(),
					  searchedAttributes[0], analyzer);
		  }
		  
		  //CONFIGURE MAX CLAUSES
	      String maxQueryClausesString = config.getString(QUERY_PARSER_CONFIG 
	    		  + "." + MAX_CLAUSES_KEY);
	      if (maxQueryClausesString != null 
	    		  && !"".equals(maxQueryClausesString)) {
	    	  BooleanQuery.setMaxClauseCount(
	    			  Integer.parseInt(maxQueryClausesString));
	      }
		  
		  //ADD SUPPORT FOR LEADING WILDCARDS
		  parser.setAllowLeadingWildcard(true);
	      parser.setMultiTermRewriteMethod(MultiTermQuery
	    		  .SCORING_BOOLEAN_QUERY_REWRITE);
	      
		  return parser;
	}
}
