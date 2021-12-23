package com.gentics.cr.rest.json;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Collection;

import junit.framework.TestCase;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRConfigFileLoader;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.util.CRUtil;

public class JSONRequestProcessorTest extends TestCase {

	RequestProcessor requestProcessor;
	CRRequest request;
	CRConfig config;

	protected void setUp() throws MalformedURLException, URISyntaxException, CRException {
		String confpath = new File(this.getClass().getResource("nodelog.yml").toURI()).getParentFile().getAbsolutePath();
		System.setProperty(CRUtil.PORTALNODE_CONFPATH, confpath);
		config = new CRConfigFileLoader("json", this.getClass().getResource(".").toString());
		requestProcessor = config.getNewRequestProcessorInstance(1);
		request = new CRRequest();
	}

	public void testObjectsFile() throws CRException {
		config = new CRConfigFileLoader("jsonfile", this.getClass().getResource(".").toString());
		requestProcessor = config.getNewRequestProcessorInstance(1);
		testGetObjects();
	}

	public void testGetObjects() throws CRException {
		Collection<CRResolvableBean> result = requestProcessor.getObjects(request);
		assertEquals("Didn't return enough items", 3, result.size());
		for (CRResolvableBean bean : result) {
			Integer id = (Integer) bean.get("id");
			assertEquals("Attribute string was not the String equaling the id of object " + id, id.toString(), bean.get("string"));
			if (id.equals(1)) {
				Collection<?> values = (Collection<Object>) bean.get("values");
				assertEquals("Didn't get all 3 values", 3, values.size());
			} else if (id.equals(2)) {
				CRResolvableBean child = bean.getObject("child", null);
				Integer childId = (Integer) child.get("id");
				assertEquals(
					"Attribute string was not the String equaling the id of object " + childId,
					childId.toString(),
					child.get("string"));
			} else if (id.equals(3)) {
				Collection<CRResolvableBean> children = bean.getMultipleObjects("children", null);
				assertEquals("Didn't get all 2 children", 2, children.size());
			}
		}
	}
}
