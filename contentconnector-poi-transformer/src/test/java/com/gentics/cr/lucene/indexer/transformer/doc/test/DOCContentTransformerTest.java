package com.gentics.cr.lucene.indexer.transformer.doc.test;

import java.io.InputStream;

import org.apache.poi.util.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.indexer.transformer.AbstractTransformerTest;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;
import com.gentics.cr.lucene.indexer.transformer.doc.DOCContentTransformer;

public class DOCContentTransformerTest extends AbstractTransformerTest {

	CRResolvableBean bean;
	GenericConfiguration config;

	@Before
	public void init() throws Exception {
		bean = new CRResolvableBean();

		InputStream stream = DOCContentTransformerTest.class.getResourceAsStream("testdoc.doc");
		byte[] arr = IOUtils.toByteArray(stream);
		bean.set("binarycontent", arr);

		config = new GenericConfiguration();
		config.set("attribute", "binarycontent");
	}

	@Test
	public void testTransformer() throws Exception {
		ContentTransformer t = new DOCContentTransformer(config);
		t.processBean(bean);
		String s = (String) bean.get("binarycontent");
		String ls = System.getProperty("line.separator");
		Assert.assertEquals("testtext" + ls + ls, s);
	}

	@After
	public void tearDown() throws Exception {

	}
}
