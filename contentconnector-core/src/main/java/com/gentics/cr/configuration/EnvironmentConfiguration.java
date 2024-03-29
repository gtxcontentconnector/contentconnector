package com.gentics.cr.configuration;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.logging.log4j.core.config.Configurator;

import com.gentics.cr.util.CRUtil;
import com.gentics.lib.log.NodeLogger;

/**
 * Environment Configuration manages the configuration of log4j and jcs.
 * Last changed: $Date: 2011-11-28 09:44:25 +0100 (Mo, 28 Nov 2011) $
 * @version $LastChangedRevision: 1545 $
 * @author $Author: bigbear.ap@gmail.com $
 *
 */
public final class EnvironmentConfiguration {

	/**
	 * Path were the configuration files are found.
	 */
	private static String configurationPath = "${" + CRUtil.PORTALNODE_CONFPATH + "}";

	private final static String LOGCONFIG_FILENAME = "nodelog.yml";

	/**
	 * Path were the configuration files are found.
	 * This variable is deprecated. Use {@link #getConfigPath()} instead.
	 */
	@Deprecated
	public static final String CONFPATH = configurationPath;

	/**
	 * Path to the default log4j property file.
	 */
	private static String loggerFilePath = configurationPath + "/" + LOGCONFIG_FILENAME;
	
	/**
	 * Path to the fallback log4j property file.
	 */
	private static String loggerFallbackPath = "defaultconfiguration/" + LOGCONFIG_FILENAME;

	/**
	 * Default log4j logger.
	 */
	private static NodeLogger log;

	/**
	 * Detect if log4j property loading already failed, so we put out the error
	 * message only once.
	 */
	private static boolean loggerInitFailed = false;
	
	/**
	 * If the initialization of the logger fails, we load the default configuration
	 * and set this property to true.
	 */
	private static boolean loggerInitFallback = false;

	/**
	 * Detect if cache property loading already failed, so we put out the error
	 * message only once.
	 */
	private static boolean cacheInitFailed = false;
	
	/**
	 * If the initialization of the cache fails, we load the default configuration
	 * and set this property to true.
	 */
	private static boolean cacheInitFallback = false;

	/**
	 * private constructor to prevent initializing of the utility class.
	 */
	private EnvironmentConfiguration() {

	}

	/**
	 * Load Environment Properties.
	 *		 - load logger properties for log4j
	 *		 - load cache properties for JCS
	 */
	public static void loadEnvironmentProperties() {
		loadLoggerProperties();
		CRUtil.normalizeConfpath();
	}

	/**
	 * Load Property file for log4j and configure it.
	 */
	public static void loadLoggerProperties() {
		loggerInitFailed = false;
		String confpath = CRUtil.resolveSystemProperties(loggerFilePath);

		File logConfigFile = new File(confpath);
		StringBuilder errorMessage = new StringBuilder("Could not find " + LOGCONFIG_FILENAME + " at: ").append(confpath);
		if (logConfigFile.exists()) {
			try {
				loadLoggerProperties(logConfigFile.toURI());
			} catch (IOException e) {
				if (!loggerInitFailed) {
					System.out.println(errorMessage.toString());
					loggerInitFailed = true;
				}
				loadLoggerFallbackProperties();
			} catch (NullPointerException e) {
				if (!loggerInitFailed) {
					System.out.println(errorMessage.toString());
					loggerInitFailed = true;
				}
				loadLoggerFallbackProperties();
			}
		} else {
			if (!loggerInitFailed) {
				System.out.println(errorMessage.toString());
				loggerInitFailed = true;
			}
			loadLoggerFallbackProperties();
		}
	}

	private static void loadLoggerFallbackProperties() {
		URL loggerConfigUrl = EnvironmentConfiguration.class.getResource(loggerFallbackPath);
		if (loggerConfigUrl != null) {
			try {
				loadLoggerProperties(loggerConfigUrl.toURI());
				loggerInitFallback = true;
				log.debug("Loaded logger fallback configuration");
			} catch (IOException | URISyntaxException e) {
				System.out.println("Could not load logger fallback configuration.");
				e.printStackTrace();
			}
		} else {
			System.out.println("Could not load logger fallback configuration.");
		}
	}

	/**
	 * Load logger properties from an InputStream
	 * @param is
	 * @throws IOException
	 */
	private static void loadLoggerProperties(URI loggerConf)
			throws IOException {
		Configurator.reconfigure(loggerConf);
		log = NodeLogger.getNodeLogger(EnvironmentConfiguration.class);
	}

	/**
	 * @return <code>true</code> if the logger init has not (yet) failed, otherwhise false.
	 */
	public static boolean getLoggerState() {
		return !loggerInitFailed;
	}
	
	/**
	 * @return <code>true</code> if the logger init has failed and we loaded the default/fallback configuration.
	 */
	public static boolean isLoggerFallbackLoaded() {
		return loggerInitFallback;
	}

	/**
	 * detect if log4j was initialized properly.
	 * @return true if we got an log4j logger false otherwise
	 */
	private static boolean loggerInitialized() {
		return !(log == null);
	}

	/**
	 * Method to log an error message.
	 * @param message error message to log
	 */
	private static void logError(final String message) {
		if (!loggerInitialized()) {
			loadLoggerProperties();
		}
		if (loggerInitialized()) {
			log.error(message);
		} else {
			System.out.println(message);
		}

	}

	/**
	 * Method to log a debug message.
	 * @param message debug message to log
	 */
	private static void logDebug(final String message) {
		if (!loggerInitialized()) {
			loadLoggerProperties();
		}
		if (loggerInitialized()) {
			log.debug(message);
		} else {
			System.out.println(message);
		}
	}

	/**
	 * @return the current log4j property file path.
	 */
	public static String getLoggerConfigPath() {
		return loggerFilePath;
	}

	/**
	 * set a new logger configuration file path where the log4j properties file is located from .
	 * @param newLoggerFilePath - path for the log4j configuration file.
	 */
	public static void setLoggerConfigPath(final String newLoggerFilePath) {
		loggerFilePath = newLoggerFilePath;
	}

	/**
	 * set the path for the configuration files.
	 * @param configLocation - directory which contains the configuration files
	 */
	public static void setConfigPath(final String configLocation) {
		configurationPath = configLocation;
		System.setProperty(CRUtil.PORTALNODE_CONFPATH, configLocation);
		loggerFilePath = configurationPath + "/" + LOGCONFIG_FILENAME;
	}

	/**
	 * @return the path where the configuration files are loaded from.
	 */
	public static String getConfigPath() {
		return configurationPath;
	}
}
