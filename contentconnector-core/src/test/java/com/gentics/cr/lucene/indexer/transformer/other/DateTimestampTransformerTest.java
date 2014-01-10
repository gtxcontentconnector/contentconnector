package com.gentics.cr.lucene.indexer.transformer.other;

import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.conf.gentics.ConfigDirectory;
import com.gentics.cr.exceptions.CRException;

public class DateTimestampTransformerTest {

	
	
	
	@Before
	public void init() throws URISyntaxException {
		ConfigDirectory.useThis();
		
	}
	
	@Test
	public void testTimestampToDate() throws CRException {
		CRResolvableBean beanToProcess = new CRResolvableBean();
		
		beanToProcess.set("src", 946681200);
		CRConfigUtil conf = new CRConfigUtil();
		conf.set("sourceattribute", "src");
		conf.set("targetattribute", "target");
		conf.set("convertto", "date");
		DateTimestampTransformer transformer = new DateTimestampTransformer(conf);
		
		transformer.processBean(beanToProcess);
		
		Assert.assertEquals("Timestamp has not been converted properly.", "01.01.00", beanToProcess.get("target"));
	}
	
	
	@Test
	public void testDateToTimestamp() throws CRException {
		CRResolvableBean beanToProcess = new CRResolvableBean();
		
		beanToProcess.set("src", "01.01.00");
		CRConfigUtil conf = new CRConfigUtil();
		conf.set("sourceattribute", "src");
		conf.set("targetattribute", "target");
		conf.set("convertto", "timestamp");
		DateTimestampTransformer transformer = new DateTimestampTransformer(conf);
		
		transformer.processBean(beanToProcess);
		
		Assert.assertEquals("Timestamp has not been converted properly.", 946681200L, beanToProcess.get("target"));
	}
}
