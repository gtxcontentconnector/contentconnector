package com.gentics.cr.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.apache.jcs.JCS;
import org.junit.Test;

import com.gentics.cr.conf.gentics.ConfigDirectory;

public class CacheInitialisationTest {
	
	@Test
	public void testCacheInitWithConfigSetted() throws Throwable {
		ConfigDirectory.useThis();
		
		JCS instance = JCS.getInstance("test");
		assertNotNull("Cannot initialize the JCS cache from the given config.", instance);
		
		String testobject = "mytestobject";
		
		instance.put("testobject", testobject);
		
		assertNotNull("Cannot load object from cache.", instance.get("testobject"));
		
		//REINIT CACHE
		EnvironmentConfiguration.loadCacheProperties();
		
		assertNotNull("Cannot load object from cache after cache re-init.", instance.get("testobject"));
	}

}
