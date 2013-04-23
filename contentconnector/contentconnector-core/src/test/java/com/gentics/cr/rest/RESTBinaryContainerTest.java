package com.gentics.cr.rest;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.apache.commons.httpclient.HttpStatus;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.HSQLTestConfigFactory;
import com.gentics.cr.HSQLTestHandler;
import com.gentics.cr.exceptions.CRException;

public class RESTBinaryContainerTest{

	private static RESTBinaryContainer container;
	private static HSQLTestHandler testHandler;
	
	@BeforeClass
	public static void setUp() throws CRException, URISyntaxException {
		CRConfigUtil config = HSQLTestConfigFactory.getDefaultHSQLConfiguration(RESTBinaryContainerTest.class.getName());
		config.set("rp.1.rpClass", "com.gentics.cr.CRRequestProcessor");
		container = new RESTBinaryContainer(config);
		testHandler = new HSQLTestHandler(config.getRequestProcessorConfig(1));
		
		CRResolvableBean testBean = new CRResolvableBean();
		testBean.setObj_type("10008");
		
		byte[] file = testHandler.getFileAsByteArray("picture.png");
		testBean.set("filename", "picture.png");
		testBean.set("binarycontent", file);
		testBean.set("mimetype", "image/png");
		
		testHandler.createBean(testBean);
		
		CRResolvableBean testBean2 = new CRResolvableBean();
		testBean2.setObj_type("10008");
		
		byte[] file2 = testHandler.getFileAsByteArray("file.txt");
		testBean2.set("filename", "file.txt");
		testBean2.set("binarycontent", file2);
		testBean2.set("mimetype", "text/plain");
		
		testHandler.createBean(testBean2);
	}

	@Test
	public void simpleBinaryTest() {
		HashMap<String, Resolvable> objects = new HashMap<String, Resolvable>();
		CRRequest req = new CRRequest();
		req.setRequestFilter("object.filename == 'picture.png'");
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		DummyResponseTypeSetter rts = new DummyResponseTypeSetter();
		
		container.processService(req, objects, stream, rts, null, false);
		
		assertEquals(HttpStatus.SC_OK, rts.getResponseCode());
	}
	
	@Test
	public void simpleNotFoundTest() {
		HashMap<String, Resolvable> objects = new HashMap<String, Resolvable>();
		CRRequest req = new CRRequest();
		req.setRequestFilter("object.filename == 'notfound.png'");
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		DummyResponseTypeSetter rts = new DummyResponseTypeSetter();
		
		container.processService(req, objects, stream, rts, null, false);
		
		assertEquals(HttpStatus.SC_NOT_FOUND, rts.getResponseCode());
	}
	
	@Test
	public void simpleTextFileTest() {
		HashMap<String, Resolvable> objects = new HashMap<String, Resolvable>();
		CRRequest req = new CRRequest();
		req.setRequestFilter("object.filename == 'file.txt'");
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		DummyResponseTypeSetter rts = new DummyResponseTypeSetter();
		
		container.processService(req, objects, stream, rts, null, false);
		String s = stream.toString();
		assertEquals(HttpStatus.SC_OK, rts.getResponseCode());
		assertEquals("testfile", s);
	}
	
	@AfterClass
	public static void tearDown() throws CRException {
		container.finalize();
		testHandler.cleanUp();
	}

}
