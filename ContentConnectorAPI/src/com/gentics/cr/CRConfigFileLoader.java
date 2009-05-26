 package com.gentics.cr;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.log4j.Logger;

import com.gentics.cr.util.CRUtil;
/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class CRConfigFileLoader extends CRConfigUtil {

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
	@SuppressWarnings("unchecked")
	public CRConfigFileLoader(String name, String webapproot) {

		super();
		this.instancename = name;
		this.webapproot = webapproot;
		Properties props = new Properties();
		
		try {
			//LOAD CACHE CONFIGURATION
			String confpath = CRUtil.resolveSystemProperties("${com.gentics.portalnode.confpath}/cache.ccf");
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
		
		try {
			//LOAD SERVLET CONFIGURATION
			
			this.setName(this.instancename);
			String confpath = CRUtil.resolveSystemProperties("${com.gentics.portalnode.confpath}/rest/"+this.getName()+".properties");
			
			props.load(new FileInputStream(confpath));
			
			for (Iterator i = props.entrySet().iterator() ; i.hasNext() ; ) {
				Map.Entry entry = (Entry) i.next();
				Object value = entry.getValue();
				Object key = entry.getKey();
				//this.filterInitProperties(key, value);
				this.setProperty((String)key, (String)value);
			}
			
		} catch (FileNotFoundException e1) {
			log.error("Could not load configuration file at: "+CRUtil.resolveSystemProperties("${com.gentics.portalnode.confpath}/rest/"+this.getName()+".properties")+"!");
		} catch (IOException e1) {
			log.error("Could not load configuration file at: "+CRUtil.resolveSystemProperties("${com.gentics.portalnode.confpath}/rest/"+this.getName()+".properties")+"!");
		}catch(NullPointerException e){
			log.error("Could not load configuration file at: "+CRUtil.resolveSystemProperties("${com.gentics.portalnode.confpath}/rest/"+this.getName()+".properties")+"!\r\n");
			e.printStackTrace();
		}
		
		// INITIALIZE DATASOURCE WITH HANDLE_PROPS AND DSPROPS
		initDS(handle_props,dsprops);

	}
	
	/**
	 * Define where to put init variables
	 * @param name
	 * @param value
	 */
	protected void filterInitProperties(Object name, Object value)
	{
		if (value instanceof String) {
			//RESOLVE SYSTEM PROPERTIES
			String newvalue = CRUtil.resolveSystemProperties((String)value);
			String key = name.toString();
						
			log.debug("Checking property '" + key + "': "+newvalue);
			
			//FILTER INIT PROPERTIES
			if(key.toUpperCase().startsWith("CACHE"))
			{
				dsprops.put(key, newvalue);
			}else if (key.equalsIgnoreCase("URL")) {
				// the parameter url contains the database url. In order to
				// distribute a hqsql database a relative path is needed.
				// Therefor $webapproot may be used in the url string which
				// points to the root dir of the webapp.
				String url = newvalue;
				url = url.replaceAll("\\$\\{webapproot\\}", this.webapproot.replace('\\', '/'));
				this.handle_props.put("url", url);
				log.debug("Resolved property '" + key + "' to " + url);

			}else if(!setProperty(key,newvalue)){
				// all oter properties may be Datasource Properties and therefore are passed through handle_props
				this.handle_props.put(key, newvalue);
				log.debug("Property '" + key + "' passed to Datasource as '"+newvalue+"'.");
			}
		}	
	}

	
}
