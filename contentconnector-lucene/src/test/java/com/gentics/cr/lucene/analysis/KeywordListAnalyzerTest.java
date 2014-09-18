package com.gentics.cr.lucene.analysis;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gentics.cr.configuration.GenericConfiguration;

public class KeywordListAnalyzerTest {
	
	
	
	@Test
	public void testSingleToken() throws IOException {
		Analyzer a = new KeywordListAnalyzer();
		TokenStream tokenStream = a.tokenStream("test", "this is a Text with Whitespaces");
		BasicAnalyzerTest.assertTokenStreamContents(tokenStream, new String[]{"this is a Text with Whitespaces"});
		tokenStream.close();
	}
	
	@Test
	public void testTokenList() throws IOException {
		Analyzer a = new KeywordListAnalyzer();
		TokenStream tokenStream = a.tokenStream("test", "this is a Text\n with Whitespaces");
		BasicAnalyzerTest.assertTokenStreamContents(tokenStream, new String[]{"this is a Text"," with Whitespaces"});
		tokenStream.close();
	}
	
	@Test
	public void testreUseAnalyzerTokenList() throws IOException {
		Analyzer a = new KeywordListAnalyzer();
		TokenStream tokenStream = a.tokenStream("test", "this is a Text\n with Whitespaces");
		BasicAnalyzerTest.assertTokenStreamContents(tokenStream, new String[]{"this is a Text"," with Whitespaces"});
		tokenStream.close();
		tokenStream = a.tokenStream("test", "this be a Text\n with Whitespaces");
		BasicAnalyzerTest.assertTokenStreamContents(tokenStream, new String[]{"this be a Text"," with Whitespaces"});
		tokenStream.close();
	}
	
}
