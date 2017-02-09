package com.gentics.cr.cache;


import java.util.Collection;
import java.util.TimerTask;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;

import com.gentics.api.lib.cache.PortalCache;
import com.gentics.api.lib.cache.PortalCacheException;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.exceptions.CRException;

/**
 * The Class NavigationUpdateJob.
 */
public class NavigationUpdateJob extends TimerTask {

	/** The Constant log. */
	private static final Logger log = Logger.getLogger(NavigationUpdateJob.class);

	/** The rp. */
	private RequestProcessor rp;

	/** The req. */
	private CRRequest req;

	/** The cache. */
	private PortalCache cache;

	/** The watch. */
	private StopWatch watch = new StopWatch();

	/** The cache key. */
	private String cacheKey;

	/**
	 * Instantiates a new navigation update job.
	 * @param cacheKey the cache key
	 * @param rp the rp
	 * @param req the req
	 * @param cache the cache
	 */
	public NavigationUpdateJob(String cacheKey, RequestProcessor rp, CRRequest req, PortalCache cache) {

		log.debug("Initializing new " + this.getClass().getSimpleName() + " instance ..");

		this.rp = rp;
		this.req = req;
		this.cacheKey = cacheKey;
		this.cache = cache;
	}

	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		try {

			if (log.isDebugEnabled()) {
				watch.reset();
				log.debug("Fetching and refreshing cache for navigation object");
				watch.start();
			}

			// fetch the navigation object and put it into the configured cache
			Collection<CRResolvableBean> crBeanCollection = null;

			try {
				// get the navigation object
				crBeanCollection = rp.getNavigation(req);

				// update the cache
				cache.put(cacheKey, crBeanCollection);

			} catch (CRException e1) {
				log.error("Error while fetching navigation object!", e1);
			} catch (PortalCacheException e) {
				log.error("Error while putting object '" + crBeanCollection + "' into cache!", e);
			}

			if (log.isDebugEnabled()) {
				watch.stop();
				log.debug("Time to fetch:");
				log.debug(watch.toString());
			}
		} catch (Throwable e) {
			// catch any exception, otherwise thread pool will stop to execute
			// the job
			log.error("Error while fetching navigation object!", e);
		}
	}
}
