package com.gentics.cr.util.resolver;

import java.util.regex.Matcher;

/**
 * Callback to resolve a system property.
 * @author bigbear3001
 */
public class SystemPropertyCallback implements Callback {
	
	/**
	 * {@inheritDoc}
	 */
	public final String getProperty(final Matcher m) {
		return System.getProperty(m.group(1), "");
	}

}
