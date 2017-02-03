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

	/** The start folder. */
	private String startFolder;

	/** The childfilter. */
	private String childfilter;

	/** The cache. */
	private PortalCache cache;

	/** The watch. */
	private StopWatch watch = new StopWatch();

	/** The cache key. */
	private String cacheKey;

	/**
	 * Instantiates a new navigation update job.
	 *
	 * @param startFolder the start folder
	 * @param childfilter the childfilter
	 * @param cacheKey the cache key
	 * @param rp the rp
	 * @param req the req
	 * @param cache the cache
	 */
	public NavigationUpdateJob(String startFolder, String childfilter, String cacheKey, RequestProcessor rp,
			CRRequest req, PortalCache cache) {

		log.debug("Initializing new " + this.getClass().getSimpleName() + " instance ..");
		log.debug("For navigation object with startfolder: " + startFolder + ", childfilter: " + childfilter);

		this.rp = rp;
		this.req = req;
		this.startFolder = startFolder;
		this.childfilter = childfilter;
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
				log.debug("Fetching and refreshing cache for navigation object, startfolder: " + startFolder
						+ ", childfilter: " + childfilter);
				watch.start();
			}

			// fetch the navigation object and put it into the configured cache
			CRResolvableBean crBean = null;
			Collection<CRResolvableBean> crBeanCollection;

			try {
				// get the navigation object
				crBeanCollection = rp.getNavigation(req);

				if (crBeanCollection.size() > 0) {
					crBean = crBeanCollection.iterator().next();

					// update the bean in the cache
					cache.put(cacheKey, crBean);
				} else {
					log.error("Navigation is empty for startfolder {" + startFolder + "} and childfilter {" + childfilter + "}");
					cache.put(cacheKey, NavigationCache.CACHED_NULL);
				}

			} catch (CRException e1) {
				log.error("Error while fetching navigation object!", e1);
			} catch (PortalCacheException e) {
				log.error("Error while putting object '" + crBean + "' into cache!", e);
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
