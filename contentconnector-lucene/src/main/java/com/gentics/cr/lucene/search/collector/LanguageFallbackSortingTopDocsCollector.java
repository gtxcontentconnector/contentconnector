package com.gentics.cr.lucene.search.collector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocsCollector;
import org.apache.lucene.util.BytesRef;

import com.gentics.cr.CRRequest;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.lib.log.NodeLogger;

/**
 * Collector class for language fallback and sorting
 * @author christopher
 *
 */
public class LanguageFallbackSortingTopDocsCollector extends
		TopDocsCollector<LanguageSortingScoreDoc> {

	private static final NodeLogger LOG = NodeLogger.getNodeLogger(LanguageFallbackSortingTopDocsCollector.class);
	private static final String LANGUAGE_PRIORITY_KEY="languagefallbackpriority";
	private static final String LANGUAGE_FIELD_KEY="languagefield";
	private static final String LANGUAGESET_FIELD_KEY ="languagesetfield";
	LanguageSortingScoreDoc pqTop;
	int docBase = 0;
	Scorer scorer;
	private Map<String, LanguageSortingScoreDoc> foundItems;
	
	private ArrayList<String> sortfields;
	boolean doSortvalues = true;
	
	private ArrayList<String> languagepriority;
	
	BinaryDocValues docLanguageSetIds = null;
	BinaryDocValues docLanguages = null;
	
	private String languagefield = "language";
	private String languagesetfield = "languagesetid";
	//sortvalues field cache
	private Map<String, BinaryDocValues> sortValues = null;
	
	
	private HashSet<String> otherLanguages;
	/**
	 * Constructor for the LanguageFallbackSortingTopDocsCollector
	 * @param request
	 * @param sort
	 * @param searcher
	 * @param numHits
	 * @param config
	 * @throws IOException
	 */
	public LanguageFallbackSortingTopDocsCollector(final CRRequest request, final Sort sort, final IndexSearcher searcher, final int numHits, final GenericConfiguration config) throws IOException {
		// NOTE: we need to make the internal priority queue larger by one item, because
		// during collection, we add documents to the priority queue before actually sorting them in
		// therefore, the "top" of the priority queue always is an element, which actually does not get returned
		super(new LanguageSortingHitQueue(numHits + 1, sort, true));
		if (sort != null) {
			SortField[] sortFields = sort.getSort();
			sortfields = new ArrayList<String>(sortFields.length);
			for (SortField field : sortFields) {
				sortfields.add(field.getField());
			}
		} else {
			doSortvalues = false;
		}
		this.foundItems = new HashMap<String, LanguageSortingScoreDoc>();
		// HitQueue implements getSentinelObject to return a ScoreDoc, so we know
		// that at this point top() is already initialized.
		pqTop = pq.top();
		languagepriority = createLanguagePriorityList(request,config);
		otherLanguages = new HashSet<String>();
		this.languagefield = config.getString(LANGUAGE_FIELD_KEY, languagefield);
		this.languagesetfield = config.getString(LANGUAGESET_FIELD_KEY, languagesetfield);
	}
	
	/**
	 * Method to generate the language priority list.
	 * Entries with a lower index have a higher priority.
	 * @param request
	 * @param config
	 * @return
	 */
	private ArrayList<String> createLanguagePriorityList(final CRRequest request, final GenericConfiguration config) {
		ArrayList<String> langPrio = new ArrayList<String>();
                Object o = request.get(LANGUAGE_PRIORITY_KEY, true);
		if (o != null) {
			if (o instanceof String) {
				langPrio.addAll(Arrays.asList(((String)o).split(",")));
			} else if (o instanceof String[]){
				langPrio.addAll(Arrays.asList((String[])o));
			} else {
				LOG.error("Could not read language priority from passed object.");
			}
		} else {
			String langPrioString = config.getString(LANGUAGE_PRIORITY_KEY);
			if (langPrioString != null) {
				langPrio.addAll(Arrays.asList(langPrioString.split(",")));
			}
		}
		return langPrio;
	}
	
	/**
	 * Fallback constructor if sorting is not set.
	 * @param request
	 * @param searcher
	 * @param numHits
	 * @param config
	 * @throws IOException
	 */
	public LanguageFallbackSortingTopDocsCollector(final CRRequest request, final IndexSearcher searcher, final int numHits, final GenericConfiguration config) throws IOException {
		this(request, null, searcher, numHits, config);
	}
	
	/**
	 * Determine if target language is better than source language
	 * by comparing their position in the language priority list.
	 * @param source
	 * @param target
	 * @return
	 */
	private boolean isBetterLanguage(String source, String target) {
		if (source == null && target == null) {
			return false;
		}
		if (source == null && target != null) {
			return true;
		}
		int sIndex = this.languagepriority.indexOf(source);
		int tIndex = this.languagepriority.indexOf(target);
		if (tIndex == -1) {
			return false;
		}
		if (sIndex == -1 && tIndex >= 0) {
			return true;
		}
		return tIndex < sIndex;
	}
	
	/**
	 * Get a set of languages that were also found but did not make it in the
	 * list because we found a better language when doing the langauge fallback.
	 * @return
	 */
	public Set<String> getOtherLanguages() {
		return this.otherLanguages;
	}
	
	@Override
	public void collect(int doc) throws IOException {
		float score = scorer.score();
	
		// This collector cannot handle these scores:
		assert score != Float.NEGATIVE_INFINITY;
		assert !Float.isNaN(score);
		
		//check fallback
		String languageSetIdentifyer = this.docLanguageSetIds.get(doc).utf8ToString();//this.docLanguageSetIds[doc];
		if (languageSetIdentifyer != null) {
			LanguageSortingScoreDoc found = foundItems.get(languageSetIdentifyer);
			if (found != null) {
				//DO FALLBACK and if not better return
				if (isBetterLanguage(found.langCode, this.docLanguages.get(doc).utf8ToString())) {
					this.otherLanguages.add(found.langCode);
					found.langCode = this.docLanguages.get(doc).utf8ToString();
					found.doc = doc + docBase;
					found.sortvalue = constructSortValues(doc);
					found.score = score;
					found.sentinel = false;
				}
				return;
			} else {
				totalHits++;
				if (!doSortvalues && score <= pqTop.score) {
					foundItems.put(languageSetIdentifyer, new LanguageSortingScoreDoc(doc + docBase, score, this.docLanguages.get(doc).utf8ToString(), constructSortValues(doc)));
					return;
				} else {
					pqTop.doc = doc + docBase;
					pqTop.langCode = this.docLanguages.get(doc).utf8ToString();
					pqTop.score = score;
					pqTop.sortvalue = constructSortValues(doc);
					pqTop.sentinel = false;
					foundItems.put(languageSetIdentifyer, pqTop);
					pqTop = pq.updateTop();
				}
			}
		} else {
			//WE DID NOT FIND LANGUAGE INFORMATION => CANNOT DO FALLBACK
			totalHits++;
			if (!doSortvalues && score <= pqTop.score) {
			  // Since docs are returned in-order (i.e., increasing doc Id), a document
			  // with equal score to pqTop.score cannot compete since HitQueue favors
			  // documents with lower doc Ids. Therefore reject those docs too.
			  return;
			}
			pqTop.doc = doc + docBase;
			pqTop.score = score;
			pqTop.sentinel = false;
			pqTop.sortvalue = constructSortValues(doc);
			pqTop = pq.updateTop();
		}
	}
	
	
	
	/**
	 * Constructs the sortvalues from the doc id and the field cache
	 * @param doc
	 * @return
	 */
	private HashMap<String, BytesRef> constructSortValues(int doc) {
		if (!doSortvalues) {
			return null;
		}
		HashMap<String, BytesRef> sV = new HashMap<String,BytesRef>();
		for (String field:this.sortfields) {
			BinaryDocValues fieldcache = this.sortValues.get(field);
			sV.put(field.intern(), BytesRef.deepCopyOf(fieldcache.get(doc)));
		}
		return sV;
	}

	@Override
	public void setScorer(Scorer scorer) throws IOException {
		this.scorer = scorer;
	}

	@Override
	public boolean acceptsDocsOutOfOrder() {
		return false;
	}

	@Override
	public void setNextReader(AtomicReaderContext arg0) throws IOException {
		AtomicReader reader = arg0.reader();
		this.docLanguageSetIds = FieldCache.DEFAULT.getTerms(reader, languagesetfield, false);
		this.docLanguages = FieldCache.DEFAULT.getTerms(reader, languagefield, false);
		if (doSortvalues) {
			this.sortValues = new HashMap<String, BinaryDocValues>();
			for (String field : sortfields) {
				sortValues.put(field, FieldCache.DEFAULT.getTerms(reader, field, false));
			}
		}
		this.docBase = arg0.docBase;
	}

}
