package com.gentics.cr.util;

/**
 * 
 * @author Christopher
 *
 */
public abstract class AccessibleBean {
	
	/**
	 * Gettor for resolvable objects in the configuration.
	 * @param key resolving key
	 * @return resolved object
	 */
	public abstract Object get(String key);
	
		/**
		 * Wrapper for {@link #get(String)}.
		 * @param key resolving key
		 * @return returns the result of {@link #get(String)} as a String
		 */
		
		public final String getString(final String key) {
			return getString(key, null);
		}
		
		/**
		 * Get configuration key as {@link String}.
		 * @param key configuration key to get
		 * @param defaultValue value to return if configuration key is not set.
		 * @return configruation key as string, if configuration key is not set
		 * returns defaultValue.
		 */
		public final String getString(final String key, 
				final String defaultValue) {
			Object result = get(key);
			if (result != null) {
				return result.toString();
			} else {
				return defaultValue;
			}
		}
		
		
		/**
		 * Get configuration key as boolean value.
		 * @param key configuration key to get
		 * @return boolean value of the configuration key if it can be parsed,
		 * otherwise it returns <code>false</code>.
		 */
		public final boolean getBoolean(final String key) {
			return getBoolean(key, false);
		}
		
		
		/**
		 * Get configuration key as boolean value.
		 * @param key configuration key to get
		 * @param defaultValue value to return if we cannot parse the boolean
		 * @return boolean value of the configuration key if it can be parsed,
		 * otherwise the default value is returned.
		 */
		public final boolean getBoolean(final String key,
				final boolean defaultValue) {
			return StringUtils.getBoolean(get(key), defaultValue);
		}
		
		/**
		 * Get configuration key as integer.
		 * @param key configuration key to get
		 * @param defaultValue value to return if we cannot parse the integer
		 * @return configuration key as integer, 
		 * if it cannot be parsed the default
		 * value is returned.
		 */
		public final int getInteger(final String key,
				final int defaultValue) {
			String stringValue = getString(key);
			if (stringValue != null) {
				return Integer.parseInt(stringValue);
			} else {
				return defaultValue;
			}
		}
		
		/**
		 * Get configuration key as float.
		 * @param key configuration key to get
		 * @param defaultValue value to return if we cannot parse the float
		 * @return configuration key as float, if it cannot be parsed the
		 * default value is returned.
		 */
		public final float getFloat(final String key,
				final float defaultValue) {
			String stringValue = getString(key);
			if (stringValue != null) {
				return Float.parseFloat(stringValue);
			} else {
				return defaultValue;
			}
		}
		

}
