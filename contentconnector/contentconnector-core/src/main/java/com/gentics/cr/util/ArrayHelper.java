package com.gentics.cr.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Vector;

/**
 * Utility class for array functions.
 * @author perhab
 *
 */
public final class ArrayHelper {

	/**
	 * private constructor for utility class.
	 */
	private ArrayHelper() {
	}

	/**
	 * Remove one ore more elements from an Array.
	 * @param <T> Object
	 * @param array array to remove the element from
	 * @param elements elements to remove from the array
	 * @return Array without the element.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] removeElements(final T[] array, final T... elements) {
		if (array.length > 0) {
			Vector<T> newArray = new Vector<T>();
			for (int i = 0; i < array.length; i++) {
				int elementFound = Arrays.binarySearch(elements, array[i]);
				if (elementFound < 0) {
					newArray.add(array[i]);
				}
			}
			T[] emptyArray = (T[]) Array.newInstance(array[0].getClass(), 0);
			return newArray.toArray(emptyArray);
		} else {
			return array;
		}
	}
}
