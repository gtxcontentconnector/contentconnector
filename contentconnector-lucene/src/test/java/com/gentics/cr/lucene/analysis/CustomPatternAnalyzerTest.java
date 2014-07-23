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
		CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);

		tokenStream.incrementToken();
		String t2 = charTermAttribute.toString();
		tokenStream.incrementToken();
		String t3 = charTermAttribute.toString();
		
		assertEquals("Second Token did not match!", "Text", t2);
		assertEquals("Third Token did not match!", "Whitespaces", t3);
		
	}
	
	@Test
	public void testLowercaseTrue() throws IOException {
		GenericConfiguration config = new GenericConfiguration();
		config.set("pattern", "\\s+");
		config.set("lowercase", "true");
		CustomPatternAnalyzer a = new CustomPatternAnalyzer(config);
		
		TokenStream tokenStream = a.tokenStream("test", "this is a Text with Whitespaces");
		CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);

		tokenStream.incrementToken();
		String t2 = charTermAttribute.toString();
		tokenStream.incrementToken();
		String t3 = charTermAttribute.toString();
		
		assertEquals("Second Token did not match!", "text", t2);
		assertEquals("Third Token did not match!", "whitespaces", t3);
		
	}
	
	@Test
	public void testDefaultLowercaseSetting() throws IOException {
		GenericConfiguration config = new GenericConfiguration();
		config.set("pattern", "\\s+");
		CustomPatternAnalyzer a = new CustomPatternAnalyzer(config);
		
		TokenStream tokenStream = a.tokenStream("test", "this is a Text with Whitespaces");
		CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);

		tokenStream.incrementToken();
		String t2 = charTermAttribute.toString();
		tokenStream.incrementToken();
		String t3 = charTermAttribute.toString();
		
		assertEquals("Second Token did not match!", "text", t2);
		assertEquals("Third Token did not match!", "whitespaces", t3);
		
	}
}
