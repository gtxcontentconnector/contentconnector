package com.gentics.cr.configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.gentics.cr.util.CRUtil;

/**
 * Loads the contents of a file to a GenericConfiguration.
 * 
 * 
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 545 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class GenericConfigurationFileLoader {
	/**
	 * Logger.
	 */
	private static Logger log = Logger.getLogger(
			GenericConfigurationFileLoader.class);
	
	/**
	 * Prevent instantiation.
	 */
	private GenericConfigurationFileLoader() { }
	
	/**
	 * Loads the contents of a Properties file that is located in the rest 
	 * directory to the passed GenericConfiguration.
	 * @param config - the configuration to load the properties to
	 * @param servletname - will be used to identify the file
	 * 						e.g.: ${com.gentics.portalnode.confpath}
	 * /rest/<servletname>.properties
	 */
	public static void loadPerServletname(GenericConfiguration config, 
			final String servletname) {
		String path = CRUtil.resolveSystemProperties(
				"${com.gentics.portalnode.confpath}/rest/"
				+ servletname + ".properties");
		try {
			load(config, path);
		} catch (FileNotFoundException e) {
			log.error("Could not load configuration from " + path, e);
		} catch (IOException e) {
			log.error("Could not load configuration from " + path, e);
		}
	}
	
	/**
	 * Loads the contents of a Properties file 
	 * to the passed GenericConfiguration.
	 * @param config - configuration to load Files to
	 * @param path - Path to the configuration File. 
	 * Can contain environment variables.
	 * @throws IOException 
	 */
	public static void load(GenericConfiguration config, 
			final String path) throws IOException {
		Properties props = new Properties();
		props.load(new FileInputStream(CRUtil.resolveSystemProperties(path)));
		for (Entry<Object, Object> entry : props.entrySet()) {
			Object value = entry.getValue();
			Object key = entry.getKey();
			setProperty(config, (String) key, (String) value);
		}
	}
	
	/**
	 * Set a property to a generic configuration.
	 * @param config configuration where the property should be set
	 * @param key resolving key
	 * @param value value
	 */
	private static void setProperty(GenericConfiguration config,
			final String key, final String value) {
		//Resolve system properties, so that they can be used in config values
		String val = CRUtil.resolveSystemProperties((String) value);
				
		//Set the property
		config.set(key, val);
	}
}
