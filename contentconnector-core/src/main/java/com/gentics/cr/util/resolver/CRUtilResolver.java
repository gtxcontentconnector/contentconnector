package com.gentics.cr.util.resolver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves property placeholders in strings.
 * @author bigbear3001
 */
public final class CRUtilResolver {

	/**
	 * pattern to find system properties.
	 */
	private static Pattern findSystemProperties = Pattern.compile("\\$\\{([a-zA-Z0-9\\._-]+)\\}");

	/**
	 * Callback for replacing system properties.
	 */
	private static Callback systemPropertyCallback = new SystemPropertyCallback();

	/**
	 * pattern to find contentconnector properties.
	 */
	private static Pattern findContentConnectorProperties = Pattern.compile("\\&\\{([a-zA-Z0-9_-]+).([a-zA-Z0-9\\._-]+)\\}");

	/**
	 * Callback for replacing ContentConnector properties.
	 */
	private static Callback contentConnectorCallback = new ContentConnectorCallback();

	/**
	 * private Constructor to prevent instanciation.
	 */
	private CRUtilResolver() {}

	/**
	 * Resolve  properties with the specified pattern.
	 * @param string - String to resolve the system properties in
	 * @param pattern - Pattern for finding the system property placeholder in
	 * the string.
	 * @param callback - Callback to use for a match
	 * @return String with replaced system properties
	 */
	public static String resolvePropertiesWithCallback(final String string, final Pattern pattern, final Callback callback) {
		// create a matcher
		Matcher m = pattern.matcher(string);
		StringBuffer output = new StringBuffer();
		int startIndex = 0;
		while (m.find()) {
			// copy static string between the last found system
			// property and this one
			if (m.start() > startIndex) {
				output.append(string.substring(startIndex, m.start()));
			}
			output.append(callback.getProperty(m));
			startIndex = m.end();
		}
		// if some trailing static string exists, copy it
		if (startIndex < string.length()) {
			output.append(string.substring(startIndex));
		}
		return output.toString();
	}

	/**
	 * Resolve the content connector properties.
	 * @param string - String to resolve the contentconnector properties in
	 * @return String with replaced contentconnector properties
	 */

	public static String resolveContentConnectorProperties(final String string) {
		return CRUtilResolver.resolvePropertiesWithCallback(string, findContentConnectorProperties, contentConnectorCallback);
	}

	/**
	 * Resolve the system properties.
	 * @param string - String to resolve the system properties in
	 * @return String with replaced system properties
	 */
	public static String resolveSystemProperties(final String string) {
		return CRUtilResolver.resolvePropertiesWithCallback(string, findSystemProperties, systemPropertyCallback);
	}
}
