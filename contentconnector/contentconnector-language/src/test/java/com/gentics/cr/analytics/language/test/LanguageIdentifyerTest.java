package com.gentics.cr.analytics.language.test;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;
import com.gentics.cr.lucene.indexer.transformer.lang.LanguageIdentifyer;

public class LanguageIdentifyerTest extends TestCase {

	CRResolvableBean bean;
	GenericConfiguration config;
	ContentTransformer t;

	@Before
	public void setUp() throws Exception {
		bean = new CRResolvableBean();

		config = new GenericConfiguration();
		config.set("attribute", "content");
		config.set("langattribute", "lang");
		t = new LanguageIdentifyer(config);
	}

	public void testTransformerDE() throws Exception {

		bean.set("content", "Dies ist ein Test um den LanguageDetector auf" + " eine Sprache zu bringen");
		t.processBean(bean);
		String s = (String) bean.get("lang");

		assertEquals("The found Language (" + s + ") should be (de).", "de", s);
	}

	public void testTransformerEN() throws Exception {
		bean.set("content", "This is some text to set the LanguageDetector" + "to a language.");
		t.processBean(bean);
		String s = (String) bean.get("lang");

		assertEquals("The found Language (" + s + ") should be (en).", "en", s);
	}

	@After
	public void tearDown() throws Exception {

	}
}
