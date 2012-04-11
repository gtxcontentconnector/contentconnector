package com.gentics.cr.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Helper class to initialize {@link Properties} from various objects.
 * @author bigbear3001
 *
 */
public final class PropertyHelper {

	/**
	 * Log4j logger for error and debug messages.
	 */
	private static Logger logger = Logger.getLogger(PropertyHelper.class);

	/**
	 * private constructor to prevent instantiation.
	 */
	private PropertyHelper() {
	}

	/**
	 * initialize a {@link Properties} object from a string.
	 * @param string - string to get the properties from
	 * @return {@link Properties} configured in the string
	 */
	public static Properties initPropertiesFromString(final String string) {
		Properties properties = new Properties();
		InputStream inputStream = new ByteArrayInputStream(string.getBytes());
		try {
			properties.load(inputStream);
		} catch (IOException e) {
			logger.error("Error reading properties from string.", e);
		}
		return properties;
	}
}
