package com.gentics.cr.util;

import java.util.Map;
/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class MapWrapper extends ResolvableWrapper {
	private Map<Object,Object> map;
	
	/**
	 * Create new instance and wrap a map
	 * @param map
	 */
	public MapWrapper(Map<Object,Object> map)
	{
		this.map=map;
	}
	
	
	/**
	 * get property to given key
	 * @param key 
	 * @return 
	 */
	public Object get(String key) {
		return map.get(key);
		
	}

}
