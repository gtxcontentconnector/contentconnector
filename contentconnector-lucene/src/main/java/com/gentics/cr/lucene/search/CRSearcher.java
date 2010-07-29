package com.gentics.cr.lucene.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopDocsCollector;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.search.TopScoreDocCollector;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRRequest;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.LuceneVersion;
import com.gentics.cr.lucene.didyoumean.DidYouMeanProvider;
import com.gentics.cr.lucene.indexaccessor.IndexAccessor;
import com.gentics.cr.lucene.indexer.index.LuceneAnalyzerFactory;
import com.gentics.cr.lucene.indexer.index.LuceneIndexLocation;
import com.gentics.cr.util.generics.Instanciator;
/**
 * 
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 545 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class CRSearcher {

  protected static Logger log = Logger.getLogger(CRSearcher.class);
  protected static Logger log_explain = Logger.getLogger(CRSearcher.class);

  protected static final String INDEX_LOCATION_KEY = "indexLocation";
  protected static final String COMPUTE_SCORES_KEY = "computescores";
  protected static final String STEMMING_KEY = "STEMMING";
  protected static final String STEMMER_NAME_KEY = "STEMMERNAME";
  private static final String COLLECTOR_CLASS_KEY = "collectorClass";
  private static final String COLLECTOR_CONFIG_KEY = "collector";
  
  private static final String DIDYOUMEAN_ENABLED_KEY="didyoumean";
  private static final String DIDYOUMEAN_SUGGEST_COUNT_KEY="didyoumeansuggestions";
  private static final String DIDYOUMEAN_MIN_SCORE="didyoumeanminscore";

  protected CRConfig config;
  private boolean computescores = true;
  private boolean didyoumeanenabled=false;
  private int didyoumeansuggestcount = 5;
  private float didyoumeanminscore = 0.5f;
  
  private DidYouMeanProvider didyoumeanprovider = null;
  
  /**
   * Create new instance of CRSearcher
   * @param config
   */
  public CRSearcher(CRConfig config) {
    this.config = config;
    String s_cs = config.getString(COMPUTE_SCORES_KEY);
    if(s_cs!=null && !"".equals(s_cs)) {
      computescores = Boolean.parseBoolean(s_cs);
    }
    
    String s_didyoumeanenabled = config.getString(DIDYOUMEAN_ENABLED_KEY);
    if(s_didyoumeanenabled!=null && !"".equals(s_didyoumeanenabled))
    {
    	didyoumeanenabled = Boolean.parseBoolean(s_didyoumeanenabled);
    }
    
    String s_suggestcount = config.getString(DIDYOUMEAN_SUGGEST_COUNT_KEY);
    if(s_suggestcount!=null && !"".equals(s_suggestcount))
    {
    	didyoumeansuggestcount = Integer.parseInt(s_suggestcount);
    }
    
    String s_minscore = config.getString(DIDYOUMEAN_MIN_SCORE);
    if(s_minscore!=null && !"".equals(s_minscore))
    {
    	didyoumeanminscore = Float.parseFloat(s_minscore);
    }
    
    //if configured => initialize DIDYOUMEAN Provider
    if(didyoumeanenabled)
    {
    	this.didyoumeanprovider = new DidYouMeanProvider(config);
    }
  }

  /**
   * Create the appropriate collector.
   * @param hits
   * @param sorting
   * @return
   * @throws IOException 
   */
  @SuppressWarnings("unchecked")
private TopDocsCollector<?> createCollector(final Searcher searcher,
      final int hits, final String[] sorting, final boolean computescores,
      final String[] userPermissions) throws IOException {
    TopDocsCollector<?> coll = null;
    String collectorClassName = (String) config.get(COLLECTOR_CLASS_KEY);
    if (collectorClassName != null) {
      Class<?> genericCollectorClass;
      try {
        genericCollectorClass = Class.forName(collectorClassName);
        GenericConfiguration collectorConfiguration =
          config.getSubConfigs().get(COLLECTOR_CONFIG_KEY.toUpperCase());
        Object[][] prioritizedParameters = new Object[3][];
        prioritizedParameters[0] =
          new Object[]{searcher, hits, collectorConfiguration, userPermissions};
        prioritizedParameters[1] =
          new Object[]{searcher, hits, collectorConfiguration};
        prioritizedParameters[2] =
          new Object[]{hits, collectorConfiguration};
        Object collectorObject = Instanciator.getInstance(genericCollectorClass,
            prioritizedParameters);
        if (collectorObject instanceof TopDocsCollector) {
          coll = (TopDocsCollector<?>) collectorObject;
        }
      } catch (ClassNotFoundException e) {
        log.error("Cannot find configured collector class: \""
            + collectorClassName + "\" in " + config.getName(), e);
      }

    }
    if (coll == null && sorting != null) {
      //TODO make collector configurable
      coll = TopFieldCollector.create(createSort(sorting), hits, true,
          computescores, computescores, computescores);
    }
    if (coll == null) {
      coll = TopScoreDocCollector.create(hits, true);
    }
    return coll;
  }

  /**
   * Creates a Sort object for the Sort collector. The general syntax for sort
   * properties is [property][:asc|:desc] where the postfix determines the
   * sortorder. If neither :asc nor :desc is given, the sorting will be done
   * ascending for this property.
   * @param sorting
   * @return
   */
  private Sort createSort(String[] sorting) {
    Sort ret = null;
    ArrayList<SortField> sf = new ArrayList<SortField>();
    for (String s : sorting) {
      // split attribute on :. First element is attribute name the
      // second is the direction
      String[] sort = s.split(":");

      if (sort[0] != null) {
        boolean reverse;
        if ("desc".equals(sort[1].toLowerCase())) {
          reverse = true;
        } else {
          reverse = false;
        }
        sf.add(new SortField(sort[0], Locale.getDefault(), reverse));
      }

    }
    ret = new Sort(sf.toArray(new SortField[]{}));

    return ret;
  }

  public void finalize() {
    LuceneIndexLocation idsLocation =
      LuceneIndexLocation.getIndexLocation(this.config);
    if (idsLocation != null) {
      idsLocation.finalize();
    }
  }

  /**
   * Run a Search against the lucene index.
   * @param searcher TODO javadoc
   * @param parsedQuery TODO javadoc
   * @param count TODO javadoc
   * @param collector TODO javadoc
   * @param explain TODO javadoc
   * @param start TODO javadoc
   * @return ArrayList of results
   */
  private HashMap<String,Object> runSearch(
      final TopDocsCollector<?> collector, final Searcher searcher,
      final Query parsedQuery, final boolean explain, final int count,
      final int start) {
    try {

      searcher.search(parsedQuery, collector);
      TopDocs tdocs = collector.topDocs();
      Float maxScoreReturn = tdocs.getMaxScore();
      ScoreDoc[] hits = tdocs.scoreDocs;
      
      LinkedHashMap<Document, Float> result =
        new LinkedHashMap<Document, Float>(hits.length);

        //Calculate the number of documents to be fetched
        int num = Math.min(hits.length - start, count);
        for (int i = 0; i < num; i++) {
          Document doc = searcher.doc(hits[start + i].doc);
          //add id field for AdvancedContentHighlighter
          doc.add(new Field("id", hits[start + i].doc + "", Field.Store.YES,
              Field.Index.NO));
          result.put(doc, hits[start + i].score);
          if (explain) {
            Explanation ex = searcher.explain(parsedQuery, hits[start + i].doc);
            log_explain.debug("Explanation for " + doc.toString() + " - "
                + ex.toString());
          }
      }
      log.debug("Fetched Document " + start + " to " + (start + num) + " of "
          + collector.getTotalHits() + " found Documents");

      HashMap<String,Object> ret = new HashMap<String,Object>(2);
      ret.put("result", result);
      ret.put("maxscore",maxScoreReturn);
      return ret;

    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public HashMap<String,Object> search(String query,
      String[] searchedAttributes, int count, int start, boolean explain)
      throws IOException {
    return search(query, searchedAttributes, count, start, explain, null);
  }

  public HashMap<String,Object> search(String query,String[] searchedAttributes,int count,int start,boolean explain, String[] sorting) throws IOException{
    return search(query, searchedAttributes, count, start, explain, sorting, null);
  }
  /**
   * Search in lucene index.
   * @param query query string
   * @param searchedAttributes TODO javadoc
   * @param count - max number of results that are to be returned
   * @param start - the start number of the page e.g. if start = 50 and count =
   * 10 you will get the elements 50 - 60
   * @param explain - if set to true the searcher will add extra explain output
   * to the logger com.gentics.cr.lucene.searchCRSearcher.explain
   * @param sorting - this argument takes the sorting array that can look like
   * this: ["contentid:asc","name:desc"]
   * @param request TODO javadoc
   *
   * @return HashMap&lt;String,Object&gt; with two entries. Entry "query"
   * contains the parsed query and entry "result" contains a Collection of
   * result documents.
   * @throws IOException TODO javadoc
   */
  @SuppressWarnings("unchecked")
public final HashMap<String, Object> search(final String query,
      final String[] searchedAttributes, final int count, final int start,
      final boolean explain, final String[] sorting, final CRRequest request)
      throws IOException {


    Searcher searcher;
    Analyzer analyzer;
    //Collect count + start hits
    int hits = count + start;

    LuceneIndexLocation idsLocation =
      LuceneIndexLocation.getIndexLocation(this.config);

    IndexAccessor indexAccessor = idsLocation.getAccessor();
    searcher = indexAccessor.getPrioritizedSearcher();
    Object userPermissionsObject = request.get(CRRequest.PERMISSIONS_KEY);
    String[] userPermissions = new String[0];
    if (userPermissionsObject instanceof String[]) {
      userPermissions = (String[]) userPermissionsObject;
    }
    TopDocsCollector<?> collector =
      createCollector(searcher, hits, sorting, computescores, userPermissions);
    HashMap<String, Object> result = null;
    try {

      analyzer = LuceneAnalyzerFactory
          .createAnalyzer((GenericConfiguration) this.config);

      if (searchedAttributes != null && searchedAttributes.length > 0) {
        CRQueryParser parser =
          new CRQueryParser(LuceneVersion.getVersion(), searchedAttributes,
              analyzer, request);
        parser.setAllowLeadingWildcard(true);

        Query parsedQuery = parser.parse(query);
        result = new HashMap<String, Object>(3);
        result.put("query", parsedQuery);
        
        
        Map<String,Object> ret = runSearch(collector, searcher, parsedQuery, explain, count, start);
        LinkedHashMap<Document, Float> coll = (LinkedHashMap<Document, Float>)ret.get("result");
        Float maxScore  = (Float)ret.get("maxscore");
        result.put("result", coll);
        int totalhits = collector.getTotalHits();
        
        result.put("hits", totalhits);
        result.put("maxscore",maxScore);
        //PLUG IN DIDYOUMEAN
        if(didyoumeanenabled && (totalhits < 1 || maxScore < this.didyoumeanminscore))
        {
        	Set<Term> termset = new HashSet<Term>();
        	parsedQuery.extractTerms(termset);
        	
        	IndexReader reader = indexAccessor.getReader(false);
        	Map<String,String[]> suggestions = this.didyoumeanprovider.getSuggestions(termset, this.didyoumeansuggestcount, reader);
        	result.put("suggestions", suggestions);
        	parsedQuery.rewrite(reader);
        	String rewrittenQuery = parsedQuery.toString();
        	indexAccessor.release(reader, false);
        	
        	//SPECIAL SUGGESTION
        	for(Entry<String,String[]> e:suggestions.entrySet())
        	{
        		String term = e.getKey();
        		String[] term_suggestions = e.getValue();
        		if(term_suggestions!=null && term_suggestions.length>0)
        		{
        			rewrittenQuery = rewrittenQuery.replaceAll(term, term_suggestions[0]);
        		}
        	}
        	Query bestQuery = parser.parse(rewrittenQuery);
        	TopDocsCollector<?> bestcollector = createCollector(searcher, 1, sorting, computescores, userPermissions);
        	runSearch(bestcollector, searcher, bestQuery, false, 1, 0);
        	result.put("bestquery", rewrittenQuery);
        	result.put("bestqueryhits", bestcollector.getTotalHits());
        }
        
        //PLUG IN DIDYOUMEAN END
        int size = 0;
        if (coll != null) {
          size = coll.size();
        }
        log.debug("Fetched " + size + " objects with query: " + query);
      }
    } catch (Exception e) {
      log.error("Error getting the results.", e);
      result = null;
    } finally {
      indexAccessor.release(searcher);
    }
    return result;
  }
}
