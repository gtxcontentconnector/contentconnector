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
	
	public MapWrapper(Map<Object,Object> map)
	{
		this.map=map;
	}
	
	
	@Override
	public Object get(String key) {
		return map.get(key);
		
	}

}
