package com.gentics.cr.util.response;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Christopher
 *
 */
public class ServletResponseTypeSetter implements IResponseTypeSetter{

	
	private HttpServletResponse response;
	
	/**
	 * Create new instance of ServletResponseTypeSetter
	 * @param response
	 */
	public ServletResponseTypeSetter(HttpServletResponse response)
	{
		this.response = response;
	}
	
	/**
	 * @param type - content type to be set
	 * 
	 */
	public void setContentType(String type) {
		this.response.setContentType(type);		
	}

}
