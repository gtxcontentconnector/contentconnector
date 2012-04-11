package com.gentics.cr.lucene.search.highlight;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.search.highlight.Fragmenter;

/**
 * WordCountFragmenter.
 * Last changed: $Date: 2009-06-26 15:48:16 +0200 (Fr, 26 Jun 2009) $
 * @version $Revision: 105 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class WordCountFragmenter implements Fragmenter {

	/**
	 * Create new instance of WordCountFragmenter.
	 */
	public WordCountFragmenter() {
		this(DEFAULT_FRAGMENT_SIZE);
	}

	/**
	 * Create new instance of WordCountFragmenter.
	 * @param wc word count
	 */
	public WordCountFragmenter(final int wc) {
		this.wordCount = wc;
		currentNumFrags = 0;
	}

	/**
	 * Start Fragmentation.
	 * @param originalText 
	 * 
	 */
	public final void start(final String originalText) {
		currentNumFrags = 0;
	}

	/**
	 * Ask Fragmenter if Token is a new Fragment.
	 * @param token 
	 * @return true if its a new fragment
	 * 
	 */
	public final boolean isNewFragment(final Token token) {
		boolean isNewFrag = currentNumFrags >= wordCount;
		currentNumFrags++;
		if (isNewFrag) {
			currentNumFrags = 0;
		}
		return isNewFrag;
	}

	/**
	 * Default fragment size.
	 */
	private static final int DEFAULT_FRAGMENT_SIZE = 10;
	/**
	 * current num frags.
	 */
	private int currentNumFrags;
	/**
	 * word count.
	 */
	private int wordCount;

	/**
	 * Lucene 3.0 Implementation.
	 * @return true if its a new fragment
	 */
	public final boolean isNewFragment() {
		boolean isNewFrag = currentNumFrags >= wordCount;
		currentNumFrags++;
		if (isNewFrag) {
			currentNumFrags = 0;
		}
		return isNewFrag;
	}

	/**
	 * Lucene 3.0 Implementation.
	 * @param arg0 arg0
	 * @param arg1 arg1
	 */
	public void start(final String arg0, final TokenStream arg1) {

	}

}
