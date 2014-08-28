package com.gentics.cr.lucene.search.collector;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TopDocsCollector;

import com.gentics.cr.configuration.GenericConfiguration;


public class LanguageFallbackTopDocsCollector extends
		TopDocsCollector<LanguageScoreDoc> {

	LanguageScoreDoc pqTop;
	int docBase = 0;
	Scorer scorer;
	private Map<String, LanguageScoreDoc> foundItems;
	
	String docLanguageSetIds[] = null;
	String docLanguages[] = null;
	
	public LanguageFallbackTopDocsCollector(final IndexSearcher searcher, final int numHits, final GenericConfiguration config) throws IOException {
		 super(new LanguageHitQueue(numHits, true));
		 this.foundItems = new HashMap<String, LanguageScoreDoc>();
	    // HitQueue implements getSentinelObject to return a ScoreDoc, so we know
	    // that at this point top() is already initialized.
	    pqTop = pq.top();
	    
	}
	
	private boolean isBetterLanguage(String source, String target) {
		if (source == null && target == null) {
			return false;
		}
		if (source == null && target != null) {
			return true;
		}
		return target.compareTo(source) < 0;
	}
	
	@Override
	public void collect(int doc) throws IOException {
		float score = scorer.score();
	
		// This collector cannot handle these scores:
		assert score != Float.NEGATIVE_INFINITY;
		assert !Float.isNaN(score);
		
		//check fallback
		String languageSetIdentifyer = this.docLanguageSetIds[doc];
		if (languageSetIdentifyer != null) {
			LanguageScoreDoc found = foundItems.get(languageSetIdentifyer);
			if (found != null) {
				//DO FALLBACK and if not better return
				if (isBetterLanguage(found.langCode, this.docLanguages[doc])) {
					found.langCode = this.docLanguages[doc];
					found.doc = doc + docBase;
					found.score = score;
				}
				return;
			} else {
				totalHits++;
				if (score <= pqTop.score) {
					foundItems.put(languageSetIdentifyer, new LanguageScoreDoc(doc + docBase, score, this.docLanguages[doc]));
					return;
				} else {
					pqTop.doc = doc + docBase;
					pqTop.langCode = this.docLanguages[doc];
					pqTop.score = score;
					foundItems.put(languageSetIdentifyer, pqTop);
					pqTop = pq.updateTop();
				}
			}
		} else {
			//WE DID NOT FIND LANGUAGE INFORMATION => CANNOT DO FALLBACK
			totalHits++;
			if (score <= pqTop.score) {
			  // Since docs are returned in-order (i.e., increasing doc Id), a document
			  // with equal score to pqTop.score cannot compete since HitQueue favors
			  // documents with lower doc Ids. Therefore reject those docs too.
			  return;
			}
			pqTop.doc = doc + docBase;
			pqTop.score = score;
			pqTop = pq.updateTop();
		}
	}

	@Override
	public void setScorer(Scorer scorer) throws IOException {
		this.scorer = scorer;
	}

	@Override
	public void setNextReader(IndexReader reader, int base)
			throws IOException {
		this.docLanguageSetIds = FieldCache.DEFAULT.getStrings(reader, "languagesetit");
		this.docLanguages = FieldCache.DEFAULT.getStrings(reader, "languagecode");
		this.docBase = base;
	}

	@Override
	public boolean acceptsDocsOutOfOrder() {
		return false;
	}

}
