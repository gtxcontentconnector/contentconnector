package com.gentics.cr.rest.csv;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRConfigFileLoader;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.util.CRUtil;

public class CSVRequestProcessorTest {

	RequestProcessor requestProcessor;
	CRRequest request;
	CRConfig config;

	@Before
	public void setUp() throws MalformedURLException, URISyntaxException,
			CRException {
		String confpath = new File(this.getClass().getResource("nodelog.properties").toURI()).getParentFile().getAbsolutePath();
		System.setProperty(CRUtil.PORTALNODE_CONFPATH, confpath);
		config = new CRConfigFileLoader("csvfile", this.getClass().getResource(".").toString());

		requestProcessor = config.getNewRequestProcessorInstance(1);
		request = new CRRequest();
	}

	@Test
	public void testGetObjects() throws CRException {
		Collection<CRResolvableBean> result = requestProcessor.getObjects(request);

		assertEquals("Didn't return enough items", 6, result.size());
		for (CRResolvableBean bean : result) {
			Integer id = Integer.valueOf((String) bean.get("id"));
				
			if(id == 4) {
				assertEquals("Attribute value was not the set value: value " + id, "value ;" + id.toString(), bean.get("value"));
			} else {
				assertEquals("Attribute value was not the set value: value " + id, "value " + id.toString(), bean.get("value"));
			}
			
			if(id == 6) {
				assertEquals("Attribute string should be a trimmed empty string", "", bean.get("string"));
			} else {
				assertEquals("Attribute string was not the String equal to the id of object " + id, id.toString(), bean.get("string"));
			}
		}
	}
}
