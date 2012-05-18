package com.gentics.cr;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.gentics.cr.exceptions.CRException;

/**
 * Utility class to instanciate RequestProcessors from a {@link CRConfigUtil}.
 * Last changed: $Date: 2009-09-02 17:57:48 +0200 (Mi, 02 Sep 2009) $
 * @version $Revision: 180 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public final class StaticRPContainer {
	/**
	 * Log4j Logger for error and debug messages.
	 */
	private static final Logger LOGGER = Logger.getLogger(StaticRPContainer.class);

	/**
	 * Table with cached request processors.
	 */
	private static ConcurrentHashMap<String, RequestProcessor> rpmap = new ConcurrentHashMap<String, RequestProcessor>(2);

	/**
	 * private constructor to prevent instantiation.
	 */
	private StaticRPContainer() {
	}

	/**
	 * Fetches a already created instance of the request processor. If request
	 * processor was not created yet, one will be instantiated.
	 * @param config 
	 * @param rpnumber - number of the request processor.
	 * @return the RequestProcessor with the given number. 
	 * @throws CRException - if there was an error generating the
	 * RequestProcessor
	 */
	public static RequestProcessor getRP(final CRConfigUtil config, final int rpnumber) throws CRException {
		return getRP(config, rpnumber + "");
	}

	/**
	 * Fetches a already created instance of the request processor. If request
	 * processor was not created yet, one will be instantiated.
	 * @param config - config to create the request processor for.
	 * @param requestProcessorName - name of the request processor
	 * @return the RequestProcessor with the given number. 
	 * @throws CRException - if there was an error generating the
	 * RequestProcessor
	 */
	public static RequestProcessor getRP(final CRConfigUtil config, final String requestProcessorName)
			throws CRException {
		String key = config.getName() + ".RP." + requestProcessorName;
		RequestProcessor rp = rpmap.get(key);
		if (rp == null) {
			LOGGER.debug("RP not found. Creating new instance");
			rp = config.getNewRequestProcessorInstance(requestProcessorName);
			if (rp != null) {
				rpmap.put(key, rp);
			}
		}
		return rp;
	}
}
