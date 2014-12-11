package com.gentics.cr.lucene.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenStream;
import org.junit.Test;
/**
 * Test the KeywordListAnalyzer
 * @author christopher
 *
 */
public class KeywordListAnalyzerTest extends BaseTokenStreamTestCase{
	
	static {
        //static block gets inherited too
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
    }
	/**
	 * Test for a single Token / single Line.
	 * @throws IOException
	 */
	@Test
	public void testSingleToken() throws IOException {
		Analyzer a = new KeywordListAnalyzer();
		TokenStream tokenStream = a.tokenStream("test", "this is a Text with Whitespaces");
		assertTokenStreamContents(tokenStream, new String[]{"this is a Text with Whitespaces"});
		tokenStream.close();
	}
	
	/**
	 * Test for multiple tokens / multi line
	 * @throws IOException
	 */
	@Test
	public void testTokenList() throws IOException {
		Analyzer a = new KeywordListAnalyzer();
		TokenStream tokenStream = a.tokenStream("test", "this is a Text\n with Whitespaces");
		assertTokenStreamContents(tokenStream, new String[]{"this is a Text"," with Whitespaces"});
		tokenStream.close();
	}
	
	/**
	 * Test if we can reuse the Analyzer
	 * @throws IOException
	 */
	@Test
	public void testreUseAnalyzerTokenList() throws IOException {
		Analyzer a = new KeywordListAnalyzer();
		TokenStream tokenStream = a.tokenStream("test", "this is a Text\n with Whitespaces");
		assertTokenStreamContents(tokenStream, new String[]{"this is a Text"," with Whitespaces"});
		tokenStream.close();
		
		tokenStream = a.tokenStream("test", "this be a Text\n with Whitespaces");
		assertTokenStreamContents(tokenStream, new String[]{"this be a Text"," with Whitespaces"});
		tokenStream.close();
	}
	
}
