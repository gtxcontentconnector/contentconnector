package com.gentics.cr.rendering.contentprocessor;

import java.util.Hashtable;
import java.util.Map;

import javax.portlet.PortletRequest;
import javax.servlet.ServletRequest;

import org.apache.log4j.Logger;

import com.gentics.cr.configuration.GenericConfiguration;


/**
 * 
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 545 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public abstract class ContentPostProcesser {
	protected GenericConfiguration conf = null;
	protected static Logger logger = Logger.getLogger("com.gentics.cr.rendering");
	
	/**
	 * 
	 * @param config
	 */
	public ContentPostProcesser (GenericConfiguration config) {
		this.conf = config;
	}
		
	/**
	 * Returns the processed String (implement the way, you want the string to
	 * be processed and changed, replaced, modified, etc)
	 * 
	 * @param obj
	 * @return
	 */
	public abstract String processString(String obj);
	/**
	 * Returns the processed String (implement the way, you want the string to
	 * be processed and changed, replaced, modified, etc)
	 * 
	 * @param obj
	 * @param request 
	 * @return
	 */
	public abstract String processString(String obj, PortletRequest request);
	/**
	 * Returns the processed String (implement the way, you want the string to
	 * be processed and changed, replaced, modified, etc)
	 * 
	 * @param obj
	 * @param request 
	 * @return
	 */
	public abstract String processString(String obj, ServletRequest request);
	
	
	private static final String FILTERCHAIN_KEY="filterchain";
	private static final String FILTERCHAIN_CLASS_KEY="processorclass";
	
	
	/**
	 * Create table of ContentTransformers configured in config
	 * @param config
	 * @return
	 */
	public static Hashtable<String,ContentPostProcesser> getProcessorTable(GenericConfiguration config)
	{
		GenericConfiguration tconf = null;
		
		/** Needn't be configured, if not set, dont use it */
		try{
			tconf = (GenericConfiguration)config.get(FILTERCHAIN_KEY);
		}catch(NullPointerException ex){
			return null;
		}
		
		if(tconf!=null)
		{
			Hashtable<String,GenericConfiguration> confs = tconf.getSubConfigs();
			if(confs!=null && confs.size()>0)
			{
				Hashtable<String,ContentPostProcesser> ret = new Hashtable<String,ContentPostProcesser>(confs.size());
				int i = 0; 
				for(Map.Entry<String,GenericConfiguration> e:confs.entrySet())
				{
					GenericConfiguration c = e.getValue();
					String filterchainClass = (String)c.get(FILTERCHAIN_CLASS_KEY);
					try
					{
						
						ContentPostProcesser t = null;
						t = (ContentPostProcesser) Class.forName(filterchainClass).getConstructor(new Class[] {GenericConfiguration.class}).newInstance(c);
						if(t!=null)
						{
							ret.put(i+"", t);
							i++;
						}
					}
					catch(Exception ex)
					{
						logger.error("Invalid configuration found. Could not initiate the defined ContentPostProcessor Object ["+filterchainClass+"]");
						ex.printStackTrace();
					}
					
				}
				return(ret);
			}
		}
		
		return null;
	}
}
