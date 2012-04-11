package com.gentics.cr.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.gentics.api.lib.resolving.Resolvable;

/**
 * 
 * Last changed: $Date: 2010-04-01 15:24:41 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 543 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public abstract class ResolvableWrapper implements Resolvable {

	/**
	 * @see com.gentics.api.lib.resolving.Resolvable#canResolve()
	 */
	public boolean canResolve() {
		return true;
	}

	/**
	 * @see com.gentics.api.lib.resolving.Resolvable#get(java.lang.String)
	 */
	public abstract Object get(String key);

	/**
	 * @see com.gentics.api.lib.resolving.Resolvable#getProperty(java.lang.String)
	 */
	public Object getProperty(String key) {
		return get(key);
	}

	/**
	 * Invoke the expected getter method for the given parameter on the given
	 * object.
	 * @param object object for which the getter method shall be invoked
	 * @param parameterName name of the parameter
	 * @return value of the parameter
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	protected final static Object invokeGetter(Object object, String parameterName) throws SecurityException,
			NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Class<? extends Object> clazz = object.getClass();
		String getterName = getGetterName(parameterName);

		Method getter = null;
		// look for the getter method
		getter = clazz.getMethod(getterName, new Class[] {});
		// TODO if "get" getter does not exist, try "is" getter
		return getter.invoke(object, new Object[] {});
	}

	/**
	 * transform the given parameter name to the expected name of the getter
	 * method (according to the javabeans spec.)
	 * @param parameterName name of the parameter
	 * @return expected name of the getter method
	 */
	protected final static String getGetterName(String parameterName) {
		StringBuffer getterName = new StringBuffer();

		getterName.append("get").append(parameterName.substring(0, 1).toUpperCase()).append(parameterName.substring(1));

		return getterName.toString();
	}
}
