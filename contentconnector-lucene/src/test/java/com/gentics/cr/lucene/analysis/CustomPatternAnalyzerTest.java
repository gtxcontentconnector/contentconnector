package com.gentics.cr.lucene.analysis;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Test;

import com.gentics.cr.configuration.GenericConfiguration;

public class CustomPatternAnalyzerTest {
	
	@Test
	public void testLowercaseFalse() throws IOException {
		GenericConfiguration config = new GenericConfiguration();
		config.set("pattern", "\\s+");
		config.set("lowercase", "false");
		Analyzer a = new CustomPatternAnalyzer(config);
	
		TokenStream tokenStream = a.tokenStream("test", "this is a Text with Whitespaces");
		BasicAnalyzerTestHelper.assertTokenStreamContents(tokenStream, new String[]{"Text","Whitespaces"});
		
		
	}
	
	@Test
	public void testLowercaseTrue() throws IOException {
		GenericConfiguration config = new GenericConfiguration();
		config.set("pattern", "\\s+");
		config.set("lowercase", "true");
		CustomPatternAnalyzer a = new CustomPatternAnalyzer(config);
		
		TokenStream tokenStream = a.tokenStream("test", "this is a Text with Whitespaces");
		BasicAnalyzerTestHelper.assertTokenStreamContents(tokenStream, new String[]{"text","whitespaces"});
		
		
	}
	
	@Test
	public void testDefaultLowercaseSetting() throws IOException {
		GenericConfiguration config = new GenericConfiguration();
		config.set("pattern", "\\s+");
		CustomPatternAnalyzer a = new CustomPatternAnalyzer(config);
		
		TokenStream tokenStream = a.tokenStream("test", "this is a Text with Whitespaces");
		BasicAnalyzerTestHelper.assertTokenStreamContents(tokenStream, new String[]{"text","whitespaces"});
		
		
	}
}
