package com.gentics.cr.lucene.indexer.transformer.other;

import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.conf.gentics.ConfigDirectory;

public class SetValueTransformerTest {

	CRResolvableBean beanToProcess = null;
	CRConfigUtil conf = new CRConfigUtil();
	
	@Before
	public void init() throws URISyntaxException {
		ConfigDirectory.useThis();
		beanToProcess = new CRResolvableBean();
		
		conf.set("attribute", "src");
		conf.set("value", "test");
	}
	
	@Test
	public void testSetValue() {
		SetValue transformer = new SetValue(conf);
		
		transformer.processBean(beanToProcess);
		
		Assert.assertEquals("Value was not properly set.", "test", beanToProcess.get("src"));
	}
}
