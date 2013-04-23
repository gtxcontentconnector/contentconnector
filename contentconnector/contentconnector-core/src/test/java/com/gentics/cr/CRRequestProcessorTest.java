package com.gentics.cr;

import java.net.URISyntaxException;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.gentics.cr.exceptions.CRException;

public class CRRequestProcessorTest extends RequestProcessorTest {

	private static CRRequestProcessor requestProcessor;
	private static HSQLTestHandler testHandler;

	@BeforeClass
	public static void setUp() throws CRException, URISyntaxException {
		CRConfigUtil config = HSQLTestConfigFactory.getDefaultHSQLConfiguration(CRRequestProcessorTest.class.getName());
		requestProcessor = new CRRequestProcessor(config.getRequestProcessorConfig(1));
		testHandler = new HSQLTestHandler(config.getRequestProcessorConfig(1));
		
		CRResolvableBean testBean = new CRResolvableBean();
		testBean.setObj_type("10008");
		
		testBean.set("filename", "picture.png");
		testBean.set("mimetype", "image/png");
		
		testHandler.createBean(testBean);
		
		CRResolvableBean testBean2 = new CRResolvableBean();
		testBean2.setObj_type("10008");
		
		testBean2.set("filename", "file.txt");
		testBean2.set("mimetype", "text/plain");
		
		testHandler.createBean(testBean2);
	}

	@Override
	protected RequestProcessor getRequestProcessor() {
		return requestProcessor;
	}
	
	@AfterClass
	public static void tearDown() throws CRException {
		requestProcessor.finalize();
		testHandler.cleanUp();
	}

	@Override
	protected int getExpectedCollectionSize() {
		return 2;
	}

	@Override
	protected CRRequest getRequest() {
		CRRequest req = new CRRequest();
		req.setRequestFilter("object.obj_type == 10008");
		return req;
	}

}
