package com.gentics.cr.portlet;

import javax.portlet.PortletConfig;

import com.gentics.cr.CRConfigFileLoader;
/**
 * 
 * Last changed: $Date: 2009-05-18 17:31:58 +0200 (Mo, 18 Mai 2009) $
 * @version $Revision: 27 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class CRPortletConfig extends CRConfigFileLoader {

	public CRPortletConfig(PortletConfig conf) {
		super(conf.getPortletName(), conf.getPortletContext().getRealPath(""));
	}

}
