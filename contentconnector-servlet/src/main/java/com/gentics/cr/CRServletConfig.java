package com.gentics.cr;

import javax.servlet.ServletConfig;

/**
 * 
 * @author Christopher
 *
 */
public class CRServletConfig extends CRConfigFileLoader {

	/**
	 * generated serial version unique id.
	 */
	private static final long serialVersionUID = 5615582224307130579L;

	/**
	 * TODO javadoc
	 * @param config TODO javadoc
	 */
	public CRServletConfig(ServletConfig config) {
		super(config.getServletName(), config.getServletContext().getRealPath(""));
	}

}
