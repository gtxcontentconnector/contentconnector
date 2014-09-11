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
	
	/**
	 * Alias for constructor.
	 * @param doc
	 * @param score
	 * @param language
	 * @param sortvalue
	 */
	public LanguageSortingScoreDoc(int doc, float score, String language, HashMap<String, BytesRef> sortvalue) {
		this(doc,score,language,sortvalue,false);
	}
	
	/**
	 * Constructor.
	 * @param doc
	 * @param score
	 * @param language
	 * @param sortvalue
	 * @param sentinel
	 */
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
