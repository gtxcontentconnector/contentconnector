package com.gentics.cr.configuration.defaultconfiguration;

import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;

import org.junit.Test;

import com.gentics.cr.configuration.EnvironmentConfiguration;

public class DefaultConfigDirectoryTest {

	@Test
	public void testDefaultConfig() throws URISyntaxException {
		DefaultConfigDirectory.useThis();
		EnvironmentConfiguration.loadLoggerProperties();
		assertTrue("Logger initialization has failed.", EnvironmentConfiguration.isLoggerFallbackLoaded() || EnvironmentConfiguration.getLoggerState());
		assertTrue("Cache initialization has failed.", EnvironmentConfiguration.isCacheFallbackLoaded() || EnvironmentConfiguration.getCacheState());
	}
}
