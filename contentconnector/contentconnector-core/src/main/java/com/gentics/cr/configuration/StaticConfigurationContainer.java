package com.gentics.cr.configuration;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.gentics.cr.CRConfigFileLoader;

/**
 * Class that acts as configuration container.
 * Last changed: $Date: 2009-09-02 17:57:48 +0200 (Mi, 02 Sep 2009) $
 * @version $Revision: 180 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public final class StaticConfigurationContainer {

	/**
	 * Logger.
	 */
	private static Logger log = Logger.getLogger(StaticConfigurationContainer.class);
	/**
	 * Cache for parsed configs.
	 */
	private static ConcurrentHashMap<String, CRConfigFileLoader> configmap = new ConcurrentHashMap<String, CRConfigFileLoader>(2);

	/**
	 * Prevent instantiation.
	 */
	private StaticConfigurationContainer() {
	}

	/**
	 * Fetches a already created instance of the requested config. 
	 * If config was not created yet, one will be instantiated.
	 * @param key konfiguration key. e.g. servlet name
	 * @param webapproot root of the file location
	 * @return Config as <code>CRConfigFileLoader</code>
	 */
	public static CRConfigFileLoader getConfig(final String key, final String webapproot) {
		return StaticConfigurationContainer.getConfig(key, webapproot, "");
	}

	/**
	 * Gets configuration.
	 * @param key configuration key.
	 * @param webapproot root of file location.
	 * @param subdir sub directory.
	 * @return Configuration as <code>CRConfigFileLoader</code>
	 */
	public static CRConfigFileLoader getConfig(final String key, final String webapproot, final String subdir) {
		assertStaticAttributesInitialized();

		CRConfigFileLoader config = configmap.get(key);
		if (config == null) {
			log.debug("Config not found, will create new config instance.");
			config = new CRConfigFileLoader(key, webapproot, subdir);
			if (config != null) {
				configmap.put(key, config);
			}
		}

		return config;
	}

	/**
	 * Used to set a config by putting it into the configmap.
	 * @param key for storing the config in the internal configmap.
	 * @param config Config to set.
	 */
	public static void setConfig(final String key, final CRConfigFileLoader config) {
		configmap.put(key, config);
	}

	/**
	 * Method to ensure that the container has been initialized.
	 */
	private static void assertStaticAttributesInitialized() {
		if (configmap == null) {
			configmap = new ConcurrentHashMap<String, CRConfigFileLoader>(2);
		}
		if (log == null) {
			log = Logger.getLogger(StaticConfigurationContainer.class);
		}
	}
}
