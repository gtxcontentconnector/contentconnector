package com.gentics.cr.util.generics;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper Class to help you with lists containing objects.
 */
public final class Lists {
	/**
	 * private constructor to prevent instanciation.
	 */
	private Lists() { }
	
	/**
	 * Convert a list of objects into a specified list of objects.
	 * @param <T> - Type of objects in target list
	 * @param <S> - Type of original objects
	 * @param list - list to convert into list of T
	 * @param clazz - Class to cast elements into
	 * @return list of objects of type T
	 */
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=74420
	public static <S, T extends S> List<T> toSpecialList(final List<S> list,
			final Class<T> clazz) {
		List<T> result = new ArrayList<T>(list.size());
		for (S listElement : list) {
			if (clazz.isInstance(listElement)) {
				result.add(clazz.cast(listElement));
			}
		}
		return result;
	}
}
