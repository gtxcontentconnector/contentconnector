package com.gentics.cr.util.request;
/**
 * This Class can be used to implement wrappers to wrap ServletRequests and PortletRequests to fetch the request parameters
 * 
 * 
 * Last changed: $Date: 2010-04-01 15:24:41 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 543 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public interface IParameterRequestWrapper {
	
	public Object getParameter(String key);
	
	public String[] getParameterValues(String key);

}
