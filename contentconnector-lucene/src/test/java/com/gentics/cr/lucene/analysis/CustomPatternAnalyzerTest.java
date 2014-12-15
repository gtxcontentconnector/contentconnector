package com.gentics.cr.lucene.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenStream;
import org.junit.Test;

import com.gentics.cr.configuration.GenericConfiguration;

public class CustomPatternAnalyzerTest extends BaseTokenStreamTestCase{
	
	static {
        //static block gets inherited too
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
    }
	
	@Test
	public void testLowercaseFalse() throws IOException {
		GenericConfiguration config = new GenericConfiguration();
		config.set("pattern", "\\s+");
		config.set("lowercase", "false");
		Analyzer a = new CustomPatternAnalyzer(config);
	
		TokenStream tokenStream = a.tokenStream("test", "this is a Text with Whitespaces");
		assertTokenStreamContents(tokenStream, new String[]{"Text","Whitespaces"});
		
		
	}
	
	@Test
	public void testLowercaseTrue() throws IOException {
		GenericConfiguration config = new GenericConfiguration();
		config.set("pattern", "\\s+");
		config.set("lowercase", "true");
		CustomPatternAnalyzer a = new CustomPatternAnalyzer(config);
		
		TokenStream tokenStream = a.tokenStream("test", "this is a Text with Whitespaces");
		assertTokenStreamContents(tokenStream, new String[]{"text","whitespaces"});
		
		
	}
	
	@Test
	public void testDefaultLowercaseSetting() throws IOException {
		GenericConfiguration config = new GenericConfiguration();
		config.set("pattern", "\\s+");
		CustomPatternAnalyzer a = new CustomPatternAnalyzer(config);
		
		TokenStream tokenStream = a.tokenStream("test", "this is a Text with Whitespaces");
		assertTokenStreamContents(tokenStream, new String[]{"text","whitespaces"});
		
		
	}
	
	@Test
	public void testCustomLowercaseSetting() throws IOException {
		GenericConfiguration config = new GenericConfiguration();
		config.set("pattern", "[;]+");
		CustomPatternAnalyzer a = new CustomPatternAnalyzer(config);
		
		TokenStream stream = a.tokenStream("test", "this is;a Text;with Whitespaces");
		
		assertTokenStreamContents(stream, new String[] {
	    	  "this is","a text","with whitespaces"
	    });
		
		TokenStream tokenStream1 = a.tokenStream("test", "this hugo;a Text;with fafa");
		assertTokenStreamContents(tokenStream1, new String[]{
				"this hugo","a text","with fafa"}
		);
	}
}
