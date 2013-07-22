package com.gentics.cr.configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.gentics.cr.util.CRUtil;

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

	/**
	 * Path were the configuration files are found.
	 * This variable is deprecated. Use {@link #getConfigPath()} instead.
	 */
	@Deprecated
	public static final String CONFPATH = configurationPath;

	/**
	 * Path to the default log4j property file.
	 */
	private static String loggerFilePath = configurationPath + "/nodelog.properties";
	
	/**
	 * Path to the fallback log4j property file.
	 */
	private static String loggerFallbackPath = "defaultconfiguration/nodelog.properties";

	/**
	 * Path to the jcs configuration file.
	 */
	private static String cacheFilePath = configurationPath + "/cache.ccf";
	
	/**
	 * Path to the fallback jcs configuration file.
	 */
	private static String cacheFallbackPath = "defaultconfiguration/cache.ccf";
	

	/**
	 * Configuration key if we should use the same caches as Gentics
	 * Portal.Node.
	 */
	private static final String USE_PORTAL_CACHE_KEY = "com.gentics.cr.useportalcaches";

	/**
	 * Default log4j logger.
	 */
	private static Logger log;

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
		loadCacheProperties();
	}

	/**
	 * Load Property file for log4j and configure it.
	 */
	public static void loadLoggerProperties() {
		loggerInitFailed = false;
		String confpath = CRUtil.resolveSystemProperties(loggerFilePath);
		StringBuilder errorMessage = new StringBuilder("Could not find nodelog.properties at: ").append(confpath);
		try {
			InputStream is = new FileInputStream(confpath);
			loadLoggerProperties(is);
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
	}
	
	private static void loadLoggerFallbackProperties() {
		InputStream stream = EnvironmentConfiguration.class.getResourceAsStream(loggerFallbackPath);
		try {
			loadLoggerProperties(stream);
			loggerInitFallback = true;
			log.debug("Loaded logger fallback configuration");
		} catch (IOException e) {
			System.out.println("Could not load logger fallback configuration.");
		}
	}

	/**
	 * Load logger properties from an InputStream
	 * @param is
	 * @throws IOException
	 */
	private static void loadLoggerProperties(InputStream is)
			throws IOException {
		Properties logprops = new Properties();
		logprops.load(is);
		PropertyConfigurator.configure(logprops);
		log = Logger.getLogger(EnvironmentConfiguration.class);
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
	 * Load Property file for JCS cache.
	 */
	public static void loadCacheProperties() {
		String confpath = CRUtil.resolveSystemProperties(cacheFilePath);
		StringBuilder errorMessage = new StringBuilder("Could not load cache configuration. Perhaps you are missing the file cache.ccf in ")
				.append(confpath).append("!");
		logDebug("Loading cache configuration from " + confpath);
		try {
			InputStream stream = new FileInputStream(confpath);
			loadCacheProperties(stream);
		} catch (NullPointerException e) {
			if (!cacheInitFailed) {
				logError(errorMessage.toString());
				cacheInitFailed = true;
			}
			loadCacheFallbackProperties();
		} catch (FileNotFoundException e) {
			if (!cacheInitFailed) {
				logError(errorMessage.toString());
				cacheInitFailed = true;
			}
			loadCacheFallbackProperties();
		} catch (IOException e) {
			if (!cacheInitFailed) {
				logError(errorMessage.toString());
				cacheInitFailed = true;
			}
			loadCacheFallbackProperties();
		}
	}
	
	/**
	 * Load cache fallback.
	 */
	private static void loadCacheFallbackProperties() {
		InputStream stream = EnvironmentConfiguration.class.getResourceAsStream(cacheFallbackPath);
		try {
			loadCacheProperties(stream);
			cacheInitFallback = true;
			log.debug("Loaded cache fallback configuration");
		} catch (IOException e) {
			System.out.println("Could not load cache fallback configuration.");
		}
	}

	/**
	 * Load cache configuration from an InputStream.
	 * @param is
	 * @throws IOException
	 */
	private static void loadCacheProperties(InputStream is)
			throws IOException{
		//LOAD CACHE CONFIGURATION
		Properties cacheProps = new Properties();
		cacheProps.load(is);
		if (cacheProps.containsKey(USE_PORTAL_CACHE_KEY) && Boolean.parseBoolean(cacheProps.getProperty(USE_PORTAL_CACHE_KEY))) {
			logDebug("Will not initialize ContentConnector Cache - Using the " + "cache configured by portalnode instead.");
		} else {
			CompositeCacheManager cManager = CompositeCacheManager.getUnconfiguredInstance();
			cManager.configure(cacheProps);
		}
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
		loggerFilePath = configurationPath + "/nodelog.properties";
		cacheFilePath = configurationPath + "/cache.ccf";
	}

	/**
	 * @return the path where the configuration files are loaded from.
	 */
	public static String getConfigPath() {
		return configurationPath;
	}

	/**
	 * @return the current cache file path.
	 */
	public static String getCacheFilePath() {
		return cacheFilePath;
	}

	/**
	 * set a new cache file path where the jcs cache.ccf is located.
	 * @param newCacheFilePath - path for the jcs cache configuration file
	 */
	public static void setCacheFilePath(final String newCacheFilePath) {
		cacheFilePath = newCacheFilePath;
	}
	
	/**
	 * @return <code>true</code> if the cache init has not (yet) failed, otherwhise false.
	 */
	public static boolean getCacheState() {
		return !cacheInitFailed;
	}
	
	/**
	 * @return <code>true</code> if the cache init has failed and we loaded the default/fallback configuration.
	 */
	public static boolean isCacheFallbackLoaded() {
		return cacheInitFallback;
	}
}
