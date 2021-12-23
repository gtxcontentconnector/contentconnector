package com.gentics.cr.configuration;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Field;

import com.gentics.api.lib.cache.PortalCache;
import com.gentics.api.lib.cache.PortalCacheException;
import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.engine.control.CompositeCacheManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gentics.cr.util.CRUtil;

public class EnvironmentConfigurationTest {

	private String confPath;

	@Before
	public void setUp() throws Exception {
		confPath = new File(this.getClass().getResource("nodelog.yml").toURI()).getParentFile().getAbsolutePath();
		System.setProperty(CRUtil.PORTALNODE_CONFPATH, "");
	}

	@After
	public void cleanup() throws Exception {
		Field cacheMgr = JCS.class.getDeclaredField("cacheMgr");
		cacheMgr.setAccessible(true);
		cacheMgr.set(cacheMgr, null);
		Field instance = CompositeCacheManager.class.getDeclaredField("instance");
		instance.setAccessible(true);
		instance.set(null, null);
	}

	@Test
	public void testLoadLoggerPropertiesURL() {
		System.setProperty(CRUtil.PORTALNODE_CONFPATH, confPath);
		EnvironmentConfiguration.loadLoggerProperties();
		assertTrue("Logger initialization has failed.", EnvironmentConfiguration.getLoggerState());
	}
	
	@Test
	public void testLoadLoggerFallbackProperties() {
		System.setProperty(CRUtil.PORTALNODE_CONFPATH, "some/wrong/path");
		EnvironmentConfiguration.loadLoggerProperties();
	}

	@Test
	public void testLoadLoggerPropertiesCleanedURL() {
		String cleanedConfpath = confPath.replace("file:/", "");
		System.setProperty(CRUtil.PORTALNODE_CONFPATH, cleanedConfpath);
		EnvironmentConfiguration.loadLoggerProperties();
		assertTrue("Logger initialization has failed.", EnvironmentConfiguration.getLoggerState());
	}

	@Test
	public void testConfigDirectory() throws CacheException, PortalCacheException {
		EnvironmentConfiguration.setConfigPath(confPath);
		PortalCache.getCache("test");
	}
}
