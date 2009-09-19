package com.gentics.cr.portlet;

import javax.portlet.PortletConfig;

import com.gentics.cr.CRConfigFileLoader;

public class CCPortletConfig extends CRConfigFileLoader {

	public CCPortletConfig(PortletConfig config) {
		super(config.getPortletName(), config.getPortletContext().getRealPath(""));
		
	}

}
