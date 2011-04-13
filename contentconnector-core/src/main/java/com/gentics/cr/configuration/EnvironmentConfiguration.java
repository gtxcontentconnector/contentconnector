package com.gentics.cr.configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.gentics.cr.util.CRUtil;

/**
 * Environment Configuration manages the configuration of log4j and jcs.
 * Last changed: $Date$
 * @version $LastChangedRevision$
 * @author $Author$
 *
 */
public final class EnvironmentConfiguration {
	/**
	 * Confpath.
	 */
  public static final String CONFPATH = "${" + CRUtil.PORTALNODE_CONFPATH 
  	+ "}";
   /**
   * Path to the default log4j property file.
   */
  private static final String LOGGER_FILE_PATH =
	  CONFPATH + "/nodelog.properties";

  /**
   * Path to the jcs configuration file.
   */
  private static final String CACHE_FILE_PATH =
	  CONFPATH + "/cache.ccf";

  /**
   * Configuration key if we should use the same caches as Gentics
   * Portal.Node.
   */
  private static final String USE_PORTAL_CACHE_KEY =
    "com.gentics.cr.useportalcaches";

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
   * Detect if cache property loading already failed, so we put out the error
   * message only once.
   */
  private static boolean cacheInitFailed = false;

  /**
   * private constructor to prevent initializing of the utility class.
   */
  private EnvironmentConfiguration() { }

  /**
   * Load Environment Properties.
   *     - load logger properties for log4j
   *     - load cache properties for JCS
   */
  public static void loadEnvironmentProperties() {
    loadLoggerPropperties();
    loadCacheProperties();
  }

  /**
   * Load Property file for log4j.
   */
  public static void loadLoggerPropperties() {
    Properties logprops = new Properties();
    String confpath = CRUtil.resolveSystemProperties(LOGGER_FILE_PATH);
    String errorMessage = "Could not find nodelog.properties at: " + confpath;
    try {
      logprops.load(new FileInputStream(confpath));
      PropertyConfigurator.configure(logprops);
      log = Logger.getLogger(EnvironmentConfiguration.class);
    } catch (IOException e) {
      if (!loggerInitFailed) {
        System.out.println(errorMessage);
        loggerInitFailed = true;
      }
    } catch (NullPointerException e) {
      if (!loggerInitFailed) {
        System.out.println(errorMessage);
        loggerInitFailed = true;
      }
    }
  }

  /**
   * Load Property file for JCS cache.
   */
  public static void loadCacheProperties() {
    String errorMessage = "Could not load cache configuration. Perhaps you are "
      + "missing the file cache.ccf in " + CRUtil.resolveSystemProperties(
          CONFPATH + "/") + "!";
    try {
      //LOAD CACHE CONFIGURATION
      String confpath = CRUtil.resolveSystemProperties(CACHE_FILE_PATH);
      Properties cacheProps = new Properties();
      cacheProps.load(new FileInputStream(confpath));
      if (cacheProps.containsKey(USE_PORTAL_CACHE_KEY)
          && Boolean.parseBoolean(cacheProps.getProperty(USE_PORTAL_CACHE_KEY)))
      {
        logDebug("Will not initialize ContentConnector Cache - Using the "
            + "cache configured by portalnode instead.");
      } else {
        CompositeCacheManager cManager =
          CompositeCacheManager.getUnconfiguredInstance();
        cManager.configure(cacheProps);
      }
    } catch (NullPointerException e) {
      if (!cacheInitFailed) {
        logError(errorMessage);
        cacheInitFailed = true;
      }
    } catch (FileNotFoundException e) {
      if (!cacheInitFailed) {
        logError(errorMessage);
        cacheInitFailed = true;
      }
    } catch (IOException e) {
      if (!cacheInitFailed) {
        logError(errorMessage);
        cacheInitFailed = true;
      }
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
      loadLoggerPropperties();
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
      loadLoggerPropperties();
    }
    if (loggerInitialized()) {
      log.error(message);
    } else {
      System.out.println(message);
    }
  }
}
