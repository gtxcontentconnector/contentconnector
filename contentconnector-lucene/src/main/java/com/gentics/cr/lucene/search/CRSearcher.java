package com.gentics.cr.lucene.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopDocsCollector;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.search.TopScoreDocCollector;

import com.gentics.api.lib.etc.ObjectTransformer;
import com.gentics.cr.CRConfig;
import com.gentics.cr.CRRequest;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.lucene.didyoumean.DidyoumeanIndexExtension;
import com.gentics.cr.lucene.facets.search.FacetsSearch;
import com.gentics.cr.lucene.facets.search.FacetsSearchConfigKeys;
import com.gentics.cr.lucene.facets.taxonomy.taxonomyaccessor.TaxonomyAccessor;
import com.gentics.cr.lucene.indexaccessor.IndexAccessor;
import com.gentics.cr.lucene.indexer.index.LuceneAnalyzerFactory;
import com.gentics.cr.lucene.indexer.index.LuceneIndexLocation;
import com.gentics.cr.lucene.search.query.BooleanQueryRewriter;
import com.gentics.cr.lucene.search.query.CRQueryParserFactory;
import com.gentics.cr.util.StringUtils;
import com.gentics.cr.util.generics.Instanciator;

/**
 * CRSearcher.
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 545 $
 * @author $Author: supnig@constantinopel.at $
 */
public class CRSearcher {

	private static Logger log = Logger.getLogger(CRSearcher.class);
	private static Logger log_explain = Logger.getLogger(CRSearcher.class);

	protected static final String INDEX_LOCATION_KEY = "indexLocation";
	protected static final String COMPUTE_SCORES_KEY = "computescores";
	protected static final String STEMMING_KEY = "STEMMING";
	protected static final String STEMMER_NAME_KEY = "STEMMERNAME";
	protected static final String COLLECTOR_CLASS_KEY = "collectorClass";
	
	private static final String COLLECTOR_CONFIG_KEY = "collector";

	public static final String RETRIEVE_COLLECTOR_KEY = "collectorInResult";
	public static final String RESULT_COLLECTOR_KEY = "collector";
	
	public static final String RETRIEVE_UNIQUE_MIMETYPES_KEY = "retrieveUniqueMimetypes";
	public static final String RESULT_UNIQUE_MIMETYPES_KEY = "unique_mimetypes";
	
	private static final String LUCENE_INDEX_MIMETYPE = "mimetype";

	/**
	 * Key to store the searchquery in the result.
	 */
	public static final String RESULT_QUERY_KEY = "query";
	/**
	 * Key to store the hitcount in the result.
	 */
	public static final String RESULT_HITS_KEY = "hits";
	/**
	 * Key to store the result in the result.
	 */
	public static final String RESULT_RESULT_KEY = "result";

	/**
	 * Key to store the maximum score of the result in the result.
	 */
	public static final String RESULT_MAXSCORE_KEY = "maxscore";

	/**
	 * Key to store the bestquery in the result.
	 */
	public static final String RESULT_BESTQUERY_KEY = "bestquery";

	/**
	 * Key to store the hitcount of the bestquery in the result.
	 */
	public static final String RESULT_BESTQUERYHITS_KEY = "bestqueryhits";

	/**
	 * Key to store the map for the suggestion terms as strings in the result.
	 */
	public static final String RESULT_SUGGESTIONS_KEY = "suggestions";

	/**
	 * Key to store the map for the suggestion terms in the result.
	 */
	public static final String RESULT_SUGGESTIONTERMS_KEY = "suggestionTerms";

	/**
	 * Key to put the suggested term for the bestresult into the result.
	 */
	public static final String RESULT_SUGGESTEDTERM_KEY = "suggestedTerm";

	/**
	 * Key to put the orignal term for the suggested term into the bestresult.
	 */
	private static final String RESULT_ORIGTERM_KEY = "originalTerm";

	/**
	 * Key to configure the limit of results we activate the didyoumean code.
	 */
	private static final String DIDYOUMEAN_ACTIVATE_KEY = "didyoumean_activatelimit";

	public static final String DIDYOUMEAN_ENABLED_KEY = "didyoumean";
	private static final String DIDYOUMEAN_BESTQUERY_KEY = "didyoumeanbestquery";
	private static final String ADVANCED_DIDYOUMEAN_BESTQUERY_KEY = "didyoumeanbestqueryadvanced";
	private static final String DIDYOUMEAN_SUGGEST_COUNT_KEY = "didyoumeansuggestions";
	private static final String DIDYOUMEAN_MIN_SCORE = "didyoumeanminscore";

	protected CRConfig config;
	private boolean computescores = true;
	private boolean didyoumeanenabled = false;
	private boolean didyoumeanbestquery = false;
	private boolean advanceddidyoumeanbestquery = false;
	private int didyoumeansuggestcount = 5;
	private float didyoumeanminscore = 0.5f;

	private FacetsSearch facetsSearch;
	
	/**
	 * retrieve unique mimetypes and put it in result.
	 */
	private boolean retrieveUniqueMimeTypes = false;
	
	/**
	 * put used collector in metadata.
	 */
	private boolean retrieveCollector = false;

	/**
	 * resultsizelimit to activate the didyoumeanfunctionality.
	 */
	private int didyoumeanactivatelimit = 0;

	private DidyoumeanIndexExtension didyoumeanprovider = null;

	/**
	 * Create new instance of CRSearcher.
	 * @param config
	 */
	public CRSearcher(final CRConfig config) {
		this.config = config;
		computescores = config.getBoolean(COMPUTE_SCORES_KEY, computescores);
		didyoumeanenabled = config.getBoolean(DIDYOUMEAN_ENABLED_KEY, didyoumeanenabled);
		didyoumeansuggestcount = config.getInteger(DIDYOUMEAN_SUGGEST_COUNT_KEY, didyoumeansuggestcount);
		didyoumeanminscore = config.getFloat(DIDYOUMEAN_MIN_SCORE, didyoumeanminscore);

		if (didyoumeanenabled) {
			didyoumeanprovider = new DidyoumeanIndexExtension(config, LuceneIndexLocation.getIndexLocation(config));
			didyoumeanbestquery = config.getBoolean(DIDYOUMEAN_BESTQUERY_KEY, didyoumeanbestquery);
			advanceddidyoumeanbestquery = config.getBoolean(ADVANCED_DIDYOUMEAN_BESTQUERY_KEY, advanceddidyoumeanbestquery);
			didyoumeanactivatelimit = config.getInteger(DIDYOUMEAN_ACTIVATE_KEY, didyoumeanactivatelimit);
		}

		facetsSearch = new FacetsSearch(config);
		
		retrieveUniqueMimeTypes = config.getBoolean(RETRIEVE_UNIQUE_MIMETYPES_KEY);
		retrieveCollector = config.getBoolean(RETRIEVE_COLLECTOR_KEY);

	}

	/**
	 * Create the appropriate collector.
	 * 
	 * @param hits
	 * @param sorting
	 * @return
	 * @throws IOException
	 */
	TopDocsCollector<?> createCollector(final CRRequest request, final IndexSearcher searcher, final int hits, final String[] sorting, final boolean computescores,
			final String[] userPermissions) throws IOException {
		TopDocsCollector<?> coll = null;
		String collectorClassName = (String) config.get(COLLECTOR_CLASS_KEY);
		if (collectorClassName != null) {
			Class<?> genericCollectorClass;
			try {
				genericCollectorClass = Class.forName(collectorClassName);
				GenericConfiguration collectorConfiguration = config.getSubConfigs().get(COLLECTOR_CONFIG_KEY.toUpperCase());
				Object[][] prioritizedParameters = new Object[6][];
				prioritizedParameters[0] = new Object[] { createSort(sorting), searcher, hits, collectorConfiguration };
				prioritizedParameters[1] = new Object[] { searcher, hits, collectorConfiguration, userPermissions };
				prioritizedParameters[2] = new Object[] { searcher, hits, collectorConfiguration };
				prioritizedParameters[3] = new Object[] { hits, collectorConfiguration };
				prioritizedParameters[4] = new Object[]	{ request, createSort(sorting), searcher, hits, collectorConfiguration };
				prioritizedParameters[5] = new Object[] { request, searcher, hits, collectorConfiguration };
				Object collectorObject = Instanciator.getInstance(genericCollectorClass, prioritizedParameters);
				if (collectorObject instanceof TopDocsCollector) {
					coll = (TopDocsCollector<?>) collectorObject;
				}
			} catch (ClassNotFoundException e) {
				log.error("Cannot find configured collector class: \"" + collectorClassName + "\" in " + config.getName(), e);
			}

		}
		if (coll == null && sorting != null) {
			// TODO make collector configurable
			coll = TopFieldCollector.create(createSort(sorting), hits, true, computescores, computescores, computescores);
		}
		if (coll == null) {
			coll = TopScoreDocCollector.create(hits, true);
		}
		return coll;
	}

	/**
	 * Creates a Sort object for the Sort collector. The general syntax for sort properties is [property][:asc|:desc]
	 * where the postfix determines the sortorder. If neither :asc nor :desc is given, the sorting will be done
	 * ascending for this property.
	 * 
	 * NOTE: using "score:asc" or "score:desc" will both result in an ascating relevance sorting
	 * 
	 * @param sorting
	 * @return
	 */
	private Sort createSort(final String[] sorting) {
		Sort ret = null;
		if (sorting == null) {
			return null;
		}
		ArrayList<SortField> sortFields = new ArrayList<SortField>();
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
				if ("score".equalsIgnoreCase(sort[0])) {
					sortFields.add(SortField.FIELD_SCORE);
				} else {
					sortFields.add(new SortField(sort[0], SortField.Type.STRING, reverse));
				}
			}

		}
		ret = new Sort(sortFields.toArray(new SortField[] {}));

		return ret;
	}

	/**
	 * Run a Search against the lucene index.
	 * 
	 * @param searcher
	 * @param parsedQuery
	 * @param count
	 * @param collector
	 * @param explain
	 * @param start
	 * @return ArrayList of results
	 */
	private HashMap<String, Object> executeSearcher(final TopDocsCollector<?> collector, final IndexSearcher searcher, final Query parsedQuery,
			final boolean explain, final int count, final int start) {
		return executeSearcher(collector, searcher, parsedQuery, explain, count, start, null, null);
	}

	/**
	 * Run a Search against the lucene index.
	 * 
	 * @param searcher
	 * @param parsedQuery
	 * @param count
	 * @param ttcollector
	 * @param explain
	 * @param start
	 * @param facetsCollector a {@link FacetsCollector} 
	 * @return ArrayList of results
	 */
	private HashMap<String, Object> executeSearcher(final TopDocsCollector<?> ttcollector, final IndexSearcher searcher, final Query parsedQuery,
			final boolean explain, final int count, final int start, final FacetsCollector facetsCollector, final Filter filter) {
		try {
			
			Collector collector = null;
			if (facetsCollector != null) {
				// wrap the TopDocsCollector and the FacetsCollector to one
				// MultiCollector and perform the search
				collector = MultiCollector.wrap(ttcollector, facetsCollector);
			} else {
				collector = ttcollector;
			}
			
			if (filter != null) {
				searcher.search(parsedQuery, filter, collector);
			} else {
				searcher.search(parsedQuery, collector);
			}
			TopDocs tdocs = ttcollector.topDocs(start, count);

			float maxScoreReturn = tdocs.getMaxScore();
			log.debug("maxScoreReturn: " + maxScoreReturn);

			ScoreDoc[] hits = tdocs.scoreDocs;
			log.debug("hits (topdocs): \n" + StringUtils.getCollectionSummary(Arrays.asList(hits), "\n"));

			LinkedHashMap<Document, Float> result = new LinkedHashMap<Document, Float>(hits.length);

			// Calculate the number of documents to be fetched
			int num = Math.min(hits.length, count);
			for (int i = 0; i < num; i++) {
				ScoreDoc currentDoc = hits[i];
				if (currentDoc.doc != Integer.MAX_VALUE) {
					log.debug("currentDoc id: " + currentDoc.doc + " ; score: " + currentDoc.score);
					Document doc = searcher.doc(currentDoc.doc);
					// add id field for AdvancedContentHighlighter
					doc.add(new Field("id", hits[i].doc + "", Field.Store.YES, Field.Index.NO));
					log.debug("adding contentid: " + doc.getField("contentid"));
					log.debug("with hits[" + i + "].score = " + hits[i].score);
					result.put(doc, hits[i].score);
					if (explain) {
						Explanation ex = searcher.explain(parsedQuery, hits[i].doc);
						log_explain.debug("Explanation for " + doc.toString() + " - " + ex.toString());
					}
				} else {
					log.error("Loading search documents failed partly (document has MAX_INTEGER as document id");
				}
			}
			log.debug("Fetched Document " + start + " to " + (start + num) + " of " + ttcollector.getTotalHits() + " found Documents");

			HashMap<String, Object> ret = new HashMap<String, Object>(3);
			
			if (retrieveCollector) {
				ret.put(RESULT_COLLECTOR_KEY, collector);
			}
			ret.put(RESULT_RESULT_KEY, result);
			ret.put(RESULT_MAXSCORE_KEY, maxScoreReturn);
			return ret;

		} catch (Exception e) {
			log.error("Error running search for query " + parsedQuery, e);
		}
		return null;
	}

	public HashMap<String, Object> search(final String query, final String[] searchedAttributes, final int count, final int start,
			final boolean explain) throws IOException, CRException {
		return search(query, searchedAttributes, count, start, explain, null);
	}

	public HashMap<String, Object> search(final String query, final String[] searchedAttributes, final int count, final int start,
			final boolean explain, final String[] sorting) throws IOException, CRException {
		return search(query, searchedAttributes, count, start, explain, sorting, new CRRequest());
	}

	/**
	 * Search in lucene index (executes executeSearcher).
	 * 
	 * @param query query string
	 * @param searchedAttributes TODO javadoc
	 * @param count - max number of results that are to be returned
	 * @param start - the start number of the page e.g. if start = 50 and count = 10 you will get the elements 50 - 60
	 * @param explain - if set to true the searcher will add extra explain output to the logger
	 *						com.gentics.cr.lucene.searchCRSearcher.explain
	 * @param sorting - this argument takes the sorting array that can look like this: ["contentid:asc","name:desc"]
	 * @param request TODO javadoc
	 * @return HashMap&lt;String,Object&gt; with two entries. Entry "query" contains the parsed query and entry "result"
	 *			contains a Collection of result documents.
	 * @throws IOException TODO javadoc
	 * @throws CRException in case maxclausecount is reached and failOnMaxClauses is enabled in the config object
	 */
	@SuppressWarnings("unchecked")
	public final HashMap<String, Object> search(final String query, final String[] searchedAttributes, final int count, final int start,
			final boolean explain, final String[] sorting, final CRRequest request) throws IOException, CRException {

		IndexSearcher searcher;
		Analyzer analyzer;
		// Collect count + start hits
		int hits = count + start;	// we want to retreive the startcount (start) to endcount (hits)

		LuceneIndexLocation idsLocation = LuceneIndexLocation.getIndexLocation(config);

		// TODO: maybe we should make this configurable. Imho this should not be a problem if enabled by default as once a search
		// has been executed the Factory is opened and we only need to make sure that on shutdown it is released.
		boolean reopenIndexFactory = true;
		IndexAccessor indexAccessor = idsLocation.getAccessor(reopenIndexFactory);
		if (indexAccessor == null) {
			log.error("IndexAccessor is null. Search will not work.");
		}

		// Resource needed for faceted search
		TaxonomyAccessor taAccessor = null;
		
		IndexReader uniqueMimeTypesIndexReader = null;
		
		List<String> uniqueMimeTypes = null;
		if (retrieveUniqueMimeTypes) {
			// retrieve all possible file types
			uniqueMimeTypesIndexReader = indexAccessor.getReader();
			AtomicReader aReader = SlowCompositeReaderWrapper.wrap(uniqueMimeTypesIndexReader);
			final Terms terms = aReader.fields().terms(LUCENE_INDEX_MIMETYPE);
			TermsEnum termEnum = terms.iterator(null);
			uniqueMimeTypes = new ArrayList<String>();
			while (termEnum.next() != null) {
				uniqueMimeTypes.add(termEnum.term().utf8ToString());
			}
		}

		// get accessors and reader only if facets are activated 
		if (facetsSearch.useFacets()) {
			taAccessor = idsLocation.getTaxonomyAccessor();
		}

		searcher = indexAccessor.getPrioritizedSearcher();
		Object userPermissionsObject = request.get(CRRequest.PERMISSIONS_KEY);
		String[] userPermissions = new String[0];
		if (userPermissionsObject instanceof String[]) {
			userPermissions = (String[]) userPermissionsObject;
		}
		TopDocsCollector<?> collector = createCollector(request, searcher, hits, sorting, computescores, userPermissions);
		
		String filterQuery = ObjectTransformer.getString(request.get(CRRequest.FILTER_QUERY_KEY), null);
		HashMap<String, Object> result = null;
		try {
			analyzer = LuceneAnalyzerFactory.createAnalyzer(config);

			if (searchedAttributes != null && searchedAttributes.length > 0 && query != null && !query.equals("")) {
				QueryParser parser = CRQueryParserFactory.getConfiguredParser(searchedAttributes, analyzer, request, config);

				Query parsedQuery = parser.parse(query);
				// GENERATE A NATIVE QUERY

				parsedQuery = searcher.rewrite(parsedQuery);

				result = new HashMap<String, Object>(3);
				result.put(RESULT_QUERY_KEY, parsedQuery);
				Filter filter = null;
				if (filterQuery != null) {
					log.debug("Using filter query: " + filterQuery);
					Query parsedFilterQuery = parser.parse(filterQuery);
					parsedFilterQuery = searcher.rewrite(parsedFilterQuery);
					filter = new QueryWrapperFilter(parsedFilterQuery);
				}

				// when facets are active create a FacetsCollector
				FacetsCollector facetsCollector = null;
				if (facetsSearch.useFacets()) {
					facetsCollector = facetsSearch.createFacetsCollector();
				}

				Map<String, Object> ret = executeSearcher(collector, searcher, parsedQuery, explain, count, start, facetsCollector, filter);
				if (log.isDebugEnabled()) {
					for (Object res : ret.values()) {
						if (res instanceof LinkedHashMap) {
							LinkedHashMap<Document, Float> documents = (LinkedHashMap<Document, Float>) res;
							if (documents != null) {
								for (Entry<Document, Float> doc : documents.entrySet()) {
									Document doCument = doc.getKey();
									log.debug("CRSearcher.search: "
									/* + doCument.toString() */
									+ " Contentid: " + doCument.get("contentid"));
								}
							}
						}
					}
				}
				if (ret != null) {
					LinkedHashMap<Document, Float> coll = (LinkedHashMap<Document, Float>) ret.get(RESULT_RESULT_KEY);
					float maxScore = (Float) ret.get(RESULT_MAXSCORE_KEY);
					result.put(RESULT_RESULT_KEY, coll);
					int totalhits = collector.getTotalHits();

					result.put(RESULT_HITS_KEY, totalhits);
					result.put(RESULT_MAXSCORE_KEY, maxScore);

					if (retrieveUniqueMimeTypes) {
						// add unique extensions
						result.put(RESULT_UNIQUE_MIMETYPES_KEY, uniqueMimeTypes);
					}
					if (retrieveCollector) {
						result.put(RESULT_COLLECTOR_KEY, ret.get(RESULT_COLLECTOR_KEY));
					}

					// PLUG IN DIDYOUMEAN
					boolean didyoumeanEnabledForRequest = StringUtils.getBoolean(request.get(DIDYOUMEAN_ENABLED_KEY), true);
					if (start == 0 && didyoumeanenabled && didyoumeanEnabledForRequest
							&& (totalhits <= didyoumeanactivatelimit || didyoumeanactivatelimit == -1 || maxScore < didyoumeanminscore)) {

						HashMap<String, Object> didyoumeanResult = didyoumean(
							query,
							parsedQuery,
							indexAccessor,
							parser,
							searcher,
							sorting,
							userPermissions,
							request);
						result.putAll(didyoumeanResult);
					}

					// PLUG IN DIDYOUMEAN END

					// if a facetsCollector was created, store the faceted search results in the meta resolveable
					if (facetsCollector != null) {
						result.put(FacetsSearchConfigKeys.RESULT_FACETS_LIST_KEY, facetsSearch.getFacetsResults(facetsCollector, taAccessor));
					}

					int size = 0;
					if (coll != null) {
						size = coll.size();
					}
					log.debug("Fetched " + size + " objects with query: " + query);
				} else {
					result = null;
				}
			}
		} catch (Exception e) {
			if (config.getBoolean("FAILONMAXCLAUSES")) {
				log.debug("Error getting the results.", e);
				throw new CRException(e);
			} else {
				log.error("Error getting the results.", e);
				result = null;
			}

		} finally {
			 /*
			  * if facets are activated cleanup the resources
			  * needed for faceted search
			  * 
			  * Always cleanup/release the taxonomy Reader/Writer 
			  * before the Reader/Writers of the main index!
			  */
			
			if (uniqueMimeTypesIndexReader != null) {
				indexAccessor.release(uniqueMimeTypesIndexReader);
			}
			indexAccessor.release(searcher);
		}
		return result;
	}

	/**
	 * get Result for didyoumean.
	 * 
	 * @param originalQuery - original query for fallback when wildcards are replaced with nothing.
	 * @param parsedQuery - parsed query in which we can replace the search words
	 * @param indexAccessor - accessor to get results from the index
	 * @param parser - query parser
	 * @param searcher - searcher to search in the index
	 * @param sorting - sorting to use
	 * @param userPermissions - user permission used to get the original result
	 * @return Map containing the replacement for the searchterm and the result for the resulting query.
	 */
	private HashMap<String, Object> didyoumean(final String originalQuery, final Query parsedQuery, final IndexAccessor indexAccessor,
			final QueryParser parser, final IndexSearcher searcher, final String[] sorting, final String[] userPermissions, final CRRequest request) {
		long dymStart = System.currentTimeMillis();
		HashMap<String, Object> result = new HashMap<String, Object>(3);

		IndexReader reader = null;
		try {
			reader = indexAccessor.getReader();
			Query rwQuery = parsedQuery;
			Set<Term> termset = new HashSet<Term>();
			rwQuery.extractTerms(termset);

			Map<Term, Term[]> suggestions = this.didyoumeanprovider.getSuggestionTerms(termset, this.didyoumeansuggestcount, reader);
			boolean containswildcards = originalQuery.indexOf('*') != -1;
			if (suggestions.size() == 0 && containswildcards) {
				String newSuggestionQuery = originalQuery.replaceAll("\\*", "");
				try {
					rwQuery = parser.parse(newSuggestionQuery);
					termset = new HashSet<Term>();
					// REWRITE NEWLY PARSED QUERY
					rwQuery = rwQuery.rewrite(reader);
					rwQuery.extractTerms(termset);
					suggestions = this.didyoumeanprovider.getSuggestionTerms(termset, this.didyoumeansuggestcount, reader);

				} catch (ParseException e) {
					log.error("Cannot Parse Suggestion Query.", e);
				}

			}
			result.put(RESULT_SUGGESTIONTERMS_KEY, suggestions);
			result.put(RESULT_SUGGESTIONS_KEY, didyoumeanprovider.getSuggestionsStringFromMap(suggestions));

			log.debug("DYM Suggestions took " + (System.currentTimeMillis() - dymStart) + "ms");

			if (didyoumeanbestquery || advanceddidyoumeanbestquery) {
				// SPECIAL SUGGESTION
				// TODO Test if the query will be altered and if any suggestion
				// have been made... otherwise don't execute second query and
				// don't include the bestquery
				for (Entry<Term, Term[]> e : suggestions.entrySet()) {
					Term term = e.getKey();
					Term[] suggestionsForTerm = e.getValue();
					if (advanceddidyoumeanbestquery) {
						TreeMap<Integer, HashMap<String, Object>> suggestionsResults = new TreeMap<Integer, HashMap<String, Object>>(
								Collections.reverseOrder());
						for (Term suggestedTerm : suggestionsForTerm) {
							Query newquery = BooleanQueryRewriter.replaceTerm(rwQuery, term, suggestedTerm);

							HashMap<String, Object> resultOfNewQuery = getResultsForQuery(newquery, searcher, sorting, userPermissions, request);
							if (resultOfNewQuery != null) {
								resultOfNewQuery.put(RESULT_SUGGESTEDTERM_KEY, suggestedTerm.text());
								resultOfNewQuery.put(RESULT_ORIGTERM_KEY, term.text());
								Integer resultCount = (Integer) resultOfNewQuery.get(RESULT_BESTQUERYHITS_KEY);
								if (resultCount > 0) {
									suggestionsResults.put(resultCount, resultOfNewQuery);
								}
							}

						}
						result.put(RESULT_BESTQUERY_KEY, suggestionsResults);
					} else {
						Query newquery = BooleanQueryRewriter.replaceTerm(rwQuery, term, suggestionsForTerm[0]);
						HashMap<String, Object> resultOfNewQuery = getResultsForQuery(newquery, searcher, sorting, userPermissions, request);
						result.putAll(resultOfNewQuery);
					}
				}
			}
			log.debug("DYM took " + (System.currentTimeMillis() - dymStart) + "ms");
			return result;
		} catch (IOException e) {
			log.error("Cannot access index for didyoumean functionality.", e);
		} finally {
			if (indexAccessor != null && reader != null) {
				indexAccessor.release(reader);
			}
		}
		return null;
	}

	private HashMap<String, Object> getResultsForQuery(final Query query, final IndexSearcher searcher, final String[] sorting,
			final String[] userPermissions, final CRRequest request) {
		HashMap<String, Object> result = new HashMap<String, Object>(3);
		try {
			TopDocsCollector<?> bestcollector = createCollector(request, searcher, 1, sorting, computescores, userPermissions);
			executeSearcher(bestcollector, searcher, query, false, 1, 0);
			result.put(RESULT_BESTQUERY_KEY, query);
			result.put(RESULT_BESTQUERYHITS_KEY, bestcollector.getTotalHits());
			return result;
		} catch (IOException e) {
			log.error("Cannot create collector to get results for query.", e);
		}
		return null;
	}
	
	/**
	 * Get the DYM provider. May be null if none is used.
	 * @return DYM provider.
	 */
	public DidyoumeanIndexExtension getDYMProvider() {
		return didyoumeanprovider;
	}

	@Override
	public void finalize() {
		if (didyoumeanprovider != null) {
			didyoumeanprovider.stop();
		}
		if (config != null) {
			LuceneIndexLocation.stopIndexLocation(config);
		}
	}
}
