package com.gentics.cr.rest;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.util.filter.Filter;

/**
 * A common interface for content repositories.
 * 
 * Holds a collection of CRResolvableBeans and each implementation of
 * ContentRepository is able to serialize the collection to the desired format.
 * 
 * ListRenderer.
 * 
 * Last changed: $Date$
 * 
 * @version $Revision$
 * @author $Author$
 * 
 * 
 */
public abstract class ContentRepository implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3367665528658725618L;
	
	/**
	 * property name for config to define if the metaresolvable should be deployed to the repository
	 */
	public static final String DEPLOYMETARESOLVABLE_KEY = "cr.deploymetaresolvable";

	private String[] attrArray;

	private String[] optionsArray;

	protected Vector<CRResolvableBean> resolvableColl;

	protected String response_encoding;

	/**
	 * Get responce encoding. Defaults to utf-8
	 * 
	 * @return
	 */
	public String getResponseEncoding() {
		if (this.response_encoding == null)
			return "utf-8";
		else
			return (this.response_encoding);
	}

	/**
	 * Sets the response encoding
	 * 
	 * @param encoding
	 */
	public void setResponseEncoding(String encoding) {
		this.response_encoding = encoding;
	}

	/**
	 * Set options array
	 * 
	 * @param optionsArray
	 */
	public void setOptionsArray(String[] optionsArray) {
		this.optionsArray = optionsArray;
	}

	/**
	 * Get Options Array
	 * 
	 * @return
	 */
	public String[] getOptionsArray() {
		return optionsArray;
	}

	/**
	 * returns true if this is the root repository and has no fathers
	 * 
	 * @return
	 */
	public boolean isRoot() {
		return (this.isRoot);
	}

	private boolean isRoot = true;
	protected static Logger log = Logger.getLogger(ContentRepository.class);

	/**
	 * Create instance
	 * 
	 * @param attr
	 */
	public ContentRepository(String[] attr) {
		this.resolvableColl = new Vector<CRResolvableBean>();
		this.attrArray = attr;
		this.response_encoding = "utf-8";
	}

	/**
	 * Create instance
	 * 
	 * @param attr
	 * @param encoding
	 */
	public ContentRepository(String[] attr, String encoding) {
		this.resolvableColl = new Vector<CRResolvableBean>();
		this.attrArray = attr;
		this.response_encoding = encoding;
	}

	/**
	 * create instance
	 * 
	 * @param attr
	 * @param encoding
	 * @param options
	 */
	public ContentRepository(String[] attr, String encoding, String[] options) {
		this.resolvableColl = new Vector<CRResolvableBean>();
		this.attrArray = attr;
		this.response_encoding = encoding;
		this.setOptionsArray(options);
	}

	/**
	 * Create instance
	 * 
	 * @param attr
	 * @param root
	 */
	public ContentRepository(String[] attr, Object root) {
		this.resolvableColl = new Vector<CRResolvableBean>();
		this.attrArray = attr;
		this.isRoot = false;
		// this.log.setLevel(Level.DEBUG);
	}

	/**
	 * Gets the contenttype as string "text/plain"
	 * 
	 * @return
	 */
	public String getContentType() {
		return "text/plain";
	}

	/**
	 * Add a resolvable bean
	 * 
	 * @param resolvableBean
	 */
	public void addObject(CRResolvableBean resolvableBean) {
		this.resolvableColl.add(resolvableBean);
	}

	/**
	 * add all given objects to the ContentRepository
	 * 
	 * @param objects
	 *            Collection of CRResolvableBeans to be added
	 */
	public void addObjects(Collection<CRResolvableBean> objects) {
		this.resolvableColl.addAll(objects);
	}

	/**
	 * Writes repository to a stream
	 * 
	 * @param stream
	 * @throws CRException
	 */
	public abstract void toStream(OutputStream stream) throws CRException;

	/**
	 * Responds with an Error to the stream
	 * 
	 * @param stream
	 * @param ex
	 * @param isDebug
	 */
	public abstract void respondWithError(OutputStream stream, CRException ex,
			boolean isDebug);

	/**
	 * Apply Filters on the ContentRepository
	 * 
	 * @param crConf
	 * @return
	 */
	public boolean applyFilters(CRConfig crConf) {
		return applyFilters(crConf, null);
	}

	/**
	 * Apply Filters
	 * 
	 * @param crConf
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean applyFilters(CRConfig crConf, Object request) {
		Iterator<String> filterIterator = crConf.getFilterChain().iterator();
		while (filterIterator.hasNext()) {
			String filterClassName = filterIterator.next();
			Filter<CRResolvableBean> filter;
			try {
				HashMap<String, Object> filterParameters = new HashMap<String, Object>();
				filterParameters.put("crConf", crConf);
				filterParameters.put("request", request);
				filter = (Filter<CRResolvableBean>) Class.forName(
						filterClassName).getConstructor().newInstance();
				Collection<CRResolvableBean> returnedObjects = filter.apply(
						getObjects(), filterParameters);
				if (returnedObjects == null) {
					log
							.error("Filter "
									+ filterClassName
									+ " doesn't return a valid result. The result was null.\nTo prevent from errors the result of the filter is not saved.");
					return false;
				}
				setObjects(returnedObjects);
				return true;
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				log.error("Cannot find Class for filer: " + filterClassName);
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Get attribute array
	 * 
	 * @return
	 */
	public String[] getAttrArray() {
		return attrArray;
	}

	/**
	 * Serialize Object
	 * 
	 * @param obj
	 * @return
	 * @throws java.io.IOException
	 */
	public byte[] getBytes(Object obj) throws java.io.IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(obj);
		oos.flush();
		oos.close();
		bos.close();
		byte[] data = bos.toByteArray();
		return data;
	}

	/**
	 * Get contained Objects
	 * 
	 * @return
	 */
	public Collection<CRResolvableBean> getObjects() {
		return (Collection<CRResolvableBean>) resolvableColl;
	}

	/**
	 * Replaces objects in the ContentRepository with the objects in the given
	 * collection
	 * 
	 * @param objects
	 *            Collection of CRResolveableBeans which overrides the objects
	 *            in the ContentRepository
	 * 
	 * throws NullPointerException when objects is null
	 */
	private void setObjects(Collection<CRResolvableBean> objects) {
		if (objects == null) {
			log
					.error("Cannot set objects when i get a null value in setObjects(Collection objects)");
			throw new NullPointerException(
					"Cannot set objects when i get a null value");
		}
		resolvableColl = (Vector<CRResolvableBean>) objects;

	}

}
