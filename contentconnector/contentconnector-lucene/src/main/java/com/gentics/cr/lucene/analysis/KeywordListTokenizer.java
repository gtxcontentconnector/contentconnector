package com.gentics.cr.lucene.analysis;

import java.io.Reader;

import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.util.AttributeSource;

/**
 * Emits the entire input as a single token.
 */
public final class KeywordListTokenizer extends CharTokenizer {

	/**
	 * Constructor.
	 * @param input input
	 */
	public KeywordListTokenizer(final Reader input) {
		super(input);
	}
	
	/**
	 * Constructor.
	 * @param source source
	 * @param input input
	 */
	public KeywordListTokenizer(final AttributeSource source,
				final Reader input) {
		super(source, input);
	}

	/**
	 * Constructor.
	 * @param factory factory.
	 * @param input input
	 */
	public KeywordListTokenizer(final AttributeFactory factory,
				final Reader input) {
		super(factory, input);
	}


	@Override
	protected boolean isTokenChar(final char c) {
		return !isSeparator(c);
	}

	/**
	 * isSeperator.
	 * @param c character
	 * @return true if seperator.
	 */
	private boolean isSeparator(final char c) {
		return '\n' == c;
	}
}
