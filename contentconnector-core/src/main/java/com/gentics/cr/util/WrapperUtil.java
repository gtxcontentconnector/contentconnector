package com.gentics.cr.util;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

/**
 * 
 * Last changed: $Date: 2010-04-01 15:24:41 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 543 $
 * @author $Author: supnig@constantinopel.at $
 * 
 */
public class WrapperUtil {

	/**
	 * Probes for known and handleable datatypes and returns the according Objects/Wrappers
	 * @param value Object to probe
	 * @return Object or ResolvableWrapper
	 */
	public static Object resolveType(Object value) {
		if (value != null) {
			if ((value instanceof Map) || (Arrays.asList(value.getClass().getInterfaces()).contains(Map.class))) {
				// return new MapWrapper((Map<Object, Object>) value);
				return value;
			} else if (value.getClass().isArray()) {
				return value;
			} else if (value instanceof Number) {
				return value;
			} else if (value instanceof String) {
				return value;
			} else if (value instanceof Date) {
				return value;
			} else {
				return new BeanWrapper(value);
			}
		}
		return value;
	}

}
