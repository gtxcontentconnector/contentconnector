package com.gentics.cr.lucene.indexer.transformer.ppt.test;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.poi.util.IOUtils;
import org.junit.After;
import org.junit.Before;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;
import com.gentics.cr.lucene.indexer.transformer.ppt.PPTContentTransformer;


public class PPTContentTransformerTest extends TestCase {
	CRResolvableBean bean;
	GenericConfiguration config;
	
	@Before
	public void setUp() throws Exception {
		bean = new CRResolvableBean();
		
		InputStream stream = PPTContentTransformerTest.class.getResourceAsStream("testdoc.ppt");
		byte[] arr = IOUtils.toByteArray(stream);
		bean.set("binarycontent", arr);
		
		config = new GenericConfiguration();
		config.set("attribute", "binarycontent");
	}
	
	public void testTransformer() throws Exception {
		ContentTransformer t = new PPTContentTransformer(config);
		t.processBean(bean);
		String s = (String) bean.get("binarycontent");
		String x = "Click to edit Master title style Click to edit Master text styles\rSecond level\rThird level\rFourth level\rFifth level Test Text ";
		assertEquals(x,s);
	}

	@After
	public void tearDown() throws Exception {
		
	}
}
