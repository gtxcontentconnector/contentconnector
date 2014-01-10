package com.gentics.cr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.jcs.JCS;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.cr.exceptions.CRException;

public class CachedCRRequestProcessorTest extends RequestProcessorTest {

	private static RequestProcessor requestProcessor;
	private static RequestProcessor requestProcessor2;
	private static HSQLCRTestHandler testHandler;
	
	private static String configname;
	
	private static String [] attributes = {"filename", "mimetype"};
	private static String [] attributes2 = {"filename", "category"};

	@BeforeClass
	public static void setUp() throws CRException, URISyntaxException {
		configname = CachedCRRequestProcessorTest.class.getName() + ".RP.1";
		CRConfigUtil config = HSQLTestConfigFactory.getDefaultHSQLConfiguration(CachedCRRequestProcessorTest.class.getName(), true);
		requestProcessor = new CachedCRRequestProcessor(config.getRequestProcessorConfig(1));
		testHandler = new HSQLCRTestHandler(config.getRequestProcessorConfig(1));
		
		CRConfigUtil config2 = HSQLTestConfigFactory.getDefaultHSQLConfiguration(CachedCRRequestProcessorTest.class.getName(), true);
		config2.setName("dummy");
		requestProcessor2 = new CachedCRRequestProcessor(config2.getRequestProcessorConfig(1));
		
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
		testHandler.createBean(createBean("10001.1", "falcon.jpg", "animals"), attributes2);
		testHandler.createBean(createBean("10001.2", "ford.jpg", "cars"), attributes2);
		testHandler.createBean(createBean("10001.3", "eagle.jpg", "animals"), attributes2);
		testHandler.createBean(createBean("10001.4", "tree.jpg", "plants"), attributes2);
		testHandler.createBean(createBean("10001.5", "bird.jpg", "animals"), attributes2);
		testHandler.createBean(createBean("10001.6", "honda.jpg", "cars"), attributes2);
		testHandler.createBean(createBean("10001.7", "flower.jpg", "plants"), attributes2);
		testHandler.createBean(createBean("10001.8", "saab.jpg", "cars"), attributes2);
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
	
	@Test
	public void testCachRegionName() throws CRException {
		CRRequest request = getRequest();
		RequestProcessor processor = getRequestProcessor();
		CRResolvableBean content = processor.getContent(request);
		assertNotNull("Content should not be null.", content);
		
		JCS cache = processor.getCache();
		
		String regionName = cache.getStatistics().getRegionName();
		assertEquals("Region name was not correct.","gentics-cr-" + configname + "-crcontent", regionName );
	}
	
	private void testAttributeArray(CRResolvableBean bean, String[] expectedAttributes) {
		Map<String, Object> attrMap = bean.getAttrMap();
		for (String att : expectedAttributes) {
			assertEquals("Expected attribute was not in attribute map.", true, attrMap.containsKey(att));
			assertEquals("Expected attribute was null.", true, bean.get(att) != null);
		}
	}
	
	@Test
	public void testSorting() throws CRException {
		CRRequest req = new CRRequest();
		req.setRequestFilter("object.obj_type == 10001");
		req.setSortArray(new String[]{"category:asc", "filename:asc"});
		RequestProcessor processor = getRequestProcessor();
		Collection<CRResolvableBean> beans = processor.getObjects(req);
		assertTrue("Collection is not properly sorted.", isSorted(beans, new String[]{"10001.5", "10001.3", "10001.1", "10001.2", "10001.6", "10001.8", "10001.7", "10001.4"}));
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
	
	private static CRResolvableBean createBean(String contentid, String filename, String catetory) {
		CRResolvableBean bean = new CRResolvableBean(contentid);
		bean.set("filename", filename);
		bean.set("category", catetory);
		return bean;
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

}
