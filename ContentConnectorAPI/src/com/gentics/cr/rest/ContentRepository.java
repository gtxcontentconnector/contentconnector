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
import com.gentics.cr.CRException;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.util.filter.Filter;

/**
 * A common interface for content repositories.
 * @author haymo
 *
 */
public abstract class ContentRepository implements Serializable {

	private static final long serialVersionUID = 1L;

	private String[] attrArray;
	
	private String[] optionsArray;
	
	//TODO CHECK WHY VECTOR IS USED HERE
	public Vector<CRResolvableBean> resolvableColl;
	
	private String response_encoding;
	
	public String getResponseEncoding()
	{
		if(this.response_encoding==null)
			return "utf-8";
		else
			return(this.response_encoding);
	}
	public void setResponseEncoding(String encoding)
	{
		this.response_encoding=encoding;
	}
	
	
	
	public void setOptionsArray(String[] optionsArray) {
		this.optionsArray = optionsArray;
	}
	public String[] getOptionsArray() {
		return optionsArray;
	}
	
	public boolean isRoot()
	{
		return(this.isRoot);
	}


	private boolean isRoot=true;
	public Logger log;
	
	public ContentRepository(String[] attr) {
		this.resolvableColl = new Vector<CRResolvableBean>();
		this.attrArray = attr;
		this.response_encoding="utf-8";
		this.log = Logger.getLogger("com.gentics.cr");
	}
	
	public ContentRepository(String[] attr, String encoding) {
		this.resolvableColl = new Vector<CRResolvableBean>();
		this.attrArray = attr;
		this.response_encoding=encoding;
		this.log = Logger.getLogger("com.gentics.cr");
	}
	
	public ContentRepository(String[] attr, String encoding, String[] options)
	{
		this.resolvableColl = new Vector<CRResolvableBean>();
		this.attrArray = attr;
		this.response_encoding=encoding;
		this.setOptionsArray(options);
		this.log = Logger.getLogger("com.gentics.cr");
	}
	
	public ContentRepository(String[] attr, Object root)
	{
		this.resolvableColl = new Vector<CRResolvableBean>();
		this.attrArray = attr;
		this.isRoot=false;
		this.log = Logger.getLogger("com.gentics.cr");
		//this.log.setLevel(Level.DEBUG);
	}

	public String getContentType() {
		return "text/plain";
	}

	public void addObject(CRResolvableBean resolvableBean) {
		this.resolvableColl.add(resolvableBean);
	}
	/**
	 * add all given objects to the ContentRepository
	 * @param objects: Collection of CRResolvableBeans to be added
	 */
	public void addObjects(Collection<CRResolvableBean> objects){
		this.resolvableColl.addAll(objects);
	}

	public void toStream(OutputStream stream) throws CRException{
	}
	
	public void respondWithError(OutputStream stream,CRException ex, boolean isDebug){
		
	}
	
	public boolean applyFilters(CRConfig crConf){
		return applyFilters(crConf, null);
	}
	
	@SuppressWarnings("unchecked")
	public boolean applyFilters(CRConfig crConf, Object request){
		Iterator<String> filterIterator = crConf.getFilterChain().iterator();
		while(filterIterator.hasNext()){
			String filterClassName = filterIterator.next();
			Filter<CRResolvableBean> filter;
			try {
				HashMap<String, Object> filterParameters = new HashMap<String, Object>();
				filterParameters.put("crConf", crConf);
				filterParameters.put("request", request);
				filter = (Filter<CRResolvableBean>) Class.forName(filterClassName).getConstructor().newInstance();
				Collection<CRResolvableBean> returnedObjects = filter.apply(getObjects(), filterParameters);
				if(returnedObjects == null){
					this.log.error("Filter "+filterClassName+" doesn't return a valid result. The result was null.\nTo prevent from errors the result of the filter is not saved.");
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
				log.error("Cannot find Class for filer: "+filterClassName);
				e.printStackTrace();
			}
		}
		return false;
	}

	public String[] getAttrArray() {
		return attrArray;
	}
	
	public byte[] getBytes(Object obj) throws java.io.IOException{
	      ByteArrayOutputStream bos = new ByteArrayOutputStream();
	      ObjectOutputStream oos = new ObjectOutputStream(bos);
	      oos.writeObject(obj);
	      oos.flush();
	      oos.close();
	      bos.close();
	      byte [] data = bos.toByteArray();
	      return data;
	  }

	public Collection<CRResolvableBean> getObjects() {
		return (Collection<CRResolvableBean>) resolvableColl;
	}
	/**
	 * Replaces objects in the ContentRepository with the objects in the given collection
	 * @param objects: Collection of CRResolveableBeans which overrides the objects in the ContentRepository
	 * 
	 * throws NullPointerException when objects is null
	 */
	private void setObjects(Collection<CRResolvableBean> objects) {
		if(objects==null){
			this.log.error("Cannot set objects when i get a null value in setObjects(Collection objects)");
			throw new NullPointerException("Cannot set objects when i get a null value");
		}
		resolvableColl = (Vector<CRResolvableBean>) objects;
		
	}
	
}
