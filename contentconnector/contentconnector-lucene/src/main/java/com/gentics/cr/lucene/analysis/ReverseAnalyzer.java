package com.gentics.cr.lucene.analysis;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.reverse.ReverseStringFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

import com.gentics.cr.lucene.LuceneVersion;

public class ReverseAnalyzer extends Analyzer {

	Analyzer subanalyzer = null;

	public ReverseAnalyzer(Analyzer subanalyzer) {
		this.subanalyzer = subanalyzer;
	}

	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		TokenStream ts = null;
		if (subanalyzer != null)
			ts = subanalyzer.tokenStream(fieldName, reader);
		else
			ts = new StandardTokenizer(LuceneVersion.getVersion(), reader);

		ts = new ReverseStringFilter(ts);

		return ts;
	}

}
