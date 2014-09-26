package com.gentics.cr.lucene.search.highlight;

import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.SimpleFragmenter;

import com.gentics.cr.configuration.GenericConfiguration;
/**
 * Phrase bolder that is limited by character count
 * @author christopher
 *
 */
public class CharacterCountPhraseBolder extends BasePhraseBolder {
	/**
	 * Default fragment size.
	 */
	private static final int DEFAULT_FRAGMENT_SIZE = 800;
	
	/**
	 * Constructor.
	 * @param config
	 */
	public CharacterCountPhraseBolder(GenericConfiguration config) {
		super(config);
	}

	@Override
	public Fragmenter getFragmenter() {
		return new SimpleFragmenter(getFragmentSize());
	}

	@Override
	public boolean isMergeAdjacentFragments() {
		return false;
	}

	@Override
	protected int getDefaultFragmentSize() {
		return DEFAULT_FRAGMENT_SIZE;
	}

}
