package com.gentics.cr.lucene.search.collector;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.BinaryDocValues;
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
	
	BinaryDocValues docLanguageSetIds = null;
	BinaryDocValues docLanguages = null;
	
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
		String languageSetIdentifyer = this.docLanguageSetIds.get(doc).utf8ToString();//this.docLanguageSetIds[doc];
		if (languageSetIdentifyer != null) {
			LanguageScoreDoc found = foundItems.get(languageSetIdentifyer);
			if (found != null) {
				//DO FALLBACK and if not better return
				if (isBetterLanguage(found.langCode, this.docLanguages.get(doc).utf8ToString())) {
					found.langCode = this.docLanguages.get(doc).utf8ToString();
					found.doc = doc + docBase;
					found.score = score;
				}
				return;
			} else {
				totalHits++;
				if (score <= pqTop.score) {
					foundItems.put(languageSetIdentifyer, new LanguageScoreDoc(doc + docBase, score, this.docLanguages.get(doc).utf8ToString()));
					return;
				} else {
					pqTop.doc = doc + docBase;
					pqTop.langCode = this.docLanguages.get(doc).utf8ToString();
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
	public boolean acceptsDocsOutOfOrder() {
		return false;
	}

	@Override
	public void setNextReader(AtomicReaderContext arg0) throws IOException {
		AtomicReader reader = arg0.reader();
		this.docLanguageSetIds = FieldCache.DEFAULT.getTerms(reader, "languagesetit", false);
		this.docLanguages = FieldCache.DEFAULT.getTerms(reader, "languagecode", false);
		this.docBase = arg0.docBase;
	}

}
