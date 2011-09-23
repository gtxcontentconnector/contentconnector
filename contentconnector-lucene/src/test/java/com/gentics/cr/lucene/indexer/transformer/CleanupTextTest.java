package com.gentics.cr.lucene.indexer.transformer;

import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.exceptions.CRException;

import junit.framework.TestCase;

public class CleanupTextTest extends TestCase {
	
	private static final String CONTENT_ATTRIBUTE = "content";
	
	private static final String UMLAUTS = "öäüÄÜÖß€";
	
	private ContentTransformer transformer;
	@Override
	protected void setUp() throws Exception {
		CRConfigUtil config = new CRConfigUtil();
		config.set("attribute", CONTENT_ATTRIBUTE);
		transformer = new CleanupText(config);
	}
	
	public void testIndexPoints() throws CRException {
		String result = transform("1. Index\n" + 
				"Title ....................................................................................................................... 1\n" +
				"1. First chapter ...................................................................................................................... 2\n" +
				"2. Second chapter .................................................................................................................................... 7");
		assertEquals("Index points are not reduced correctly.", "1. Index Title ... 1 1. First chapter ... 2 2. Second chapter ... 7", result);
		
		result = transform("First chapter . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . Page 33 Second chapter . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . Page 66");
		assertEquals("Index points are not reduced correctly.", "First chapter ... Page 33 Second chapter ... Page 66", result);
	}
	
	public void testUmlauts() throws CRException {
		assertEquals("Umlauts cannot be processed correctly.", UMLAUTS, transform(UMLAUTS + ""));
	}
	
	public void testNotPrintableCharacters() throws CRException {
		String stringWithNonPrintableCharacters = "Drittstaaten:  HYPERLINK \"http://www.help.gv.at/Content.Node/12/Seite.120000.html\" \\o \"Öffnet in neuem Fenster\" \\t \"_blank\" Aufenthaltsberechtigung";
		String expectedResult = "Drittstaaten: HYPERLINK \"http://www.help.gv.at/Content.Node/12/Seite.120000.html\" \\o \"Öffnet in neuem Fenster\" \\t \"_blank\" Aufenthaltsberechtigung";
		assertEquals("Special characters are not elminiated correctly.", expectedResult, transform(stringWithNonPrintableCharacters + ""));
		
		stringWithNonPrintableCharacters = "Person Familienname:  FORMTEXT       Vorname:  FORMTEXT       Standort:  FORMTEXT       Stock:  FORMTEXT ";
		expectedResult = "Person Familienname: FORMTEXT Vorname: FORMTEXT Standort: FORMTEXT Stock: FORMTEXT ";
		assertEquals("Special characters are not elminiated correctly.", expectedResult, transform(stringWithNonPrintableCharacters + ""));
	}
	
	public void testMultipleSpaces() throws CRException {
		final String stringWithMultipleSpaces = "test1  test2   test3    test4";
		final String expectedResult = "test1 test2 test3 test4";
		assertEquals("Multiple Spaces are not replaced correctly.", expectedResult, transform(stringWithMultipleSpaces));
	}
	
	public void testPendingSpace() throws CRException {
		assertEquals("Pending Space was not handled correctly.", "a ", transform("a "));
		assertEquals("Pending Space was not handled correctly.", "a... ", transform("a.... "));
	}
	
	public void testByteArray() throws CRException {
		CRResolvableBean bean = new CRResolvableBean();
		bean.set(CONTENT_ATTRIBUTE, UMLAUTS.getBytes());
		transformer.processBean(bean);
		String result = bean.getString(CONTENT_ATTRIBUTE);
		assertEquals("Cannot handle the byte array correctly.", UMLAUTS, result);
	}
	
	public void testUnchanged() throws CRException {
		String testStringNotToChange = "test";
		assertSame("String had not to be changed.", testStringNotToChange, transform(testStringNotToChange));
		
	}

	private String transform(String string) throws CRException {
		CRResolvableBean bean = new CRResolvableBean();
		bean.set(CONTENT_ATTRIBUTE, string);
		transformer.processBean(bean);
		return bean.getString(CONTENT_ATTRIBUTE);
	}
}
