package com.gentics.cr.util.generics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Helper Class to help you with lists containing objects.
 */
public final class Lists {
	/**
	 * private constructor to prevent instantiation.
	 */
	private Lists() { }
	
	
	/**
	 * Convert a list of objects into a specified list of objects.
	 * @param <T> - Type of objects in target list
	 * @param list - list to convert into list of T
	 * @param clazz - Class to cast elements into
	 * @return list of objects of type T, <code>null</code> in case list is not
	 * a List
	 */
	public static <T> List<T> toSpecialList(final Object list,
			final Class<T> clazz) {
		if (list instanceof List) {
			List<?> givenList = (List<?>) list;
			List<T> result = new ArrayList<T>(givenList.size());
			for (Object listElement : givenList) {
				if (clazz.isInstance(listElement)) {
					result.add(clazz.cast(listElement));
				}
			}
			return result;
		} else {
			return null;
		}
	}
	
	/**
	 * Convert a list of objects into a specified list of objects.
	 * @param <T> - Type of objects in target list
	 * @param <S> - Type of original objects
	 * @param list - list to convert into list of T
	 * @param clazz - Class to cast elements into
	 * @return list of objects of type T
	 */
	public static <S, T extends S> List<T> toSpecialList(
			final Collection<S> list, final Class<T> clazz) {
		List<T> result = new ArrayList<T>(list.size());
		for (S listElement : list) {
			if (clazz.isInstance(listElement)) {
				result.add(clazz.cast(listElement));
			}
		}
		return result;
	}
}
