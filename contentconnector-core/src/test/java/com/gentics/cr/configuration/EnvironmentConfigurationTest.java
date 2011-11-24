package com.gentics.cr.configuration;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.junit.Before;
import org.junit.Test;

import com.gentics.cr.util.CRUtil;

public class EnvironmentConfigurationTest {
	
	private URL confPath;
	
	@Before
	public void setUp() throws Exception {
		confPath = new File(this.getClass().getResource("nodelog.properties").toURI()).getParentFile().toURI().toURL();
		System.setProperty(CRUtil.PORTALNODE_CONFPATH, "");
	}
	
	@Test
	public void testLoadLoggerPropertiesURL(){
		System.setProperty(CRUtil.PORTALNODE_CONFPATH, confPath.getPath());
		EnvironmentConfiguration.loadLoggerPropperties();
		assertTrue("Logger initialization has failed.", EnvironmentConfiguration.getLoggerState());
	}
	
	@Test
	public void testLoadLoggerPropertiesCleanedURL(){
		String cleanedConfpath = confPath.getPath().replace("file:/", "");
		System.setProperty(CRUtil.PORTALNODE_CONFPATH, cleanedConfpath);
		EnvironmentConfiguration.loadLoggerPropperties();
		assertTrue("Logger initialization has failed.", EnvironmentConfiguration.getLoggerState());
	}
	
	@Test(expected=IllegalStateException.class)
	public void testCacheInitFail() throws CacheException {
		EnvironmentConfiguration.loadCacheProperties();
		JCS instance = JCS.getInstance("test");
	}
	
	@Test
	public void testCacheInitSystemProperty() throws CacheException {
		System.setProperty(CRUtil.PORTALNODE_CONFPATH, confPath.getPath());
		EnvironmentConfiguration.loadCacheProperties();
		JCS instance = JCS.getInstance("test");
		assertNotNull("Cannot initialize the JCS cache from the given config: " + confPath, instance);
		assertEquals("The cache attributes are not loaded from the given config", 314, instance.getCacheAttributes().getMaxObjects());
	}
	
	@Test
	public void testCacheInitWithConfigSetted() throws CacheException {
		EnvironmentConfiguration.setCacheFilePath(confPath.getPath() + File.pathSeparator + "cache.ccf");
		EnvironmentConfiguration.loadCacheProperties();
		JCS instance = JCS.getInstance("test");
		assertNotNull("Cannot initialize the JCS cache from the given config: " + confPath, instance);
		assertEquals("The cache attributes are not loaded from the given config", 314, instance.getCacheAttributes().getMaxObjects());

	}
	
}
