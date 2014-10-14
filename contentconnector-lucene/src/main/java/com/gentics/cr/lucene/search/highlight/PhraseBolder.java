package com.gentics.cr.lucene.search.highlight;

import org.apache.lucene.search.highlight.Fragmenter;

import com.gentics.cr.configuration.GenericConfiguration;

/**
 * PhraseBolder.
 * Last changed: $Date: 2009-06-26 15:48:16 +0200 (Fr, 26 Jun 2009) $
 * @version $Revision: 105 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class PhraseBolder extends BasePhraseBolder {
	
	/**
	 * Default fragment size.
	 */
	private static final int DEFAULT_FRAGMENT_SIZE = 100;
	
	/**
	 * Create new Instance of PhraseBolder.
	 * @param config configuration
	 */
	public PhraseBolder(final GenericConfiguration config) {
		super(config);
	}

	@Override
	public Fragmenter getFragmenter() {
		return new WordCountFragmenter(getFragmentSize());
	}

	@Override
	public boolean isMergeAdjacentFragments() {
		return true;
	}

	@Override
	protected int getDefaultFragmentSize() {
		return DEFAULT_FRAGMENT_SIZE;
	}

}
