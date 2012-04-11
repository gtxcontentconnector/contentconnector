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
	private GenericConfigurationFactory() {
	}

	/**
	 * Creates a <code>GenericConfiguration</code> instance from a Map.
	 * 
	 * @param configMap
	 *            Map that will be used.
	 * @return created GenericConfiguration instance
	 */
	public static GenericConfiguration createFromMap(final Map<String, String> configMap) {
		return GenericConfigurationFactory.createFromMap(configMap, null);
	}

	/**
	 * Creates a <code>GenericConfiguration</code> instance from a Map.
	 * 
	 * @param configMap
	 *            Map that will be used.
	 * @param keyHandling
	 *            Defines in which way the keys should be stored in the
	 *            configuration.
	 * @return created GenericConfiguration instance
	 */
	public static GenericConfiguration createFromMap(final Map<String, String> configMap,
			final GenericConfiguration.KeyConversion keyHandling) {
		GenericConfiguration conf = new GenericConfiguration();
		if (keyHandling != null) {
			conf.setKeyConversion(keyHandling);
		}

		for (Entry<String, String> e : configMap.entrySet()) {
			String value = CRUtil.resolveSystemProperties(e.getValue());
			conf.set(e.getKey(), value);
		}
		return conf;
	}

	/**
	 * Creates a <code>GenericConfiguration</code> instance from
	 * <code>Properties</code>.
	 * 
	 * @param props
	 *            Map that will be used.
	 * @return created GenericConfiguration instance
	 */
	public static GenericConfiguration createFromProperties(final Properties props) {
		return GenericConfigurationFactory.createFromProperties(props, null);
	}

	/**
	 * Creates a <code>GenericConfiguration</code> instance from
	 * <code>Properties</code>.
	 * 
	 * @param props
	 *            Map that will be used.
	 * @param keyHandling
	 *            Defines in which way the keys should be stored in the
	 *            configuration.
	 * @return created GenericConfiguration instance
	 */
	public static GenericConfiguration createFromProperties(final Properties props,
			final GenericConfiguration.KeyConversion keyHandling) {
		GenericConfiguration conf = new GenericConfiguration();
		if (keyHandling != null) {
			conf.setKeyConversion(keyHandling);
		}

		for (Entry<Object, Object> e : props.entrySet()) {
			String value = CRUtil.resolveSystemProperties((String) e.getValue());
			conf.set((String) e.getKey(), value);
		}
		return conf;
	}

	/**
	 * Creates a <code>GenericConfiguration</code> instance from a String.
	 * 
	 * @param configurationString
	 *            String that will be used.
	 * @return created GenericConfiguration instance
	 * @throws IOException
	 *             if no stream could be created from the String
	 */
	public static GenericConfiguration createFromString(final String configurationString) throws IOException {
		return GenericConfigurationFactory.createFromString(configurationString, null);
	}

	/**
	 * Creates a <code>GenericConfiguration</code> instance from a String.
	 * 
	 * @param configurationString
	 *            String that will be used.
	 * @param keyHandling
	 *            Defines in which way the keys should be stored in the
	 *            configuration.
	 * @return created GenericConfiguration instance
	 * @throws IOException
	 *             if no stream could be created from the String
	 */
	public static GenericConfiguration createFromString(final String configurationString,
			final GenericConfiguration.KeyConversion keyHandling) throws IOException {
		if (configurationString != null) {
			Properties props = new Properties();
			props.load(new ByteArrayInputStream(configurationString.getBytes()));

			return createFromProperties(props, keyHandling);
		}
		return null;
	}
}
