package com.gentics.cr.util;

import java.util.Map;

/**
 * Wrapper for maps.
 * Last changed: $Date: 2010-04-01 15:24:41 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 543 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class MapWrapper extends ResolvableWrapper {
	/**
	 * Wrapped map.
	 */
	private Map<Object, Object> wrappedMap;

	/**
	 * Create new instance and wrap a map.
	 * @param map map to be wrapped
	 */
	public MapWrapper(final Map<Object, Object> map) {
		this.wrappedMap = map;
	}

	/**
	 * Get the wrapped map instance.
	 * @return wrapped map
	 */
	public final Map<Object, Object> getMap() {
		return wrappedMap;
	}

	/**
	 * get property to given key.
	 * @param key key of the maps item
	 * @return item to be retrieved.
	 */
	public final Object get(final String key) {
		return wrappedMap.get(key);

	}

}
