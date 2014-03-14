package com.gentics.cr.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.util.generics.Lists;

/**
 * 
 * @author Christopher
 *
 */
public abstract class AccessibleBean {

	/**
	 * Getter for resolvable objects in the configuration.
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
	 * @return configuration key as string, if configuration key is not set
	 * returns defaultValue.
	 */
	public final String getString(final String key, final String defaultValue) {
		Object result = get(key);
		if (result == null) {
			return defaultValue;
		} else if (result instanceof byte[]) {
			return new String((byte[]) result);
		} else {
			return result.toString();
		}
	}

	/**
	 * get an attribute as {@link CRResolvableBean}.
	 * @param key - attribute key to get
	 * @param defaultValue - default value in case the attribute is not set.
	 * @return attribute as {@link CRResolvableBean}, <code>null</code> in case the attribute is not set
	 */
	public final CRResolvableBean getObject(final String key, final CRResolvableBean defaultValue) {
		Object result = get(key);
		if (result == null) {
			return defaultValue;
		} else if (result instanceof CRResolvableBean) {
			return (CRResolvableBean) result;
		} else {
			return defaultValue;
		}
	}

	/**
	 * Get configuration key as collection of {@link String}s.
	 * @param key - configuration key to get
	 * @param separator - separator to split a string value into multiple
	 * strings
	 * @param defaultValue - default value to use if we cannot get the
	 * property
	 * @return configuration key as collection of strings
	 */
	public final Collection<String> getMultipleString(final String key, final String separator,
			final Collection<String> defaultValue) {
		Object value = get(key);
		if (value instanceof Collection) {
			return Lists.toSpecialList((Collection<?>) value, String.class);
		} else if (value instanceof String) {
			return Arrays.asList(((String) value).split(separator));
		} else if (value != null) {
			return Collections.singletonList(value.toString());
		} else {
			return defaultValue;
		}
	}

	/**
	 * Get configuration key as collection of {@link CRResolvableBean}s.
	 * @param key - configuration key to get
	 * @param defaultValue - default value to use if we cannot get the
	 * property
	 * @return configuration key as collection of {@link CRResolvableBean}s
	 */
	public final Collection<CRResolvableBean> getMultipleObjects(final String key,
			final Collection<CRResolvableBean> defaultValue) {
		Object value = get(key);
		if (value instanceof Collection) {
			return Lists.toSpecialList((Collection<?>) value, CRResolvableBean.class);
		} else if (value instanceof CRResolvableBean) {
			return Collections.singletonList((CRResolvableBean) value);
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
	public final boolean getBoolean(final String key, final boolean defaultValue) {
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
	public final int getInteger(final String key, final int defaultValue) {
		Object o = this.get(key);
		if (o == null) {
			return defaultValue;
		} else {
			if (o instanceof Number) {
				return ((Number) o).intValue();
			}
			String stringValue = getString(key);
			if (stringValue != null) {
				return Integer.parseInt(stringValue);
			} else {
				return defaultValue;
			}
		}
	}

	/**
	 * Get configuration key as long.
	 * @param key configuration key to get
	 * @param defaultValue value to return if we cannot parse the long
	 * @return configuration key as long, 
	 * if it cannot be parsed the default
	 * value is returned.
	 */
	public final long getLong(final String key, final long defaultValue) {
		Object o = this.get(key);
		if (o == null) {
			return defaultValue;
		} else {
			if (o instanceof Number) {
				return ((Number) o).longValue();
			}
			String stringValue = getString(key);
			if (stringValue != null) {
				return Long.parseLong(stringValue);
			} else {
				return defaultValue;
			}
		}
	}

	/**
	 * Get configuration key as float.
	 * @param key configuration key to get
	 * @param defaultValue value to return if we cannot parse the float
	 * @return configuration key as float, if it cannot be parsed the
	 * default value is returned.
	 */
	public final float getFloat(final String key, final float defaultValue) {
		Object o = this.get(key);
		if (o == null) {
			return defaultValue;
		} else {
			if (o instanceof Number) {
				return ((Number) o).floatValue();
			}
			String stringValue = getString(key);
			if (stringValue != null) {
				return Float.parseFloat(stringValue);
			} else {
				return defaultValue;
			}
		}
	}

}
