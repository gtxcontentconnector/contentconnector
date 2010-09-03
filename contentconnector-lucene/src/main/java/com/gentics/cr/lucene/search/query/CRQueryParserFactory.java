package com.gentics.cr.lucene.search.query;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MultiTermQuery;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRRequest;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.LuceneVersion;

public class CRQueryParserFactory {

	private static String MAX_CLAUSES_KEY="maxqueryclauses";
	
	private static String USE_CRQUERYPARSER_KEY="usecrqueryparser";
	
	private static String QUERY_PARSER_CONFIG="queryparser";
	
	/***
	 * Generates a prepared and configured QueryParser
	 * @param searchedAttributes
	 * @param analyzer
	 * @param request
	 * @param config
	 * @return
	 */
	public static QueryParser getConfiguredParser(String[] searchedAttributes, Analyzer analyzer, CRRequest request, CRConfig config)
	{
		  QueryParser parser = null;
		  
		  
		  Object subconfig = config.get(QUERY_PARSER_CONFIG);
		  if(subconfig!=null && subconfig instanceof GenericConfiguration)
		  {
			  GenericConfiguration pconfig = (GenericConfiguration)subconfig;
			  
			  
			  //TODO CONTINUE HERE
		  }
		  
		  
		  
		  String useCRQUeryParser = config.getString(USE_CRQUERYPARSER_KEY);
		  if(useCRQUeryParser!=null && Boolean.parseBoolean(useCRQUeryParser))
		  {
			  parser = new CRQueryParser(LuceneVersion.getVersion(), searchedAttributes, analyzer, request);  
		  }
		  else
		  {
			  //USE DEFAULT QUERY PARSER
			  parser = new QueryParser(LuceneVersion.getVersion(),searchedAttributes[0],analyzer);
		  }
		  
		  //CONFIGURE MAX CLAUSES
	      String maxQueryClausesString = config.getString(MAX_CLAUSES_KEY);
	      if(maxQueryClausesString!=null && !"".equals(maxQueryClausesString))
	      {
	    	  BooleanQuery.setMaxClauseCount(Integer.parseInt(maxQueryClausesString));
	      }
		  
		  //ADD SUPPORT FOR LEADING WILDCARDS
		  parser.setAllowLeadingWildcard(true);
	      parser.setMultiTermRewriteMethod(MultiTermQuery.SCORING_BOOLEAN_QUERY_REWRITE);
	      
		  return parser;
	}
}
