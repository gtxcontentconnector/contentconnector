package com.gentics.cr.lucene.indexer.transformer.test;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.poi.util.IOUtils;
import org.junit.After;
import org.junit.Before;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;
import com.gentics.cr.lucene.indexer.transformer.POIContentTransformer;


public class POIContentTransformerTest extends TestCase {
	CRResolvableBean beanDOCX, beanPPTX, beanXLSX;
	GenericConfiguration config;
	
	ContentTransformer t;
	
	@Before
	public void setUp() throws Exception {
		beanDOCX = new CRResolvableBean();
		
		InputStream stream1 = POIContentTransformerTest.class.getResourceAsStream("testdoc.docx");
		byte[] arr1 = IOUtils.toByteArray(stream1);
		beanDOCX.set("binarycontent", arr1);
		
		beanPPTX = new CRResolvableBean();
		
		InputStream stream2 = POIContentTransformerTest.class.getResourceAsStream("testdoc.pptx");
		byte[] arr2 = IOUtils.toByteArray(stream2);
		beanPPTX.set("binarycontent", arr2);
		
		beanXLSX = new CRResolvableBean();
		
		InputStream stream3 = POIContentTransformerTest.class.getResourceAsStream("testdoc.xlsx");
		byte[] arr3 = IOUtils.toByteArray(stream3);
		beanXLSX.set("binarycontent", arr3);
		
		config = new GenericConfiguration();
		config.set("attribute", "binarycontent");
		
		t = new POIContentTransformer(config);
	}
	
	public void testTransformerDOCX() throws Exception {
		
		t.processBean(beanDOCX);
		String s = (String) beanDOCX.get("binarycontent");
		
		assertTrue("testtext\n".equals(s));
	}
	
	public void testTransformerPPTX() throws Exception {
		t.processBean(beanPPTX);
		String s = (String) beanPPTX.get("binarycontent");
		
		assertTrue("testtext\n\n".equals(s));
	}
	
	public void testTransformerXLSX() throws Exception {
		t.processBean(beanXLSX);
		String s = (String) beanXLSX.get("binarycontent");
		
		assertTrue("Tabelle1\ntesttext\nTabelle2\nTabelle3\n".equals(s));
	}

	@After
	public void tearDown() throws Exception {
		
	}
}
