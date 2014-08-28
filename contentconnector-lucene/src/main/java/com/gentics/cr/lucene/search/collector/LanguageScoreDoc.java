package com.gentics.cr.lucene.search.collector;

import org.apache.lucene.search.ScoreDoc;

public class LanguageScoreDoc extends ScoreDoc {

	public String langCode;
	
	public LanguageScoreDoc(int doc, float score, String language) {
		super(doc, score);
		this.langCode = language;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 8426726101987179013L;

}
