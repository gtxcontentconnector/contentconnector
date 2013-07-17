package com.gentics.cr.mccr;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gentics.api.lib.etc.ObjectTransformer;
import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.exceptions.CRException;

public class MCCRAllChannelsRequestProcessorTest {

	private static MCCRAllChannelsRequestProcessor requestProcessor;
	private static HSQLMCCRTestHandler testHandler;

	@BeforeClass
	public static void setUp() throws CRException, URISyntaxException {
		CRConfigUtil config = HSQLTestConfigFactory.getDefaultHSQLConfiguration(MCCRAllChannelsRequestProcessorTest.class.getName());
		requestProcessor = new MCCRAllChannelsRequestProcessor(config.getRequestProcessorConfig(1));
		testHandler = new HSQLMCCRTestHandler(config.getRequestProcessorConfig(1));
		
		CRResolvableBean testBean = new CRResolvableBean();
		testBean.setObj_type("10007");
		testBean.setContentid("10007.1");
		testBean.set("content", "test");
		testBean.set("filename", "a");
		testBean.set("node_id", 1);
		
		testHandler.createBean(testBean, 1, 1);
		
		CRResolvableBean testBean2 = new CRResolvableBean();
		testBean2.setObj_type("10007");
		testBean2.setContentid("10007.2");
		testBean2.set("filename", "z");
		testBean2.set("content", "testabgeleitet");
		testBean2.set("node_id", 2);
		
		testHandler.createBean(testBean2, 2, 1);
		
		CRResolvableBean testBean3 = new CRResolvableBean();
		testBean3.setObj_type("10007");
		testBean3.setContentid("10007.3");
		testBean3.set("filename", "c");
		testBean3.set("content", "testeigenernode");
		testBean3.set("node_id", 3);
		
		testHandler.createBean(testBean3, 3, 2);
	}

	@Test
	public void basicResultTest() throws CRException {
		Collection<CRResolvableBean> objects = requestProcessor.getObjects(getRequest());
		assertEquals(3, objects.size());
	}
	
	@Test
	public void uniqueContentidTest() throws CRException {
		Collection<CRResolvableBean> objects = requestProcessor.getObjects(getRequest());
		for (CRResolvableBean bean : objects) {
			String uniqueid = (String) bean.get(MCCRAllChannelsRequestProcessor.UNIQUE_ID_KEY);
			String channel_id = ObjectTransformer.getString(bean.get("channel_id"), "");
			String channelset_id = ObjectTransformer.getString(bean.get("channelset_id"), "");
			assertEquals("Uniqueid was not correct on " + bean.getContentid(), channel_id + "." + channelset_id, uniqueid);
		}
	}
	
	@Test
	public void sortingTest() throws CRException {
		CRRequest req = getRequest();
		req.setSortArray(new String[]{"filename:desc"});
		Collection<CRResolvableBean> objects = requestProcessor.getObjects(req);
		assertEquals("Collection is not sorted (filename:desc)", true, isSorted(objects, new String[]{"10007.2", "10007.3", "10007.1"}));
		
		req.setSortArray(new String[]{"filename:asc"});
		objects = requestProcessor.getObjects(req);
		assertEquals("Collection is not sorted (filename:desc)", true, isSorted(objects, new String[]{"10007.1", "10007.3", "10007.2"}));
		
		req.setSortArray(new String[]{MCCRAllChannelsRequestProcessor.UNIQUE_ID_KEY + ":asc"});
		objects = requestProcessor.getObjects(req);
		assertEquals("Collection is not sorted (filename:desc)", true, isSorted(objects, new String[]{"10007.1", "10007.2", "10007.3"}));
	}
	
	@Test
	public void testPrefill() throws CRException {
		CRRequest request = getRequest();
		Collection<CRResolvableBean> beans = requestProcessor.getObjects(request);
		
		String[] atts = new String[]{"filename"};
		
		request.setAttributeArray(atts);
		requestProcessor.fillAttributes(beans, request);
		
		for(CRResolvableBean bean : beans) {
			testAttributeArray(bean, atts);
		}
	}
	
	@Test
	public void testPrefillWithUniqueID() throws CRException {
		CRRequest request = getRequest();
		Collection<CRResolvableBean> beans = requestProcessor.getObjects(request);
		
		String[] atts = new String[]{"filename"};
		
		request.setAttributeArray(atts);
		requestProcessor.fillAttributes(beans, request, MCCRAllChannelsRequestProcessor.UNIQUE_ID_KEY);
		
		for(CRResolvableBean bean : beans) {
			testAttributeArray(bean, atts);
		}
	}
	
	private void testAttributeArray(CRResolvableBean bean, String[] expectedAttributes) {
		for (String att : expectedAttributes) {
			assertEquals("Expected attribute was null.", true, bean.get(att) != null);
		}
	}
	
	@AfterClass
	public static void tearDown() throws CRException {
		requestProcessor.finalize();
		testHandler.cleanUp();
	}
	
	private boolean isSorted(Collection<? extends Resolvable> coll, String[] expectedcontentids) {
		@SuppressWarnings("unchecked")
		Iterator<Resolvable> collIterator = (Iterator<Resolvable>) coll.iterator();
		for (int i = 0; i < coll.size(); i++) {
			Resolvable reso = collIterator.next();
			String cId = (String) reso.get("contentid");
			if (!cId.equalsIgnoreCase(expectedcontentids[i])) {
				return false;
			}
		}
		return true;
	}
	
	protected CRRequest getRequest() {
		CRRequest req = new CRRequest();
		req.setRequestFilter("object.obj_type == 10007");
		return req;
	}

}
