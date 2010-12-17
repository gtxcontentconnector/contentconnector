package com.gentics.cr.configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import com.gentics.cr.util.CRUtil;

/**
 * 
 * @author Christopher
 *
 */
public final class GenericConfigurationFactory {

	/**
	 * Prevent instantiation.
	 */
	private GenericConfigurationFactory() { }
	
	/**
	 * Creates a <code>GenericConfiguration</code> instance from a Map.
	 * @param configMap Map that will be used.
	 * @return created GenericConfiguration instance
	 */
	public static GenericConfiguration createFromMap(final Map<String, String> 
					configMap) {
		GenericConfiguration conf = new GenericConfiguration();
		
		for (Entry<String, String> e : configMap.entrySet()) {
			String value = CRUtil.resolveSystemProperties(e.getValue());
			conf.set(e.getKey(), value);
		}
		return conf;
	}
	
	/**
	 * Creates a <code>GenericConfiguration</code> instance 
	 * from <code>Properties</code>.
	 * @param props Map that will be used.
	 * @return created GenericConfiguration instance
	 */
	public static GenericConfiguration createFromProperties(final 
			Properties props) {
		GenericConfiguration conf = new GenericConfiguration();
		
		for (Entry<Object, Object> e : props.entrySet()) {
			String value = CRUtil
				.resolveSystemProperties((String) e.getValue());
			conf.set((String) e.getKey(), value);
		}
		return conf;
	}
	
	/**
	 * Creates a <code>GenericConfiguration</code> instance from a String.
	 * @param configurationString String that will be used.
	 * @return created GenericConfiguration instance
	 * @throws IOException if no stream could be created from the String
	 */
	public static GenericConfiguration createFromString(final String 
			configurationString) throws IOException {
		if (configurationString != null) {
			Properties props = new Properties();
			props.load(new ByteArrayInputStream(
						configurationString.getBytes()));
			
			return createFromProperties(props);
		}
		return null;
	}
}
