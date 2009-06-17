package com.gentics.cr.configuration;

import com.gentics.cr.util.CRUtil;

/**
 * 
 * Last changed: $Date: 2009-06-09 14:33:13 +0200 (Di, 09 Jun 2009) $
 * @version $Revision: 79 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class ConfigurationSettings {
	
	private static final String CONFIGURATION_PROPERTY_NAME="com.gentics.portalnode.confmode";

	/**
	 * Gets the configuration path set for this environment
	 * @return configuration path as String
	 * 			- returns "" when configuration mode is not set
	 * 			- configuration path could be e.g. "dev/", "test/", "prod/" or something similar 
	 */
	public static String getConfigurationPath()
	{
		String path="";
		path = getConfigurationMode();
		if(path!=null && !path.equals(""))
		{
			return(path+"/");
		}
		return("");
	}
	
	/**
	 * Gets the configuration mode set for this environment
	 * @return configuration mode as String
	 */
	public static String getConfigurationMode()
	{
		String mode="";
		mode = CRUtil.resolveSystemProperties("${"+CONFIGURATION_PROPERTY_NAME+"}");
		return(mode);
	}
}
