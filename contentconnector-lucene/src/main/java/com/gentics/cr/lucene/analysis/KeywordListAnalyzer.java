package com.gentics.cr.lucene.analysis;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;

/**
 * "Tokenizes" the entire stream as a single token. This is useful
 * for data like zip codes, ids, and some product names.
 */
public class KeywordListAnalyzer extends Analyzer {

	@Override
	protected TokenStreamComponents createComponents(String field, Reader reader) {
		return new TokenStreamComponents(new KeywordListTokenizer(reader));
	}
}
