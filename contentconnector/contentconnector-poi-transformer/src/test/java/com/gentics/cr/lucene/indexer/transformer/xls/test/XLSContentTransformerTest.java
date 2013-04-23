package com.gentics.cr.lucene.indexer.transformer.xls.test;

import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.poi.util.IOUtils;
import org.junit.After;
import org.junit.Before;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;
import com.gentics.cr.lucene.indexer.transformer.xls.XLSContentTransformer;

public class XLSContentTransformerTest extends TestCase {
	CRResolvableBean bean, xlsxbean;
	GenericConfiguration config;

	@Before
	public void setUp() throws Exception {
		bean = new CRResolvableBean();

		InputStream stream = XLSContentTransformerTest.class.getResourceAsStream("testdoc.xls");
		byte[] arr = IOUtils.toByteArray(stream);
		bean.set("binarycontent", arr);

		config = new GenericConfiguration();
		config.set("attribute", "binarycontent");

	}

	public void testTransformer() throws Exception {
		ContentTransformer t = new XLSContentTransformer(config);
		t.processBean(bean);
		String s = (String) bean.get("binarycontent");

		assertTrue("testtext,".equals(s));
	}

	@After
	public void tearDown() throws Exception {

	}
}
