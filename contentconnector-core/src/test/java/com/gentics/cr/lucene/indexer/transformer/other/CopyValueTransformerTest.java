package com.gentics.cr.lucene.indexer.transformer.other;

import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.conf.gentics.ConfigDirectory;

public class CopyValueTransformerTest {

	CRResolvableBean beanToProcess = null;
	CRConfigUtil conf = new CRConfigUtil();
	
	@Before
	public void init() throws URISyntaxException {
		ConfigDirectory.useThis();
		beanToProcess = new CRResolvableBean();
		
		beanToProcess.set("src", "test");
		conf.set("sourceattribute", "src");
		conf.set("targetattribute", "target");
	}
	
	@Test
	public void testSimpleCopy() {
		CopyValue transformer = new CopyValue(conf);
		
		transformer.processBean(beanToProcess);
		
		Assert.assertEquals("Value was not properly copied.", "test", beanToProcess.get("target"));
	}
}
