package com.gentics.cr.lucene.analysis;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Test;

public class BasicUmlautAnalyzerTest {
	
	@Test
	public void testSimpleSentence() throws IOException {
		testToken("Große Nüsse nässen möglich", new String[]{"grosze","nuesse","naessen","moeglich"});
	}
	
	@Test
	public void testSZ() throws IOException {
		testToken("große ß", new String[]{"grosze","sz"});
	}
	
	@Test
	public void testAE() throws IOException {
		testToken("nässen Ä", new String[]{"naessen","ae"});
	}
	
	@Test
	public void testUE() throws IOException {
		testToken("nüsse Ü", new String[]{"nuesse","ue"});
	}
	
	@Test
	public void testOE() throws IOException {
		testToken("nötig Ö", new String[]{"noetig","oe"});
	}
	
	@Test
	public void testSpecial() throws IOException {
		testToken("öööö", new String[]{"oeoeoeoe"});
	}
	
	
	@Test
	public void testSpecial2() throws IOException {
		testToken("ößäü", new String[]{"oeszaeue"});
	}
	
	@Test
	public void testSpecial3() throws IOException {
		testToken("", new String[]{""});
	}
	
	@Test
	public void testSpecial4() throws IOException {
		testToken("alles gute", new String[]{"alles", "gute"});
	}
	
	private void testToken(String in, String[] expected) throws IOException {
		Analyzer a = new BasicUmlautAnalyzer();
		
		TokenStream tokenStream = a.tokenStream("test", in);
		CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
		tokenStream.reset();
		for (String ex : expected) {
			tokenStream.incrementToken();
			String t1 = charTermAttribute.toString();
			assertEquals("Token did not match!", ex, t1);
		}
		tokenStream.end();
		tokenStream.close();
		a.close();
	}
	
}
