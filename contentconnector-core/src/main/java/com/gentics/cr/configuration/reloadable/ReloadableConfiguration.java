package com.gentics.cr.configuration.reloadable;

import com.gentics.cr.CachedCRRequestProcessor;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.util.Constants;
import com.gentics.lib.log.NodeLogger;

/**
 * Operates as an Interface between the servlet and the Indexer Engine.
 * Last changed: $Date: 2009-09-02 17:57:48 +0200 (Mi, 02 Sep 2009) $
 * @version $Revision: 180 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public abstract class ReloadableConfiguration {

	/**
	* Log4j logger for error and debug messages.
	*/
	private static NodeLogger logger = 
			NodeLogger.getNodeLogger(CachedCRRequestProcessor.class);

	/**
	 * Interval for checking for new jobs.
	 */
	private static final int CHECK_INTERVAL = 5;

	/**
	 * Listener that will be executed on a config change.
	 */
	private ReloadListener listener;

	/**
	 * Thread that checks the configuration for changes.
	 */
	private Thread reloadChecker;

	/**
	 * Object for synchronizing method blocks.
	 */
	private Object syn = new Object();

	/**
	 * Sets the current check interval.
	 * @param interval Interval in seconds
	 */
	public final void setCheckInterval(final int interval) {
		synchronized (syn) {
			this.checkInterval = interval;
		}
	}

	/**
	 * Gets the current check interval.
	 * @return Interval in seconds
	 */
	public final int getCheckInterval() {
		int i = 0;
		synchronized (syn) {
			i = this.checkInterval;
		}
		return i;
	}

	/**
	 * Set the default value of checkInterval to CHECK_INTERVAL.
	 */
	private int checkInterval = CHECK_INTERVAL;

	/**
	 * Creates a new instance of ReloadableContainer.
	 * @param reloadListener ReloadListener that will be executed if the config
	 *  changes.
	 */
	public ReloadableConfiguration(final ReloadListener reloadListener) {
		this.listener = reloadListener;
	}

	/**
	 * Reloads the current configuration.
	 * It is recommended that this method is implemented 
	 * in a thread safe manner.
	 * @return newly loaded configuration
	 */
	public abstract GenericConfiguration reloadConfiguration();

	/**
	 * Checks if the current configuration has been changed.
	 * @return true if the configuration has been changed 
	 * and should be reloaded.
	 */
	public abstract boolean hasConfigChanged();

	/**
	 * This Method has to be called after the first time 
	 * the configuration has finishd loading.
	 */
	public final void startChangeListener() {
		this.reloadChecker = new Thread(new Runnable() {

			public void run() {
				while (!Thread.currentThread().isInterrupted()) {
					try {
						Thread.sleep(getCheckInterval()
								* Constants.MILLISECONDS_IN_A_SECOND);
						if (hasConfigChanged()) {
							if (listener != null) {
								listener.onBeforeReload();
							}
							GenericConfiguration c = reloadConfiguration();
							if (listener != null) {
								listener.onReloadFinished(c);
							}
						}
					} catch (InterruptedException e) {
						logger.debug(e.getMessage(), e);
					}
				}
			}

		});
	}

	/**
	 * This Method should be called right before the application stops.
	 * It will stop the reload checker Thread.
	 */
	public final void destroy() {
		if (this.reloadChecker != null && this.reloadChecker.isAlive()) {
			if (!this.reloadChecker.isInterrupted()) {
				this.reloadChecker.interrupt();
			}
			try {
				this.reloadChecker.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
