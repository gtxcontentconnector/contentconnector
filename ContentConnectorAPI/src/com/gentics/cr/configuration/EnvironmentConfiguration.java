package com.gentics.cr.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.gentics.cr.util.CRUtil;



/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class EnvironmentConfiguration {
	
	private static final String LOGGER_FILE_PATH = "${com.gentics.portalnode.confpath}/nodelog.properties";
	private static final String CACHE_FILE_PATH = "${com.gentics.portalnode.confpath}/cache.ccf";
	
	private static Logger log = Logger.getLogger(EnvironmentConfiguration.class);
	
	/**
	 * Load Environment Properties
	 * 		- load logger properties for log4j
	 * 		- load chache properties for JCS
	 */
	public static void loadEnvironmentProperties()
	{
		loadLoggerPropperties();
		loadCacheProperties();
	}
	
	/**
	 * Load Property file for Log4J
	 */
	public static void loadLoggerPropperties()
	{
		Properties logprops = new Properties();
		try {
			if(CRUtil.resolveSystemProperties("${com.gentics.portalnode.confpath}").equals("")){
				System.setProperty("com.gentics.portalnode.confpath", System.getProperty("catalina.base")+File.separator+"conf"+File.separator+"gentics"+File.separator);
			}
			String confpath = CRUtil.resolveSystemProperties(LOGGER_FILE_PATH);
			//System.out.println("TRYING TO LOAD NODELOGPROPS FROM: "+confpath);
			logprops.load(new FileInputStream(confpath));
			PropertyConfigurator.configure(logprops);
		} catch (IOException e) {
			log.error("Could not find nodelog.properties.");
			//e.printStackTrace();
		}catch (NullPointerException e) {
			log.error("Could not find nodelog.properties.");
			//e.printStackTrace();
		}
	}
	
	/**
	 * Load Property file for JCS chache
	 */
	public static void loadCacheProperties()
	{
		try {
			//LOAD CACHE CONFIGURATION
			String confpath = CRUtil.resolveSystemProperties(CACHE_FILE_PATH);
			Properties cache_props = new Properties();
			cache_props.load(new FileInputStream(confpath));
			CompositeCacheManager cManager = CompositeCacheManager.getUnconfiguredInstance();
			cManager.configure(cache_props);
		} catch(NullPointerException e){
			log.error("Could not load cache configuration. Perhaps you are missing the file cache.ccf in "+CRUtil.resolveSystemProperties("${com.gentics.portalnode.confpath}/")+"!");
		} catch (FileNotFoundException e) {
			log.error("Could not load cache configuration. Perhaps you are missing the file cache.ccf in "+CRUtil.resolveSystemProperties("${com.gentics.portalnode.confpath}/")+"!");
		} catch (IOException e) {
			log.error("Could not load cache configuration. Perhaps you are missing the file cache.ccf in "+CRUtil.resolveSystemProperties("${com.gentics.portalnode.confpath}/")+"!");
		}
	}
}
