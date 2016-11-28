package com.gentics.cr.rest.csv;

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

public class CSVRequestProcessorTest extends TestCase {

	RequestProcessor requestProcessor;
	CRRequest request;
	CRConfig config;

	protected void setUp() throws MalformedURLException, URISyntaxException,
			CRException {
		String confpath = new File(this.getClass().getResource("nodelog.properties").toURI()).getParentFile().getAbsolutePath();
		System.setProperty(CRUtil.PORTALNODE_CONFPATH, confpath);
		config = new CRConfigFileLoader("csvfile", this.getClass().getResource(".").toString());

		requestProcessor = config.getNewRequestProcessorInstance(1);
		request = new CRRequest();
	}

	public void testGetObjects() throws CRException {
		Collection<CRResolvableBean> result = requestProcessor.getObjects(request);

		assertEquals("Didn't return enough items", 3, result.size());
		for (CRResolvableBean bean : result) {
			String id = (String) bean.get("id");
			assertEquals("Attribute string was not the String equal to the id of object " + id, id.toString(), bean.get("string"));
			assertEquals("Attribute value was not the set value: value " + id, "value " + id.toString(), bean.get("value"));
		}
	}
}
