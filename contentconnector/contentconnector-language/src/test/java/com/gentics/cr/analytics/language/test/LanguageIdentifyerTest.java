package com.gentics.cr.analytics.language.test;

import java.io.File;
import java.util.Scanner;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gentics.DefaultTestConfiguration;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.EnvironmentConfiguration;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;
import com.gentics.cr.lucene.indexer.transformer.lang.LanguageIdentifyer;

public class LanguageIdentifyerTest {

	CRResolvableBean bean;
	GenericConfiguration config;
	ContentTransformer t;

	@Before
	public void init() throws Exception {
		EnvironmentConfiguration.setConfigPath(new File(DefaultTestConfiguration.class.getResource("conf/gentics").getFile())
				.getAbsolutePath());
		EnvironmentConfiguration.loadEnvironmentProperties();
		bean = new CRResolvableBean();

		config = new GenericConfiguration();
		config.set("attribute", "content");
		config.set("langattribute", "lang");
		t = new LanguageIdentifyer(config);
	}

	@Test
	public void testTransformerDE() throws Exception {
		testString("de", "Dies ist ein Test um den LanguageDetector auf eine Sprache zu bringen");
		testString(
			"de",
			"Verwaltungsgerichtshof Zl. 2011/06/0125-7 I M N A M E N D E R R E P U B L I K ! Der Verwaltungsgerichtshof hat durch den Vorsitzenden Senatspräsident Dr. Kail und die Senatspräsidentin Dr. Bernegger sowie die Hofräte Dr. Waldstätten, Dr. Bayjones und Dr. Moritz als Richter, im Beisein des Schriftführers Mag. Zöchling, über die Beschwerde 1. des B F und 2. der G F, beide in G, beide vertreten durch Mag. Wolfgang Jantscher, Rechtsanwalt in 8010 Graz, Wastiangasse 1, gegen den Bescheid der Berufungskommission der Landeshauptstadt Graz vom 14. Juni 2011, Zl. 005729/2010-37, betreffend Einwendungen gegen ein Bauvorhaben (weitere Partei: Steiermärkische Landesregierung; mitbeteiligte Partei: R in G), zu Recht erkannt:");
	}

	@Test
	public void testTransformerEN() throws Exception {
		testString("en", "This is some text to set the LanguageDetector to a language.");
	}

	@Test
	public void testFiles() throws Exception {
		/*
		 * @formatter:off
		 * Files retrieved with following xpath-expression: 
		 * //index/doc[position() mod 10 = 1 and field[@name='CRID']/val='index.LEAP.FILES' and field[@name='node_id']/val='724' and field[@name='mimetype']/val='application/pdf']/field[@name='content']/val/concat("&#10;BEGIN_TEST_ENTRY&#10;", substring(.,0,300))
		 */
		testFile("de", "test-pdf-de.txt");
		testFile("en", "test-pdf-en.txt");

		testFile("de", "test-excel-de.txt");
		testFile("de", "test-word-de.txt");
	}

	private void testFile(final String lang, final String filename) throws Exception {
		Scanner scanner = new Scanner(ClassLoader.getSystemResourceAsStream(filename), "UTF-8");
		try {
			StringBuilder item = null;
			while (scanner.hasNextLine()) {
				String s = scanner.nextLine();
				if (s.contains("BEGIN_TEST_ENTRY")) {
					if (item != null) {
						testString(lang, item.toString());
					}
					item = new StringBuilder();
				}
				if (item != null) {
					item.append(System.lineSeparator());
					item.append(s);
				}
			}
		} finally {
			scanner.close();
		}
	}

	private void testString(final String language, final String str) throws CRException {
		bean.set("content", str);
		t.processBean(bean);
		String s = (String) bean.get("lang");

		Assert.assertEquals("The found Language (" + s + ") should be (" + language + "). String: '" + str + "'", language, s);
	}

	@After
	public void tearDown() throws Exception {

	}
}
