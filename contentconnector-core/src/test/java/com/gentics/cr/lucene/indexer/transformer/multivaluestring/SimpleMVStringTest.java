package com.gentics.cr.lucene.indexer.transformer.multivaluestring;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.conf.gentics.ConfigDirectory;

public class SimpleMVStringTest {

	CRResolvableBean beanToProcess = null;
	CRConfigUtil conf = new CRConfigUtil();
	
	@Before
	public void init() throws URISyntaxException {
		ConfigDirectory.useThis();
		beanToProcess = new CRResolvableBean();
		
		List<String> perms = Arrays.asList(new String[]{"test", "soc", "mv", "value"});
		
		beanToProcess.set("permissions", perms);
		conf.set("attribute", "permissions");
	}
	
	@Test
	public void testStringCollection() {
		SimpleMVString transformer = new SimpleMVString(conf);
		
		transformer.processBean(beanToProcess);
		Assert.assertEquals("Collection was not properly converted.", "test soc mv value ", beanToProcess.get("permissions"));
	}
	
	@Test
	public void testStringWithDelimiterCollection() {
		conf.set("delimiter", "#");
		SimpleMVString transformer = new SimpleMVString(conf);
		
		transformer.processBean(beanToProcess);
		Assert.assertEquals("Collection was not properly converted.", "test#soc#mv#value#", beanToProcess.get("permissions"));
	}
	
	@Test
	public void testNullBean() {
		SimpleMVString transformer = new SimpleMVString(conf);
		transformer.processBean(null);
	}
	
	@Test
	public void testNullAttribute() {
		CRResolvableBean bean = new CRResolvableBean();
		SimpleMVString transformer = new SimpleMVString(conf);
		
		transformer.processBean(bean);
		
		Assert.assertEquals("Nullvalue could not be found.", "NULL", bean.get("permissions"));
	}
}
