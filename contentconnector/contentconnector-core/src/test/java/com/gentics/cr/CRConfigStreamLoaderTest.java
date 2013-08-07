package com.gentics.cr;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

import com.gentics.cr.exceptions.CRException;

public class CRConfigStreamLoaderTest {

	@Test
	public void configurationTest() throws CRException, IOException {
		CRConfigUtil config = new CRConfigStreamLoader("test", CRConfigStreamLoaderTest.class.getResourceAsStream("configuration/test.properties"));
		
		Assert.assertEquals("Config property did not match expected value.", "value1", config.get("property1"));
		
		CRConfigUtil rpConfig = config.getRequestProcessorConfig(1);
		
		Assert.assertEquals("Config property did not match expected value.", "rp1", rpConfig.get("test"));
	}

}
