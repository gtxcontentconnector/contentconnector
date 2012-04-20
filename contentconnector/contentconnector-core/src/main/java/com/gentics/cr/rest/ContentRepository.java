package com.gentics.cr.rest;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

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
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * 
 * @version $Revision: 545 $
 * @author $Author: supnig@constantinopel.at $
 * 
 * 
 */
public abstract class ContentRepository implements Serializable {

	/**
	 * Serial Id.
	 */
	private static final long serialVersionUID = -3367665528658725618L;

	/**
	 * property name for config to define if the metaresolvable should be deployed to the repository.
	 */
	public static final String DEPLOYMETARESOLVABLE_KEY = "cr.deploymetaresolvable";

	private String[] attrArray;

	private String[] optionsArray;

	protected Vector<CRResolvableBean> resolvableColl;

	protected String response_encoding;

	/**
	 * Holds objects that can later be deployed in the render context.
	 */
	private HashMap<String, Object> additinalDeployableObjects;

	/**
	 * Adds objects that can be deployed in the render context.
	 * @param objects objects to deploy
	 */
	public final void addAdditionalDeployableObjects(final Map<String, Object> objects) {
		if (additinalDeployableObjects == null) {
			additinalDeployableObjects = new HashMap<String, Object>();
		}
		additinalDeployableObjects.putAll(objects);
	}

	/**
	 * Add object that can be deployed in the render context.
	 * @param key key for deployment
	 * @param value deployed object
	 */
	public final void addAdditionalDeployableObject(final String key, final Object value) {
		if (additinalDeployableObjects == null) {
			additinalDeployableObjects = new HashMap<String, Object>();
		}
		additinalDeployableObjects.put(key, value);
	}

	/**
	 * Returns all objects that were deployed to be put in the render context.
	 * @return objects
	 */
	public final HashMap<String, Object> getAdditionalDeployableObjects() {
		return additinalDeployableObjects;
	}

	/**
	 * Get responce encoding. Defaults to utf-8
	 * 
	 * @return encoding
	 */
	public final String getResponseEncoding() {
		if (this.response_encoding == null) {
			return "utf-8";

		} else {
			return this.response_encoding;
		}
	}

	/**
	 * Sets the response encoding.
	 * 
	 * @param encoding encoding
	 */
	public final void setResponseEncoding(final String encoding) {
		this.response_encoding = encoding;
	}

	/**
	 * Set options array.
	 * 
	 * @param optionsArray the array to be set.
	 */
	public final void setOptionsArray(final String[] optionsArray) {
		this.optionsArray = optionsArray;
	}

	/**
	 * Get Options Array.
	 * 
	 * @return options array
	 */
	public final String[] getOptionsArray() {
		return optionsArray;
	}

	/**
	 * Method to determine if this repository is the root repository. 
	 * 
	 * @return true if this is the root repository and has no fathers.
	 */
	public final boolean isRoot() {
		return this.isRoot;
	}

	/**
	 * root element.
	 */
	private boolean isRoot = true;

	/**
	 * Log4J logger.
	 */
	protected static Logger log = Logger.getLogger(ContentRepository.class);

	/**
	 * Create instance.
	 * 
	 * @param attr
	 */
	public ContentRepository(String[] attr) {
		this.resolvableColl = new Vector<CRResolvableBean>();
		this.attrArray = attr;
		this.response_encoding = "utf-8";
	}

	/**
	 * Create instance.
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
	 * create instance.
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
	 * Create instance.
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
	 * Gets the contenttype as string "text/plain".
	 * 
	 * @return
	 */
	public String getContentType() {
		return "text/plain";
	}

	/**
	 * Add a resolvable bean.
	 * @param resolvableBean
	 */
	public void addObject(CRResolvableBean resolvableBean) {
		this.resolvableColl.add(resolvableBean);
	}

	/**
	 * add all given objects to the ContentRepository.
	 * 
	 * @param objects
	 *            Collection of CRResolvableBeans to be added
	 */
	public void addObjects(Collection<CRResolvableBean> objects) {
		this.resolvableColl.addAll(objects);
	}

	/**
	 * Writes repository to a stream.
	 * 
	 * @param stream
	 * @throws CRException
	 */
	public abstract void toStream(OutputStream stream) throws CRException;

	/**
	 * Responds with an Error to the stream.
	 * 
	 * @param stream
	 * @param ex
	 * @param isDebug
	 */
	public abstract void respondWithError(final OutputStream stream, final CRException ex, final boolean isDebug);

	/**
	 * Apply Filters on the ContentRepository.
	 * 
	 * @param crConf
	 * @return
	 */
	public boolean applyFilters(CRConfig crConf) {
		return applyFilters(crConf, null);
	}

	/**
	 * Apply Filters.
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
				filter = (Filter<CRResolvableBean>) Class.forName(filterClassName).getConstructor().newInstance();
				Collection<CRResolvableBean> returnedObjects = filter.apply(getObjects(), filterParameters);
				if (returnedObjects == null) {
					log.error("Filter "
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
				log.error("Cannot find Class for filer: " + filterClassName, e);
			}
		}
		return false;
	}

	/**
	 * Get attribute array.
	 * 
	 * @return
	 */
	public String[] getAttrArray() {
		return attrArray;
	}

	/**
	 * Serialize Object.
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
	 * @param index - index of the object to get from the ContentRepository
	 * @return object with the given index
	 */
	public final CRResolvableBean getObject(final int index) {
		return resolvableColl.get(index);
	}

	/**
	 * @return objects contained in the ContentRepository
	 */
	public Collection<CRResolvableBean> getObjects() {
		return resolvableColl;
	}

	/**
	 * @return number of objects contained in the ContentRepository
	 */
	public final int getSize() {
		return resolvableColl.size();
	}

	/**
	 * Remove the object with the given index from the ContentRepository.
	 * @param index - index of the object to remove
	 * @return the removed object if there was an object present at the index
	 */
	public final Object remove(final int index) {
		return resolvableColl.remove(index);
	}

	/**
	 * Replace the object at the given index with the given object.
	 * @param resolvableBean - object to set for the given index
	 * @param index - index to set the object
	 */
	public final void setObject(final int index, final CRResolvableBean resolvableBean) {
		this.resolvableColl.add(resolvableBean);
	}

	/**
	 * Replaces objects in the ContentRepository with the objects in the given
	 * collection.
	 * @param objects
	 *            Collection of CRResolveableBeans which overrides the objects
	 *            in the ContentRepository
	 * 
	 * throws NullPointerException when objects is null
	 */
	private void setObjects(final Collection<CRResolvableBean> objects) {
		if (objects == null) {
			log.error("Cannot set objects when i get a null value in setObjects(Collection objects)");
			throw new NullPointerException("Cannot set objects when i get a null value");
		}
		resolvableColl = (Vector<CRResolvableBean>) objects;

	}

	/**
	 * clear the given element from all attributes.
	 */
	protected void clearElement(Element elem) {
		if (elem != null) {
			NodeList list = elem.getChildNodes();

			//int len = list.getLength();
			for (int i = 0; i < list.getLength(); i++) {
				elem.removeChild(list.item(i));
			}
			NamedNodeMap map = elem.getAttributes();
			//len =map.getLength();
			for (int i = 0; i < map.getLength(); i++) {
				elem.removeAttribute(map.item(i).getNodeName());
			}
		}
	}

}
