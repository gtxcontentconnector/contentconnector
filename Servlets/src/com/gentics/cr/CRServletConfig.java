package com.gentics.cr;

import javax.servlet.ServletConfig;


public class CRServletConfig extends CRConfigFileLoader {

	public CRServletConfig(ServletConfig config) {
		super(config.getServletName(),config.getServletContext().getRealPath(""));		
	}
	
}
