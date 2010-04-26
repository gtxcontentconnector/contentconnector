package com.gentics.cr.configuration;

import java.util.Hashtable;

import org.apache.log4j.Logger;

import com.gentics.cr.CRConfigFileLoader;
/**
 * 
 * Last changed: $Date: 2009-09-02 17:57:48 +0200 (Mi, 02 Sep 2009) $
 * @version $Revision: 180 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class StaticConfigurationContainer {
	
	private static Logger log = Logger.getLogger(StaticConfigurationContainer.class);
	private static Hashtable<String,CRConfigFileLoader> configmap = new Hashtable<String,CRConfigFileLoader>(2);
	
	/**
	 * Fetches a already created instance of the requested config. If config was not created yet, one will be instantiated.
	 * @param key
	 * @param webapproot
	 * @return
	 */
	public static CRConfigFileLoader getConfig(String key, String webapproot){
		return StaticConfigurationContainer.getConfig(key, webapproot, "");
	}
	
	/**
	 * Gets configuration
	 * @param key
	 * @param webapproot
	 * @param subdir
	 * @return
	 */
	public static CRConfigFileLoader getConfig(String key, String webapproot, String subdir)
	{
		CRConfigFileLoader config = configmap.get(key);
		if(config==null)
		{
			log.debug("Config not found, will create new config instance.");
			config = new CRConfigFileLoader(key,webapproot,subdir);
			if(config!=null)
			{
				configmap.put(key, config);
			}
		}
		
		
		return config;
	}
}
