package com.gentics.cr.configuration.defaultconfiguration;

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
public final class DefaultConfigDirectory {
	
	/**
	 * private constructor to prevent instantiation.
	 */
	private DefaultConfigDirectory() { }
	
	/**
	 * Executing this method configures the {@link EnvironmentConfiguration} to use the directory of this class (that contains a cache.ccf
	 * and the nodelog.properties).
	 */
	public static void useThis() throws URISyntaxException {
		String configLocation = DefaultConfigDirectory.class.getResource(".").toURI().getPath();
		EnvironmentConfiguration.setConfigPath(configLocation);
		EnvironmentConfiguration.loadEnvironmentProperties();
	}
}
