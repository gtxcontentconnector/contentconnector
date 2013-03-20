package com.gentics.cr.lucene.indexer.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.util.CRUtil;

public class CleanupTextTransformerTest {

	CRConfigUtil config = null;

	/**
	 * Log4j logger for debug and error messages.
	 */
	private static Logger logger = Logger.getLogger(CleanupTextTransformerTest.class);

	private static final String CONTENT_ATTRIBUTE = "content";

	/**
	 * öäüÄÜÖß€
	 */
	private static final String UMLAUTS = "\u00F6\u00E4\u00FC\u00C4\u00DC\u00D6\u00DF\u20AC";

	@Before
	public void setUp() throws Exception {
		URL confPath = new File(this.getClass().getResource("/config/nodelog.properties").toURI()).getParentFile().toURI().toURL();
		System.setProperty(CRUtil.PORTALNODE_CONFPATH, confPath.getPath());
		config = new CRConfigUtil();
		config.set("attribute", CONTENT_ATTRIBUTE);
	}

	@Test
	public void testStripWhitespace() throws IOException, CRException, URISyntaxException {
		String testContent = readFile("whitespacefile.txt");

		testContent = transform(testContent);
		assertEquals(readFile("cleanedwhitespacefile.txt"), testContent);
	}

	@Test
	public void testNotPrintableCharacters() throws CRException {
		String stringWithNonPrintableCharacters = "Drittstaaten:  HYPERLINK \"http://www.help.gv.at/Content.Node/12/Seite.120000.html\" \\o \"Öffnet in neuem Fenster\" \\t \"_blank\" Aufenthaltsberechtigung";
		String expectedResult = "Drittstaaten: HYPERLINK \"http://www.help.gv.at/Content.Node/12/Seite.120000.html\" \\o \"Öffnet in neuem Fenster\" \\t \"_blank\" Aufenthaltsberechtigung";
		assertEquals("Special characters are not elminiated correctly.", expectedResult, transform(stringWithNonPrintableCharacters));
	}

	@Test
	public void testNotPrintableCharactersWithNewLines() throws CRException {
		String stringWithNonPrintableCharacters = "Person Familienname:  FORMTEXT   \n    Vorname:  FORMTEXT   \t    Standort:  FORMTEXT       Stock:  FORMTEXT ";
		String expectedResult = "Person Familienname: FORMTEXT\nVorname: FORMTEXT Standort: FORMTEXT Stock: FORMTEXT ";
		assertEquals("Special characters are not elminiated correctly.", expectedResult, transform(stringWithNonPrintableCharacters));
	}

	@Test
	public void testMultipleSpaces() throws CRException {
		final String stringWithMultipleSpaces = "test1  test2   test3    test4";
		final String expectedResult = "test1 test2 test3 test4";
		assertEquals("Multiple Spaces are not replaced correctly.", expectedResult, transform(stringWithMultipleSpaces));
	}

	@Test
	public void testPendingSpace() throws CRException {
		assertEquals("Pending Space was not handled correctly.", "a ", transform("a "));
		assertEquals("Pending Space was not handled correctly.", "a... ", transform("a.... "));
	}

	@Test
	public void testByteArray() throws CRException, UnsupportedEncodingException {
		CRResolvableBean bean = new CRResolvableBean();
		bean.set(CONTENT_ATTRIBUTE, UMLAUTS.getBytes("UTF-8"));
		ContentTransformer transformer = new CleanupTextTransformer(config);
		transformer.processBean(bean);
		String result = bean.getString(CONTENT_ATTRIBUTE);
		assertEquals("Cannot handle the byte array correctly.", UMLAUTS, result);
	}

	@Test
	public void testUmlauts() throws CRException {
		assertEquals("Umlauts cannot be processed correctly.", UMLAUTS, transform(UMLAUTS + ""));
	}

	@Test
	public void testIndexPoints() throws CRException {
		String result = transform("1. Index\n"
				+ "Title ....................................................................................................................... 1\n"
				+ "1. First chapter ...................................................................................................................... 2\n"
				+ "2. Second chapter .................................................................................................................................... 7");
		assertEquals(
			"Index points are not reduced correctly.",
			"1. Index\nTitle ... 1\n1. First chapter ... 2\n2. Second chapter ... 7",
			result);
	}

	@Test
	public void testIndexPoints2() throws CRException {
		String result = transform("First chapter . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . "
				+ "Page 33 Second chapter . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . Page 66");
		assertEquals(
			"Index points with spaces between are not reduced correctly.",
			"First chapter ... Page 33 Second chapter ... Page 66",
			result);
	}

	@Test
	public void testUnchanged() throws CRException {
		String testStringNotToChange = "test";
		assertSame("String had not to be changed.", testStringNotToChange, transform(testStringNotToChange));
	}

	private String readFile(final String fileName) throws URISyntaxException, FileNotFoundException, IOException {
		FileInputStream inputStream = new FileInputStream(new File(this.getClass().getResource(fileName).toURI()));
		try {
			return IOUtils.toString(inputStream);
		} finally {
			inputStream.close();
		}
	}

	private String transform(String string) throws CRException {
		CRResolvableBean bean = new CRResolvableBean();
		bean.set(CONTENT_ATTRIBUTE, string);
		ContentTransformer transformer = new CleanupTextTransformer(config);
		transformer.processBean(bean);
		return bean.getString(CONTENT_ATTRIBUTE);
	}
}
