package com.gentics.cr.lucene.analysis;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.util.AttributeFactory;
import org.apache.lucene.util.AttributeSource;

/**
 * Emits the entire input as a single token.
 */
public final class KeywordListTokenizer extends Tokenizer {

	private Reader reader;
	/**
	 * Constructor.
	 * @param input input
	 */
	public KeywordListTokenizer(final Reader input) {
		super(input);
		this.reader = input;
	}

	/**
	 * Constructor.
	 * @param source source
	 * @param input input
	 */
	public KeywordListTokenizer(final AttributeSource source, final Reader input) {
		super(input);
	}

	/**
	 * Constructor.
	 * @param factory factory.
	 * @param input input
	 */
	public KeywordListTokenizer(final AttributeFactory factory, final Reader input) {
		super(factory, input);
	}


	@Override
	public boolean incrementToken() throws IOException {
		return this.reader.read() == '\n';
	}
}
