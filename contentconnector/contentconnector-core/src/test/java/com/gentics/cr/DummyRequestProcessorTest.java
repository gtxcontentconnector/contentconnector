package com.gentics.cr;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.Collection;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gentics.cr.exceptions.CRException;

public class DummyRequestProcessorTest extends RequestProcessorTest {

	private static RequestProcessor requestProcessor;
	
	
	@BeforeClass
	public static void setUp() throws CRException, URISyntaxException {
		CRConfigUtil config = HSQLTestConfigFactory.getDefaultHSQLConfiguration(DummyRequestProcessorTest.class.getName(), true);
		requestProcessor = new DummyRequestProcessor(config.getRequestProcessorConfig(1));
			
	}
	
	@Test
	public void testGetObjects() throws CRException {
		CRRequest request = getRequest();
		RequestProcessor processor = getRequestProcessor();
		Collection<CRResolvableBean> beans = processor.getObjects(request);
		assertEquals("Collection did not have the expected size.", getExpectedCollectionSize() , beans.size());
	}
	
	

	@Override
	protected RequestProcessor getRequestProcessor() {
		return requestProcessor;
	}
	
	@AfterClass
	public static void tearDown() throws CRException {
		requestProcessor.finalize();
	}

	
	@Override
	protected CRRequest getRequest() {
		CRRequest req = new CRRequest();
		req.setRequestFilter("object.obj_type == 10008");
		return req;
	}

	@Override
	protected int getExpectedCollectionSize() {
		// TODO Auto-generated method stub
		return 10;
	}

}
