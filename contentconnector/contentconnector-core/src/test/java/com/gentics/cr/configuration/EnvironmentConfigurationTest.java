package com.gentics.cr.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.engine.control.CompositeCacheManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gentics.cr.util.CRUtil;

public class EnvironmentConfigurationTest {
	
	private URL confPath;
	
	@Before
	public void setUp() throws Exception {
		confPath = new File(this.getClass().getResource("nodelog.properties").toURI()).getParentFile().toURI().toURL();
		System.setProperty(CRUtil.PORTALNODE_CONFPATH, "");
		EnvironmentConfiguration.setCacheFilePath("${" + CRUtil.PORTALNODE_CONFPATH + "}/cache.ccf");
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
	public void testLoadLoggerPropertiesURL(){
		System.setProperty(CRUtil.PORTALNODE_CONFPATH, confPath.getPath());
		EnvironmentConfiguration.loadLoggerProperties();
		assertTrue("Logger initialization has failed.", EnvironmentConfiguration.getLoggerState());
	}
	
	@Test
	public void testLoadLoggerPropertiesCleanedURL(){
		String cleanedConfpath = confPath.getPath().replace("file:/", "");
		System.setProperty(CRUtil.PORTALNODE_CONFPATH, cleanedConfpath);
		EnvironmentConfiguration.loadLoggerProperties();
		assertTrue("Logger initialization has failed.", EnvironmentConfiguration.getLoggerState());
	}
	
	@Test(expected=IllegalStateException.class)
	public void testCacheInitFail() throws Throwable {
		EnvironmentConfiguration.loadCacheProperties();
		JCS.getInstance("test");
	}
	
	@Test
	public void testCacheInitSystemProperty() throws Throwable {
		System.setProperty(CRUtil.PORTALNODE_CONFPATH, confPath.getPath());
		EnvironmentConfiguration.loadCacheProperties();
		JCS instance = JCS.getInstance("test");
		assertNotNull("Cannot initialize the JCS cache from the given config: " + confPath, instance);
		assertEquals("The cache attributes are not loaded from the given config", 314, instance.getCacheAttributes().getMaxObjects());
	}
	
	@Test
	public void testCacheInitWithConfigSetted() throws Throwable {
		EnvironmentConfiguration.setCacheFilePath(confPath.getPath() + File.separator + "cache2.ccf");
		EnvironmentConfiguration.loadCacheProperties();
		JCS instance = JCS.getInstance("test");
		assertNotNull("Cannot initialize the JCS cache from the given config: " + confPath, instance);
		assertEquals("The cache attributes are not loaded from the given config", 315, instance.getCacheAttributes().getMaxObjects());
	}
	
	@Test
	public void testConfigDirectory() throws CacheException{
		EnvironmentConfiguration.setConfigPath(confPath.getPath());
		EnvironmentConfiguration.loadCacheProperties();
		JCS.getInstance("test");
	}
}
