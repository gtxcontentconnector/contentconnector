package com.gentics.cr.configuration;

import static org.junit.Assert.assertNotNull;

import com.gentics.api.lib.cache.PortalCache;
import org.junit.Test;

import com.gentics.cr.conf.gentics.ConfigDirectory;

public class CacheInitialisationTest {
	
	@Test
	public void testCacheInitWithConfigSetted() throws Throwable {
		ConfigDirectory.useThis();
		
		PortalCache instance = PortalCache.getCache("test");
		assertNotNull("Cannot initialize the JCS cache from the given config.", instance);
		
		String testobject = "mytestobject";
		
		instance.put("testobject", testobject);

		assertNotNull("Cannot load object from cache.", instance.get("testobject"));
	}

}
