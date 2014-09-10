package com.gentics.cr.lucene.search.collector;

import java.util.HashMap;

import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.util.BytesRef;

/**
 * Documentcontainer for sorting and language fallback.
 * @author christopher
 *
 */
public class LanguageSortingScoreDoc extends ScoreDoc {

	public String langCode;
	public boolean sentinel;
	public HashMap<String, BytesRef> sortvalue;
	
	public LanguageSortingScoreDoc(int doc, float score, String language, HashMap<String, BytesRef> sortvalue) {
		super(doc, score);
		this.langCode = language;
		this.sentinel = false;
	}
	
	public LanguageSortingScoreDoc(int doc, float score, String language, HashMap<String, BytesRef> sortvalue, boolean sentinel) {
		super(doc, score);
		this.langCode = language;
		this.sentinel = sentinel;
		this.sortvalue = sortvalue;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 8426726101987179013L;

}
