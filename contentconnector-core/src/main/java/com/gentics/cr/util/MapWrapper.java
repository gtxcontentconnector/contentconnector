package com.gentics.cr.util;

import java.util.Map;
/**
 * 
 * Last changed: $Date: 2010-04-01 15:24:41 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 543 $
 * @author $Author: supnig@constantinopel.at $
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
