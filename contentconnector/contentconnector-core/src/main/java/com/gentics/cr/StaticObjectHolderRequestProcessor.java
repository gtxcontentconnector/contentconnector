package com.gentics.cr;

import java.util.Collection;
import java.util.Vector;

import com.gentics.cr.exceptions.CRException;

/**
 * This is a simple RequestProcessor that just holds a static array of CRResolvables in the JVM. 
 * This is especially useful for unit testing classes that work with a RequestProcessor but maybe somebody can 
 * use this for other things.
 * @author bigbear3001
 *
 */
public class StaticObjectHolderRequestProcessor extends RequestProcessor {

	/**
	 * Collection of resolvables that we return in 
	 * {@link com.gentics.cr.StaticObjectHolderRequestProcessor#getObjects(CRRequest, boolean)}.
	 */
	public static Collection<CRResolvableBean> objects = new Vector<CRResolvableBean>();

	/**
	 * Initialize a new StaticObjectHolderRequestProcessor.
	 * @param config - configuration of the RequestProcessor
	 * @throws CRException - if no configuration is given, or the PlinkProcessor could not be initialized.
	 * @see RequestProcessor
	 */
	public StaticObjectHolderRequestProcessor(final CRConfig config) throws CRException {
		super(config);
	}

	@Override
	public Collection<CRResolvableBean> getObjects(final CRRequest request, final boolean doNavigation) throws CRException {
		//TODO add the possibility to filter the objects with the request
		return objects;
	}

	@Override
	public void finalize() {
	}

	/**
	 * @param newObjects - collection of resolvables that should be returned, as this one is used in 
	 * all threads it should be thread safe if you plan to manipulate in any way.
	 */
	public static void setObjects(final Collection<CRResolvableBean> newObjects) {
		objects = newObjects;
	}

}
