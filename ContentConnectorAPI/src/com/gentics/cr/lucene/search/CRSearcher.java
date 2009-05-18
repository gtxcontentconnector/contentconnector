package com.gentics.cr.lucene.search;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class CRSearcher {
	
	protected Logger log = Logger.getLogger("com.gentics.cr.lucene.search");
	protected String indexPath;
	
	/**
	 * Create new instance of CRSearcher
	 * @param indexPath
	 */
	public CRSearcher(String indexPath) {
		this.indexPath = indexPath;
	}
	
	/**
	 * Search in lucene index
	 * @param query query string
	 * @param searchedAttributes
	 * @param count - max number of results that are to be returned
	 * @return HashMap<String,Object with two entries. Entry "query" contains the paresed query and entry "result" contains a Collection of result documents.
	 */
	public HashMap<String,Object> search(String query,String[] searchedAttributes,int count) {
		try {
			IndexReader reader;
			IndexSearcher searcher;
			Analyzer analyzer;
		
			Directory directory = FSDirectory.getDirectory(indexPath);
			reader = IndexReader.open(directory, true);
			searcher = new IndexSearcher(reader);
			analyzer = new StandardAnalyzer();
			
			if(searchedAttributes!=null && searchedAttributes.length>0)
			{
				QueryParser parser = new QueryParser(searchedAttributes[0], analyzer);
				
				Query parsedQuery = parser.parse(query);
				
				HashMap<String,Object> result = new HashMap<String,Object>(2);
				result.put("query", parsedQuery);
				result.put("result", runSearch(searcher,parsedQuery,count));
				
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
	private ArrayList<Document> runSearch(IndexSearcher searcher, Query parsedQuery, int count) {
		try {
		    TopDocCollector collector = new TopDocCollector(count);
		    searcher.search(parsedQuery, collector);
		    ScoreDoc[] hits = collector.topDocs().scoreDocs;
	
		    ArrayList<Document> result = new ArrayList<Document>(hits.length);
		    this.log.debug("Found "+hits.length+" Documents");
		    for(int i = 0 ; i < hits.length ; i++) {
				Document doc = searcher.doc(hits[i].doc);
				result.add(doc);
			}
			
			return(result);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return(null);
	}
}
