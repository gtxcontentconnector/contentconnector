package com.gentics.cr;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.gentics.cr.configuration.EnvironmentConfiguration;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.util.CRUtil;

/**
 * Loads a configuration from a given file.
 * Last changed: $Date: 2010-04-01 15:24:02 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 541 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class CRConfigStreamLoader extends CRConfigUtil {
	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = -3060175234254768280L;
	/**
	 * Log4j logger for error and debug messages.
	 */
	private static Logger log = Logger.getLogger(CRConfigStreamLoader.class);
	
	/**
	 * Load config from String with subdir.
	 * @param name configuration name
	 * @param stream stream of properties.
	 * @throws IOException 
	 */
	public CRConfigStreamLoader(final String name, final InputStream stream) throws IOException {
		super();
		
		//Load Environment Properties
		EnvironmentConfiguration.loadEnvironmentProperties();
		setName(name);
		
		loadConfigFile(stream);
		// initialize datasource with handle_props and dsprops
		initDS();

	}

	/**
	 * Load the config Stream for this instance.
	 * @param stream a property stream.
	 * @throws IOException 
	 */
	private void loadConfigFile(final InputStream stream) throws IOException {
		Properties props = new Properties();
		props.load(stream);
		loadConfiguration(this, props, null);
	}

		/**
	 * Loads configuration properties into a GenericConfig instance and resolves
	 * system variables.
	 * @param emptyConfig - configuration to load the properties into.
	 * @param props - properties to load into the configuration
	 * @param webapproot - root directory of the web application for resolving ${webapproot} in property values.
	 */
	public static void loadConfiguration(final GenericConfiguration emptyConfig, final Properties props,
			final String webapproot) {
		for (Entry<Object, Object> entry : props.entrySet()) {
			Object value = entry.getValue();
			Object key = entry.getKey();
			setProperty(emptyConfig, (String) key, (String) value);
		}
	}

	/**
	 * Set a property for this configuration. Resolves system properties in
	 * values.
	 * @param config - configuration to set the property for
	 * @param key - property to set
	 * @param value - value to set for the property
	 */
	private static void setProperty(final GenericConfiguration config, final String key, final String value) {
		//Resolve system properties, so that they can be used in config values
		String resolvedValue = CRUtil.resolveSystemProperties((String) value);
		
		//Set the property
		config.set(key, resolvedValue);
		log.debug("CONFIG: " + key + " has been set to " + resolvedValue);
	}

}
