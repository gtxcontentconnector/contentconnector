package com.gentics.cr.portlet;

import javax.portlet.PortletConfig;

import com.gentics.cr.CRConfigFileLoader;
/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class CRPortletConfig extends CRConfigFileLoader {

	public CRPortletConfig(PortletConfig conf) {
		super(conf.getPortletName(), conf.getPortletContext().getRealPath(""));
	}

}
