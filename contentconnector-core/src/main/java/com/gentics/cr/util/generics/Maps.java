package com.gentics.cr.util.generics;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Helper Class to help you with maps containing objects.
 */
public final class Maps {
	/**
	 * private constructor to prevent instantiation.
	 */
	private Maps() { }
	
	/**
	 * Converts a generic map into a map with special values.
	 * @param <K> - Type of the keys
	 * @param <V> - Type of the values
	 * @param map - map to convert
	 * @param keyClass - Class of the keys
	 * @param valueClass - Class of the values
	 * @return map with keys of type K and values of type V. all not castable
	 * key value pairs are not contained in the list.
	 */
	public static <K, V> Map<K, V> toSpecialMap(final Map<?, ?> map,
			final Class<K> keyClass, final Class<V> valueClass) {
		Map<K, V> result;
		if (map instanceof HashMap) {
			result = new HashMap<K, V>(map.size());
		} else {
			result = new Hashtable<K, V>(map.size());
		}
		for (Object keyObject : map.keySet()) {
			if (keyClass.isInstance(keyObject)) {
				K key = keyClass.cast(keyObject);
				Object valueObject = map.get(key);
				if (valueClass.isInstance(valueObject)) {
					V value = valueClass.cast(valueObject);
					result.put(key, value);
				}
			}
		}
		return result;
	}
}
