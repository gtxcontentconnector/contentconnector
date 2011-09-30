package com.gentics.cr.configuration;

import java.io.File;
import java.net.URL;

import com.gentics.cr.util.CRUtil;

import junit.framework.TestCase;

public class EnvironmentConfigurationTest extends TestCase {
	
	private URL confPath;
	
	@Override
	protected void setUp() throws Exception {
		confPath = new File(this.getClass().getResource("nodelog.properties").toURI()).getParentFile().toURI().toURL();
	}
	
	public void testLoadLoggerPropertiesURL(){
		System.setProperty(CRUtil.PORTALNODE_CONFPATH, confPath.getPath());
		EnvironmentConfiguration.loadLoggerPropperties();
		assertTrue("Logger initialization has failed.", EnvironmentConfiguration.getLoggerState());
	}
	
	public void testLoadLoggerPropertiesCleanedURL(){
		String cleanedConfpath = confPath.getPath().replace("file:/", "");
		System.setProperty(CRUtil.PORTALNODE_CONFPATH, cleanedConfpath);
		EnvironmentConfiguration.loadLoggerPropperties();
		assertTrue("Logger initialization has failed.", EnvironmentConfiguration.getLoggerState());
	}
}
