package com.gentics.cr.lucene.search.highlight;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.search.highlight.Fragmenter;

/**
 * 
 * Last changed: $Date: 2009-06-26 15:48:16 +0200 (Fr, 26 Jun 2009) $
 * @version $Revision: 105 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class WordCountFragmenter implements Fragmenter {

	/**
	 * Create new instance of WordCountFragmenter
	 */
	public WordCountFragmenter()
    {
        this(DEFAULT_FRAGMENT_SIZE);
    }

	/**
	 * Create new instance of WordCountFragmenter
	 * @param wordCount
	 */
    public WordCountFragmenter(int wordCount)
    {
        this.wordCount = wordCount;
        currentNumFrags=0;
    }

    /**
     * Start Fragmentation
     * @param originalText 
     * 
     */
    public void start(String originalText)
    {
        currentNumFrags = 0;
    }

    /**
     * Ask Fragmenter if Token is a new Fragment
     * @param token 
     * @return 
     * 
     */
    public boolean isNewFragment(Token token)
    {
        boolean isNewFrag = currentNumFrags >= wordCount;
        currentNumFrags++;
        if(isNewFrag)
            currentNumFrags=0;
        return isNewFrag;
    }

    private static final int DEFAULT_FRAGMENT_SIZE = 10;
    private int currentNumFrags;
    private int wordCount;
	

}
