package com.gentics.cr.lucene.analysis;

import java.io.Reader;

import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.util.AttributeFactory;
import org.apache.lucene.util.AttributeSource;

import com.gentics.cr.lucene.LuceneVersion;

/**
 * Tokenizes only by new lines
 */
public final class KeywordListTokenizer extends CharTokenizer {

	/**
	 * Constructor.
	 * @param input input
	 */
	public KeywordListTokenizer(final Reader input) {
		super(LuceneVersion.getVersion(), input);
	}

	/**
	 * Constructor.
	 * @param source source
	 * @param input input
	 */
	public KeywordListTokenizer(final AttributeSource source, final Reader input) {
		super(LuceneVersion.getVersion(),input);
	}

	/**
	 * Constructor.
	 * @param factory factory.
	 * @param input input
	 */
	public KeywordListTokenizer(final AttributeFactory factory, final Reader input) {
		super(LuceneVersion.getVersion(),factory, input);
	}


	@Override
	protected boolean isTokenChar(int character) {
		return character != '\n';
	}
}
