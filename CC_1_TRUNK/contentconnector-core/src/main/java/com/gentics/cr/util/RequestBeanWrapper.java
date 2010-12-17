package com.gentics.cr.util;

import java.util.Arrays;
import java.util.Map;

import javax.portlet.PortletRequest;
import javax.servlet.ServletRequest;

/**
 * Wrapper for requests.
 * Last changed: $Date: 2010-04-01 15:24:41 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 543 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class RequestBeanWrapper extends ResolvableWrapper {
    /**
     * wrapped object.
     */
	private Object bean;

    /**
     * Create new Instance and wrap Bean.
     * @param bean wrapped bean
     */
    public RequestBeanWrapper(final Object bean) {
    	this.bean = bean;
    }

    /**
     * @see com.gentics.api.lib.resolving.Resolvable#get(java.lang.String)
     */
    public Object get(String key) {
        if (bean instanceof ServletRequest 
        		&& "Parameters".equalsIgnoreCase(key)) {
        	return new SmartRequestParameterWrapper(
        			((ServletRequest)bean).getParameterMap());
        }
        if (bean instanceof PortletRequest 
        		&& "Parameters".equalsIgnoreCase(key)) {
        	return new SmartRequestParameterWrapper(
        			((PortletRequest)bean).getParameterMap());
        }
    	try {
            Object value = invokeGetter(bean, key);
            //if value is set then check for basic types otherwise wrap objects
            return WrapperUtil.resolveType(value);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Private wapper class for parameter map.
     * @author Christopher
     *
     */
    public class SmartRequestParameterWrapper {
    	/**
    	 * wrapped map.
    	 */
    	private Map<String, String[]> parameters;
    	
    	/**
    	 * constructor.
    	 * @param p
    	 */
    	public SmartRequestParameterWrapper(Map<String, String[]> p) {
    		this.parameters = p;
    	}
    	
    	/**
    	 * smartly get parameter
    	 * @param key
    	 * @return
    	 */
    	public Object get(String key) {
    		Object val = null;
    		if (this.parameters != null) {
    			String[] arr = this.parameters.get(key);
    			if (arr != null && arr.length == 1) {
    				val = arr[0];
    			} else {
    				val = arr;
    			}
    		}
    		return val;
    	}
    }

}
