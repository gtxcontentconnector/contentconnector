package com.gentics.cr;

import javax.servlet.ServletConfig;

/**
 * 
 * @author Christopher
 *
 */
public class CRServletConfig extends CRConfigFileLoader {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5615582224307130579L;

	public CRServletConfig(ServletConfig config) {
		super(config.getServletName(),config.getServletContext().getRealPath(""));		
	}
	
}
