package com.gentics.cr.lucene.indexer.transformer.xlsx.test;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.poi.util.IOUtils;
import org.junit.After;
import org.junit.Before;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;
import com.gentics.cr.lucene.indexer.transformer.xlsx.XLSXContentTransformer;


public class XLSXContentTransformerTest extends TestCase {
	CRResolvableBean bean, xlsxbean;
	GenericConfiguration config;
	
	@Before
	public void setUp() throws Exception {
		bean = new CRResolvableBean();
		
		InputStream stream = XLSXContentTransformerTest.class.getResourceAsStream("testdoc.xlsx");
		byte[] arr = IOUtils.toByteArray(stream);
		bean.set("binarycontent", arr);
		
		config = new GenericConfiguration();
		config.set("attribute", "binarycontent");
		
			
	}
	
	public void testTransformer() throws Exception {
		ContentTransformer t = new XLSXContentTransformer(config);
		t.processBean(bean);
		String s = (String) bean.get("binarycontent");
		
		assertEquals("testtextÜ",s);
	}
	
	
	@After
	public void tearDown() throws Exception {
		
	}
}
