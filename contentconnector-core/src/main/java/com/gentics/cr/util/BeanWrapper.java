package com.gentics.cr.util;
/**
 * 
 * Last changed: $Date: 2010-04-01 15:24:41 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 543 $
 * @author $Author: supnig@constantinopel.at $
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
