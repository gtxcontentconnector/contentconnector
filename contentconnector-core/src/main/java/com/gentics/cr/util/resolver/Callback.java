package com.gentics.cr.util.resolver;

import java.util.regex.Matcher;

/**
 * Interface for callback objects when replacing in strings with regular
 * expressions.
 * @author bigbear3001
 *
 */
public interface Callback {

	/**
	 * Get the property value for the given match.
	 * @param m - Matcher to get the groups from
	 * @return value of the property
	 */
	String getProperty(Matcher m);

}
