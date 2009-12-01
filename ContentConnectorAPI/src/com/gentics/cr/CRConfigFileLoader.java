 package com.gentics.cr;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.gentics.cr.configuration.ConfigurationSettings;
import com.gentics.cr.configuration.EnvironmentConfiguration;
import com.gentics.cr.util.CRUtil;
/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class CRConfigFileLoader extends CRConfigUtil {

	/**
	 * 
	 */
	private static final long serialVersionUID = -87744244157623456L;
	private static Logger log = Logger.getLogger(CRConfigFileLoader.class);
	private String instancename;
	private String webapproot;
	protected Properties dsprops = new Properties();
	protected Properties handle_props = new Properties();
	protected Properties cache_props = new Properties();
	
	/**
	 * Create new instance of CRConfigFileLoader
	 * @param name of config
	 * @param webapproot root directory of application (config read fallback)
	 */
	public CRConfigFileLoader(String name, String webapproot) {
		this(name, webapproot, "");
	}
	
	/**
	 * Load config from String with subdir
	 * @param name
	 * @param webapproot
	 * @param subdir
	 */
	public CRConfigFileLoader(String name, String webapproot, String subdir) {

		super();
		this.instancename = name;
		this.webapproot = webapproot;
		
		//Load Environment Properties
		EnvironmentConfiguration.loadEnvironmentProperties();
		
		this.setName(this.instancename);
		
		//LOAD DEFAULT CONFIGURATION
		loadConfigFile("${com.gentics.portalnode.confpath}/rest/"+subdir+this.getName()+".properties");
		
		//LOAD ENVIRONMENT SPECIFIC CONFIGURATION
		String modePath = ConfigurationSettings.getConfigurationPath();
		if(modePath!=null && !"".equals(modePath))
		{
			loadConfigFile("${com.gentics.portalnode.confpath}/rest/"+subdir+modePath+this.getName()+".properties");
		}
		
		// INITIALIZE DATASOURCE WITH HANDLE_PROPS AND DSPROPS
		initDS();

	}
	
	private void loadConfigFile(String path)
	{
		Properties props = new Properties();
		try {
			//LOAD SERVLET CONFIGURATION
			String confpath = CRUtil.resolveSystemProperties(path);
			
			props.load(new FileInputStream(confpath));
			
			for (Entry<Object,Object> entry:props.entrySet()) {
				Object value = entry.getValue();
				Object key = entry.getKey();
				setProperty((String)key, (String)value);
			}
			
		} catch (FileNotFoundException e1) {
			log.error("Could not load configuration file at: "+CRUtil.resolveSystemProperties("${com.gentics.portalnode.confpath}/rest/"+this.getName()+".properties")+"!");
			System.out.println(e1.getMessage());
			e1.printStackTrace();
			
		} catch (IOException e1) {
			log.error("Could not load configuration file at: "+CRUtil.resolveSystemProperties("${com.gentics.portalnode.confpath}/rest/"+this.getName()+".properties")+"!");
			System.out.println(e1.getMessage());
			e1.printStackTrace();
		}catch(NullPointerException e){
			log.error("Could not load configuration file at: "+CRUtil.resolveSystemProperties("${com.gentics.portalnode.confpath}/rest/"+this.getName()+".properties")+"!\r\n");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets and prepares the properties
	 * @param key
	 * @param value
	 */
	protected void setProperty(String key, String value)
	{
		//Resolve system properties, so that they can be used in config values
		value = CRUtil.resolveSystemProperties((String)value);
		
		//Replace webapproot in the properties values, so that this variable can be used
		if(this.webapproot!=null)
		{
			value = resolveProperty("\\$\\{webapproot\\}", this.webapproot.replace('\\', '/'), value);
		}
		
		//Set the property
		set(key,value);
		log.debug("CONFIG: "+key+" has been set to "+value);
	}
	
	
    
    protected String resolveProperty(String pattern, String replacement, String value)
    {
    	value = value.replaceAll(pattern,replacement);
    	return(value);
    }
    
		
}
