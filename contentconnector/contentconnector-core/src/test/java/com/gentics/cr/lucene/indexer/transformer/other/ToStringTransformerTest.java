package com.gentics.cr.lucene.indexer.transformer.other;

import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.conf.gentics.ConfigDirectory;

public class ToStringTransformerTest {

	CRResolvableBean beanToProcess = null;
	CRConfigUtil conf = new CRConfigUtil();
	
	@Before
	public void init() throws URISyntaxException {
		ConfigDirectory.useThis();
		beanToProcess = new CRResolvableBean();
		beanToProcess.set("src", new Integer(10));
		conf.set("attribute", "src");
	}
	
	@Test
	public void testToString() {
		ToString transformer = new ToString(conf);
		
		transformer.processBean(beanToProcess);
		
		Assert.assertEquals("Value was not properly converted to String.", "10", beanToProcess.get("src"));
	}
}
