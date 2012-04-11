package com.gentics.cr.configuration;

import com.gentics.cr.util.CRUtil;

/**
 * Class that manages staging within the configuration.
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 545 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public final class ConfigurationSettings {

	/**
	 * Prevents instantiation.
	 */
	private ConfigurationSettings() {
	}

	/**
	 * Key to determine the current configuration mode.
	 */
	private static final String CONFIGURATION_PROPERTY_NAME = "com.gentics.portalnode.confmode";

	/**
	 * Gets the configuration path set for this environment.
	 * @return configuration path as String
	 * 			- returns "" when configuration mode is not set
	 * 			- configuration path could be e.g. "dev/", "test/", "prod/"
	 * or something similar 
	 */
	public static String getConfigurationPath() {
		String path = "";
		path = getConfigurationMode();
		if (path != null && !path.equals("")) {
			return (path + "/");
		}
		return ("");
	}

	/**
	 * Gets the configuration mode set for this environment.
	 * @return configuration mode as String
	 */
	public static String getConfigurationMode() {
		String mode = "";
		mode = CRUtil.resolveSystemProperties("${" + CONFIGURATION_PROPERTY_NAME + "}");
		return (mode);
	}
}
