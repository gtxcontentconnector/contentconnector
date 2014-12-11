package com.gentics.cr.lucene.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenStream;
import org.junit.Test;

public class BasicUmlautAnalyzerTest extends BaseTokenStreamTestCase{
	
	static {
        //static block gets inherited too
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
    }
	
	
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
	public void testSpecial4() throws IOException {
		testToken("alles gute", new String[]{"alles", "gute"});
	}
	
	@Test
	public void testSpecial5() throws IOException {
		testToken("unternehmenspräsentation", new String[]{"unternehmenspraesentation"});
	}
	
	@Test
	public void testSpecial6() throws IOException {
		testToken("asdf loesadfjloesadjfloesadjfloeksadjkfsoödjföls_ asdf", new String[]{"asdf","loesadfjloesadjfloesadjfloeksadjkfsooedjfoels_","asdf"});
	}
	
	private void testToken(String in, String[] expected) throws IOException {
		Analyzer a = new BasicUmlautAnalyzer();
		
		TokenStream tokenStream = a.tokenStream("test", in);
		assertTokenStreamContents(tokenStream, expected);
		tokenStream.end();
		tokenStream.close();
		a.close();
	}
	
	@Test
	public void testReuse() throws IOException {
		Analyzer a = new BasicUmlautAnalyzer();
		
		TokenStream tokenStream = a.tokenStream("test", "ößäü");
		assertTokenStreamContents(tokenStream, new String[]{"oeszaeue"});
		tokenStream.end();
		tokenStream.close();
		
		TokenStream tokenStream1 = a.tokenStream("test", "testing äöa");
		assertTokenStreamContents(tokenStream1, new String[]{"testing", "aeoea"});
		tokenStream1.end();
		tokenStream1.close();
		
		a.close();
	}
	
	
}
