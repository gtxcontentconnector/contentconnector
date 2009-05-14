package com.gentics.cr.util;




/*
 * @author norbert
 * @date 14.08.2008
 * @version $Id: $
 */

/**
 * @author norbert
 *
 */
public class BeanWrapper extends ResolvableWrapper {
    private Object bean;

    public BeanWrapper(Object bean) {
    	this.bean=bean;
    }

    
    
    
    /* (non-Javadoc)
     * @see com.gentics.api.lib.resolving.Resolvable#get(java.lang.String)
     */
    public Object get(String key) {
        try {
            Object value = invokeGetter(bean, key);
            //if value is set then check for basic types otherwise wrap objects
            return WrapperUtil.resolveType(value);
        } catch (Exception e) {
            return null;
        }
    }

}
