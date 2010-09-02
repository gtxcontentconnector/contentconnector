package com.gentics.cr.lucene.search.query;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MultiTermQuery;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRRequest;
import com.gentics.cr.lucene.LuceneVersion;

public class CRQueryParserFactory {

	private static String MAX_CLAUSES_KEY="maxqueryclauses";
	
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
		  CRQueryParser parser = new CRQueryParser(LuceneVersion.getVersion(), searchedAttributes, analyzer, request);
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
