package com.gentics.cr.util;
/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class BeanWrapper extends ResolvableWrapper {
    private Object bean;

    /**
     * Create new Instance and wrap Bean
     * @param bean
     */
    public BeanWrapper(Object bean) {
    	this.bean=bean;
    }

    /**
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
