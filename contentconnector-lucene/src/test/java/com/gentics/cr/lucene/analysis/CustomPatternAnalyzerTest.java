package com.gentics.cr.lucene.analysis;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenStream;
import org.junit.AfterClass;
import org.junit.Test;

import com.gentics.cr.configuration.GenericConfiguration;

public class CustomPatternAnalyzerTest extends BaseTokenStreamTestCase{
	
	static {
        //static block gets inherited too
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
    }

	@AfterClass
	public static void stopLog4j2() {
		LogManager.shutdown();
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
	
	@Test
	public void testHashtagDelimiterSetting() throws IOException {
		GenericConfiguration config = new GenericConfiguration();
		config.set("pattern", "[#]+");
		config.set("lowercase", "false");
		CustomPatternAnalyzer a = new CustomPatternAnalyzer(config);
		
		TokenStream tokenStream = a.tokenStream("test", "This test-text,#has a; custom#delimiter|seperation.");
		assertTokenStreamContents(tokenStream, new String[]{
				"This test-text,","has a; custom","delimiter|seperation."}
		);
	}
	
	@Test
	public void testNoStopwordsSetting() throws IOException {
		GenericConfiguration config = new GenericConfiguration();
		config.set("pattern", "[#]+");
		config.set("lowercase", "false");
		config.set("stopwords", "false");
		CustomPatternAnalyzer a = new CustomPatternAnalyzer(config);
		
		TokenStream tokenStream = a.tokenStream("test", "all#stopwords#above#about#are still here.");
		assertTokenStreamContents(tokenStream, new String[]{
				"all","stopwords","above","about","are still here."}
		);
	}
}
