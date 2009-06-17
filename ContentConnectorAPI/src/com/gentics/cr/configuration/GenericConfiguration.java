package com.gentics.cr.configuration;

import java.util.HashMap;

/**
 * 
 * Last changed: $Date: 2009-06-09 14:33:13 +0200 (Di, 09 Jun 2009) $
 * @version $Revision: 79 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class GenericConfiguration {
	
	private HashMap<String,String> properties;
	private HashMap<String,GenericConfiguration> subconfigs;
	
	public GenericConfiguration()
	{
		
	}
	
	/**
	 * Gets the property to the given key
	 * 		- will resolve sub properties like "conf1.A.1.1" to
	 * 			this config
	 * 					-config config1
	 *	 					-config A
	 * 							-config 1
	 * 								-config 1
	 * 		- will resolve properties like "conf1" to 
	 * 			this config
	 * 					-property config1
	 * @param key
	 * @return property value as string
	 */
	public String get(String key)
	{
		if(isSubKey(key))
		{
			if(this.subconfigs!=null)
			{
				GenericConfiguration subConf = this.subconfigs.get(getSubConfigKey(key));
				if(subConf!=null)
				{
					return(subConf.get(getSubKey(key)));
				}
			}
		}
		else if(this.properties!=null)
		{
			return properties.get(key);
		}
		return null;
	}
	
	
	/**
	 * Sets the property value to the given key
	 * 		- will resolve sub properties like "conf1.A.1.1" to
	 * 			this config
	 * 					-config config1
	 *	 					-config A
	 * 							-config 1
	 * 								-config 1
	 * 		- will resolve properties like "conf1" to 
	 * 			this config
	 * 					-property config1
	 * @param key Property key as string
	 * @param value Property value as string
	 * 
	 */
	public void set(String key, String value)
	{
		if(isSubKey(key))
		{
			if(this.subconfigs==null)this.subconfigs = new HashMap<String,GenericConfiguration>();
			String confKey = getSubConfigKey(key);
			if(this.subconfigs.get(confKey)==null)this.subconfigs.put(confKey, new GenericConfiguration());
			this.subconfigs.get(confKey).set(getSubKey(key), value);
		}
		else
		{
			if(this.properties==null)this.properties = new HashMap<String,String>();
			this.properties.put(key, value);
		}
	}
	
	/**
	 * Returns true if key is resolving to a sub configuration
	 * @param key
	 * @return true if key is resolving to a sub configuration
	 */
	private boolean isSubKey(String key)
	{
		boolean ret = false;
		if(key!=null)
		{
			String[] arr = key.split(".");
			if(arr!=null && arr.length>1)
			{
				ret=true;
			}
		}
		return (ret);
	}
	
	/**
	 * Gets the sub configuration key
	 * @param key
	 * @return the sub configuration key as string, null if the key does not resolve to a sub configuration
	 */
	private String getSubKey(String key)
	{
		String sub = null;
		
		if(key!=null)
		{
			String[] arr = key.split(".");
			if(arr!=null && arr.length>1)
			{
				int firstpos = key.indexOf(".");
				sub = key.substring(firstpos);
			}
		}
		return(sub);
	}
	
	/**
	 * Gets the key under which the sub configuration is stored in the subconfigs hashmap
	 * @param key
	 * @return sub config key as string
	 */
	private String getSubConfigKey(String key)
	{
		String sub = null;
		
		if(key!=null)
		{
			String[] arr = key.split(".");
			if(arr!=null && arr.length>1)
			{
				sub=arr[0];
			}
		}
		return(sub);
	}
}
