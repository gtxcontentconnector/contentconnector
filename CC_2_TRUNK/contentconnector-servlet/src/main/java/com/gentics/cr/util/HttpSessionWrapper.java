package com.gentics.cr.util;

import javax.servlet.http.HttpSession;
/**
 * 
 * Last changed: $Date: 2010-04-01 15:24:41 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 543 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class HttpSessionWrapper extends ResolvableWrapper {
	private HttpSession session;
	
	/**
	 * Create new instance
	 * @param session
	 */
	public HttpSessionWrapper(HttpSession session)
	{
		this.session=session;
	}
	
	
	/**
	 * 
	 * Get Property
	 * @param key 
	 * @return 
	 * 
	 */
	public Object get(String key) {
		try {
			Object value;
			if(key.equalsIgnoreCase("ATTRIBUTES"))
			{
				value = session.getAttribute(key);
			}
			else
			{
				value = invokeGetter(session, key);
			}
			//if value is set then check for basic types otherwise wrap objects
            return WrapperUtil.resolveType(value);
        } catch (Exception e) {
            return null;
        }
	}

}
