package com.gentics.cr.conf.gentics;

import java.net.URISyntaxException;

import com.gentics.cr.configuration.EnvironmentConfiguration;

/**
 * Static helper class to locate the {@link EnvironmentConfiguration} used by the test in a convenient way.
 * <code>
 *  ConfigDirectory.useThis();
 * </code>
 * @author bigbear3001
 *
 */
public final class ConfigDirectory {
	
	/**
	 * private constructor to prevent instantiation.
	 */
	private ConfigDirectory() { }
	
	/**
	 * Executing this method configures the {@link EnvironmentConfiguration} to use the directory of this class (that contains a cache.ccf
	 * and the nodelog.yml).
	 */
	public static void useThis() throws URISyntaxException {
		String configLocation = ConfigDirectory.class.getResource(".").toURI().getPath();
		EnvironmentConfiguration.setConfigPath(configLocation);
		EnvironmentConfiguration.loadEnvironmentProperties();
	}
}
