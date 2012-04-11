package com.gentics.cr.util.resolver;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;

import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.configuration.StaticConfigurationContainer;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.util.StringUtils;

/**
 * Callback to resolve a content connector property.
 * @author bigbear3001
 */
public class ContentConnectorCallback implements Callback {

	/**
	 * Log4j logger for error and debug messages.
	 */
	private Logger logger = Logger.getLogger(ContentConnectorCallback.class);

	/**
	 * configuration cache.
	 */
	private Map<String, CRConfigUtil> configs = new Hashtable<String, CRConfigUtil>();

	/**
	 * List to prevent recursions when resolving.
	 */
	private List<String> initConfigs = new Vector<String>();

	/**
	 * {@inheritDoc}
	 */
	public final String getProperty(final Matcher m) {
		String propertyFileName = m.group(1);
		String propertyName = m.group(2);
		CRConfigUtil config;
		try {
			config = getConfig(propertyFileName);
			return config.getString(propertyName);
		} catch (CRException e) {
			logger.error("Erro while getting configuration. Maybe you got" + " recursive property definitions.", e);
		}
		return null;
	}

	/**
	 * @param configName - configuration name
	 * @return configuration with the specified name
	 * @throws CRException if there was a recursion while loading the
	 * configuration.
	 */
	private CRConfigUtil getConfig(final String configName) throws CRException {
		CRConfigUtil config;
		if (configs.containsKey(configName)) {
			config = configs.get(configName);
		} else if (!initConfigs.contains(configName)) {
			initConfigs.add(configName);
			config = StaticConfigurationContainer.getConfig(configName, "");
			configs.put(configName, config);
			initConfigs.remove(configName);
		} else {
			CRException e = new CRException();
			e.setMessage("Recursion in content connector properties detected. " + "Configs currently initialising: "
					+ StringUtils.getCollectionSummary(initConfigs));
			throw e;
		}
		return config;
	}

}
