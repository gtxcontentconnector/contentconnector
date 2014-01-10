package com.gentics.cr.lucene.indexer.transformer.txt;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.conf.gentics.ConfigDirectory;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;

public class TXTContentTransformerTest {
	CRResolvableBean bean;
	GenericConfiguration config;

	@Before
	public void init() throws Exception {
		ConfigDirectory.useThis();
		bean = new CRResolvableBean();

		InputStream stream = TXTContentTransformerTest.class.getResourceAsStream("testdoc.txt");
		byte[] arr = IOUtils.toByteArray(stream);
		bean.set("binarycontent", arr);

		config = new GenericConfiguration();
		config.set("attribute", "binarycontent");
	}

	@Test
	public void testTransformer() throws Exception {
		ContentTransformer t = new TXTContentTransformer(config);
		t.processBean(bean);
		String s = (String) bean.get("binarycontent");
		Assert.assertEquals("testdoc text", s);
	}
}
