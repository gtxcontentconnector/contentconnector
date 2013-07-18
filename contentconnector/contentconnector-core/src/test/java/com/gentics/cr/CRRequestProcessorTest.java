package com.gentics.cr;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gentics.cr.exceptions.CRException;

public class CRRequestProcessorTest extends RequestProcessorTest {

	private static CRRequestProcessor requestProcessor;
	private static HSQLTestHandler testHandler;
	
	private static String [] attributes = {"filename", "mimetype"};

	@BeforeClass
	public static void setUp() throws CRException, URISyntaxException {
		CRConfigUtil config = HSQLTestConfigFactory.getDefaultHSQLConfiguration(CRRequestProcessorTest.class.getName());
		requestProcessor = new CRRequestProcessor(config.getRequestProcessorConfig(1));
		testHandler = new HSQLTestHandler(config.getRequestProcessorConfig(1));
		
		CRResolvableBean testBean = new CRResolvableBean();
		testBean.setObj_type("10008");
		
		testBean.set("filename", "picture.png");
		testBean.set("mimetype", "image/png");
		
		testHandler.createBean(testBean, attributes);
		
		CRResolvableBean testBean2 = new CRResolvableBean();
		testBean2.setObj_type("10008");
		
		testBean2.set("filename", "file.txt");
		testBean2.set("mimetype", "text/plain");
		
		testHandler.createBean(testBean2, attributes);
	}
	
	@Test
	public void testPrefill() throws CRException {
		CRRequest request = getRequest();
		RequestProcessor processor = getRequestProcessor();
		Collection<CRResolvableBean> beans = processor.getObjects(request);
		
		String[] atts = new String[]{"filename"};
		
		request.setAttributeArray(atts);
		processor.fillAttributes(beans, request);
		
		for(CRResolvableBean bean : beans) {
			testAttributeArray(bean, atts);
		}
	}
	
	private void testAttributeArray(CRResolvableBean bean, String[] expectedAttributes) {
		Map<String, Object> attrMap = bean.getAttrMap();
		for (String att : expectedAttributes) {
			assertEquals("Expected attribute was not in attribute map.", true, attrMap.containsKey(att));
			assertEquals("Expected attribute was null.", true, bean.get(att) != null);
		}
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
