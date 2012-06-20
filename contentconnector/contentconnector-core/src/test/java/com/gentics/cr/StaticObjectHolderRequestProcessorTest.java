package com.gentics.cr;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

import org.easymock.EasyMock;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gentics.cr.configuration.EnvironmentConfiguration;
import com.gentics.cr.configuration.SimpleCRConfig;
import com.gentics.cr.exceptions.CRException;

public class StaticObjectHolderRequestProcessorTest {
	
	
	
	
	
	private static StaticObjectHolderRequestProcessor processor;
	private static Collection<CRResolvableBean> objects;
	
	@BeforeClass
	public static void init() throws CRException, URISyntaxException {
		String configLocation = StaticObjectHolderRequestProcessorTest.class.getResource("/com/gentics/cr/conf/gentics").toURI().getPath();
		EnvironmentConfiguration.setConfigPath(configLocation);
		EnvironmentConfiguration.loadEnvironmentProperties();
		objects = new ArrayList<CRResolvableBean>();
		objects.add(createBean("a.txt", ""));
		objects.add(createBean("b.txt", ""));
		objects.add(createBean("c.txt", ""));
		CRConfig config = new SimpleCRConfig();
		processor = new StaticObjectHolderRequestProcessor(config);
		processor.setObjects(objects);
	}
	
	

	private static CRResolvableBean createBean(String filename, String pubdir) {
		CRResolvableBean bean = new CRResolvableBean();
		bean.set("filename", filename);
		bean.set("pub_dir", pubdir);
		return bean;
	}
	
	
	@Test
	public void testReturnAllBeans() throws CRException {
		Collection<CRResolvableBean> beans = processor.getObjects(new CRRequest("1 == 1"));
		assertEquals("Didn't get all beans for a rule that is true on every object.", 3, beans.size());
	}
	
	@Test
	public void testReturnBeanWithRule() throws CRException {
		Collection<CRResolvableBean> beans = processor.getObjects(new CRRequest("object.filename == 'a.txt'"));
		assertEquals("Size of returned collection points out that there isn't only one object returned.", 1, beans.size());
		CRResolvableBean bean = beans.iterator().next();
		assertEquals("filename of the returned bean has not the expected value.", "a.txt", bean.get("filename"));
	}
	
	@Test
	public void testReturnNoBean() throws CRException {
		Collection<CRResolvableBean> beans = processor.getObjects(new CRRequest("1 != 1"));
		assertEquals("Somehow a result was returned for a rule that shouldn't match one element.", 0, beans.size());
	}
}
