package com.gentics.cr.lucene.analysis;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.reverse.ReverseStringFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

import com.gentics.cr.lucene.LuceneVersion;

public class ReverseAnalyzer extends Analyzer {

	@Override
	protected TokenStreamComponents createComponents(String field, Reader reader) {
		Tokenizer tokenizer = new StandardTokenizer(LuceneVersion.getVersion(),reader);
		TokenFilter filter = new ReverseStringFilter(LuceneVersion.getVersion(),tokenizer);
		
		return new TokenStreamComponents(tokenizer, filter);
	}

}
