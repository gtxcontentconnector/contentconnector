package com.gentics.cr.lucene.search;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocCollector;

import com.gentics.cr.CRConfig;
import com.gentics.cr.lucene.indexer.index.IndexLocation;
/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class CRSearcher {
	
	protected static Logger log = Logger.getLogger(CRSearcher.class);
	protected static Logger log_explain = Logger.getLogger(CRSearcher.class);
	
	protected static final String INDEX_LOCATION_KEY = "indexLocation";
	protected static final String STEMMING_KEY = "STEMMING";
	protected static final String STEMMER_NAME_KEY = "STEMMERNAME";
	
	protected String indexPath;
	protected CRConfig config;
	/**
	 * Create new instance of CRSearcher
	 * @param indexPath
	 */
	public CRSearcher(String indexPath) {
		this.indexPath = indexPath;
		this.config=null;
	}
	
	/**
	 * Create new instance of CRSearcher
	 * @param config
	 */
	public CRSearcher(CRConfig config) {
		this.config = config;
		this.indexPath = (String)config.get(INDEX_LOCATION_KEY);
	}
	
	/**
	 * Search in lucene index
	 * @param query query string
	 * @param searchedAttributes
	 * @param count - max number of results that are to be returned
	 * @param explain - if set to true the searcher will add extra explain output to the logger com.gentics.cr.lucene.searchCRSearcher.explain
	 * @return HashMap<String,Object with two entries. Entry "query" contains the paresed query and entry "result" contains a Collection of result documents.
	 */
	public HashMap<String,Object> search(String query,String[] searchedAttributes,int count,boolean explain) {
		try {
			IndexReader reader;
			IndexSearcher searcher;
			Analyzer analyzer;
		
			//Directory directory = FSDirectory.getDirectory(indexPath);
			IndexLocation idsLocation = IndexLocation.getIndexLocation(this.config);
			reader = IndexReader.open(idsLocation.getDirectory(), true);
			searcher = new IndexSearcher(reader);
			boolean doStemming = Boolean.parseBoolean((String)this.config.get(STEMMING_KEY));
			if(doStemming)
			{
				analyzer = new SnowballAnalyzer((String)this.config.get(STEMMER_NAME_KEY));
			}
			else
			{
				analyzer = new StandardAnalyzer();
			}
			
			if(searchedAttributes!=null && searchedAttributes.length>0)
			{
				QueryParser parser = new QueryParser(searchedAttributes[0], analyzer);
				
				Query parsedQuery = parser.parse(query);
				
				HashMap<String,Object> result = new HashMap<String,Object>(2);
				result.put("query", parsedQuery);
				LinkedHashMap<Document,Float> coll = runSearch(searcher,parsedQuery,count,explain);
				result.put("result", coll);
				int size=0;
				if(coll!=null)size=coll.size();
				log.debug("Found "+size+" objects with query: "+query);
				return(result);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return(null);
	}

	/**
	 * Run a Search against the lucene index
	 * @param searcher
	 * @param parsedQuery
	 * @param count
	 * @return ArrayList of results
	 */
	private LinkedHashMap<Document,Float> runSearch(IndexSearcher searcher, Query parsedQuery, int count,boolean explain) {
		try {
		    TopDocCollector collector = new TopDocCollector(count);
		    searcher.search(parsedQuery, collector);
		    ScoreDoc[] hits = collector.topDocs().scoreDocs;
	
		    LinkedHashMap<Document,Float> result = new LinkedHashMap<Document,Float>(hits.length);
		    log.debug("Found "+hits.length+" Documents");
		    for(int i = 0 ; i < hits.length ; i++) {
		    	Document doc = searcher.doc(hits[i].doc);
		    	result.put(doc,hits[i].score);
		    	if(explain)
		    	{
		    		Explanation ex = searcher.explain(parsedQuery, hits[i].doc);
		    		log_explain.debug("Explanation for "+doc.toString()+" - "+ex.toString());
		    	}
			}
			
			return(result);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return(null);
	}
}
