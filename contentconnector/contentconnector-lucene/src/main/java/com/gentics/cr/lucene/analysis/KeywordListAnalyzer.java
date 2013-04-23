package com.gentics.cr.lucene.analysis;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;

/**
 * "Tokenizes" the entire stream as a single token. This is useful
 * for data like zip codes, ids, and some product names.
 */
public class KeywordListAnalyzer extends Analyzer {

	@Override
	public final TokenStream tokenStream(final String fieldName, final Reader reader) {
		return new KeywordListTokenizer(reader);
	}

	@Override
	public final TokenStream reusableTokenStream(final String fieldName, final Reader reader) throws IOException {

		Tokenizer tokenizer = (Tokenizer) getPreviousTokenStream();
		if (tokenizer == null) {
			tokenizer = new KeywordListTokenizer(reader);
			setPreviousTokenStream(tokenizer);
		} else {
			tokenizer.reset(reader);
		}
		return tokenizer;
	}
}
