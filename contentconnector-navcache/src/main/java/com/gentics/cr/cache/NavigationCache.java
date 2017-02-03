package com.gentics.cr.cache;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.gentics.api.lib.cache.PortalCache;
import com.gentics.api.lib.cache.PortalCacheAttributes;
import com.gentics.api.lib.cache.PortalCacheException;
import com.gentics.cr.CRConfig;
import com.gentics.cr.CRConfigFileLoader;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;

/**
 * The Class NavigationCache.
 */
public class NavigationCache {
	/**
	 * Object representing a null entry in the cache
	 */
	public final static CRResolvableBean CACHED_NULL = new CRResolvableBean();

	/** The Constant log. */
	private static final Logger log = Logger.getLogger(NavigationCache.class);

	/** The min schedule time. */
	private static int MIN_SCHEDULE_TIME = 60;

	/** The seconds before cache. */
	private static int SECONDS_BEFORE_CACHE = 30;

	/** The cores. */
	private static int THREADS = 2;

	/** Key for the cache zone. */
	public static final String CACHEZONE_KEY = "gentics-cr-navigation";

	/** The Constant PROPERTY_FILENAME. */
	private static final String PROPERTY_FILENAME = "navigationcache";

	/** The instance. */
	private static NavigationCache instance;

	/** The cache. */
	private PortalCache cache;

	/** The schedule time. */
	private Long scheduleTime;

	/** The scheduler. */
	private ScheduledExecutorService scheduler;

	/** The cached keys. */
	private Set<String> cachedKeys = new HashSet<String>();

	/** The cr conf. */
	private CRConfig crConf;

	/**
	 * Instantiates a new navigation cache.
	 */
	private NavigationCache() {

		log.debug("Initializing new " + this.getClass().getSimpleName() + " instance ..");

		crConf = new CRConfigFileLoader(PROPERTY_FILENAME, null);

		// read configuration
		if (crConf != null) {
			THREADS = crConf.getInteger("threads", THREADS);
			SECONDS_BEFORE_CACHE = crConf.getInteger("secondsbeforecache", SECONDS_BEFORE_CACHE);
		}

		try {
			log.info("Cache region: " + CACHEZONE_KEY);
			cache = PortalCache.getCache(CACHEZONE_KEY);

			if (cache == null) {
				throw new InstantiationException("Could not get PortalCache instance! Cache is null!");
			}

			PortalCacheAttributes attributes = cache.getDefaultCacheAttributes();

			if (attributes != null) {
				// schedule time should be max life second minus the configured
				// SECONDS_BEFORE_CACHE to refill the cache in time
				int maxLifeSeconds = attributes.getMaxAge();
				log.info("MaxlifeSeconds for cache: " + maxLifeSeconds);

				int possibleScheduleTime = maxLifeSeconds - SECONDS_BEFORE_CACHE;
				scheduleTime = (long) ((possibleScheduleTime > MIN_SCHEDULE_TIME) ? possibleScheduleTime
						: MIN_SCHEDULE_TIME);
			} else {
				scheduleTime = new Long(MIN_SCHEDULE_TIME);
			}

			log.info("Computed scheduleTime: " + scheduleTime);

			scheduler = Executors.newScheduledThreadPool(THREADS);
			log.info("Initialized thread pool executor with " + THREADS + " threads in threadpool.");
		} catch (PortalCacheException e) {
			log.error("Could not initialize the PortalCache!", e);
		} catch (InstantiationException e) {
			log.error("Could not initialize the PortalCache!", e);
		}
	}

	/**
	 * Gets the Navigation Cache instance.
	 * 
	 * @return the navigation cache
	 */
	public static NavigationCache get() {
		if (instance == null) {
			// performance, thread safe
			synchronized (NavigationCache.class) {
				if (instance == null) {
					instance = new NavigationCache();
				}
			}
		}
		return instance;
	}

	/**
	 * Gets the cached navigation object.
	 * 
	 * @param startFolder
	 *            the start folder
	 * @param childFilter
	 *            the child filter
	 * @param requestProcessor
	 *            the request processor
	 * @return the cached navigation object
	 */
	public CRResolvableBean getCachedNavigationObject(String startFolder, String childFilter, RequestProcessor requestProcessor) {
		return getCachedNavigationObject(getCacheKey(startFolder, childFilter, requestProcessor));
	}

	/**
	 * Gets the cached navigation object.
	 * 
	 * @param cacheKey
	 *            the cache key
	 * @return the cached navigation object
	 */
	private CRResolvableBean getCachedNavigationObject(String cacheKey) {

		CRResolvableBean crBean = null;

		if (cache != null) {
			Object obj = null;
			try {
				obj = cache.get(cacheKey);
			} catch (PortalCacheException e) {
				;
			}

			if (obj != null) {
				if (obj instanceof CRResolvableBean) {

					crBean = (CRResolvableBean) obj;
					if (log.isDebugEnabled()) {
						log.debug("Loaded from cache: " + crBean);
					}
				} else {
					log.error("Not the right collection in cache!! " + obj.toString());
				}
			}
		}

		return crBean;
	}

	/**
	 * Fetch and cache navigation object.
	 * 
	 * @param startFolder
	 *            the start folder
	 * @param childfilter
	 *            the childfilter
	 * @param requestProcessor
	 *            the request processor
	 * @param req
	 *            the req
	 * @return the cR resolvable bean
	 */
	public synchronized CRResolvableBean fetchAndCacheNavigationObject(String startFolder, String childfilter,
			RequestProcessor requestProcessor, CRRequest req) {

		String cacheKey = getCacheKey(startFolder, childfilter, requestProcessor);
		CRResolvableBean crBean = getCachedNavigationObject(cacheKey);

		if (crBean != null) {
			log.info("Thread was waiting until synchronized fetch and caching was done");
			// do nothing and return crBean early
			return crBean;
		}

		if (cachedKeys.contains(cacheKey)) {
			log.error("Request for navigation object that should be in cache! This should not happen, check parameters! "
					+ "startfolder: " + startFolder + ", childfilter: " + childfilter);
			// avoid double job generation
			return null;
		}

		// first generate a new update job for this navigation object
		NavigationUpdateJob job = new NavigationUpdateJob(startFolder, childfilter, cacheKey, requestProcessor, req, cache);
		// run it the first time so now it will be in cache
		job.run();
		// fetch the object from cache
		crBean = getCachedNavigationObject(startFolder, childfilter, requestProcessor);
		// schedule task for periodical execution
		scheduler.scheduleWithFixedDelay(job, scheduleTime, scheduleTime, TimeUnit.SECONDS);
		// remember this cache key to check for double jobs
		cachedKeys.add(cacheKey);

		return crBean;
	}

	/**
	 * Gets the cache key.
	 * 
	 * @param startFolder
	 *            the start folder
	 * @param childFilter
	 *            the child filter
	 * @param request processor
	 *            the request processor
	 * @return the cache key
	 */
	private static String getCacheKey(String startFolder, String childFilter, RequestProcessor requestProcessor) {



		if (StringUtils.isEmpty(startFolder) && StringUtils.isEmpty(childFilter)) {
			throw new IllegalArgumentException("At least one String input has to have a value!");
		}

		StringBuilder builder = new StringBuilder();

		if (!StringUtils.isEmpty(startFolder)) {
			String startFolderHexCode = String.format("%08x", startFolder.hashCode());
			builder.append(startFolderHexCode);
		}

		if (!StringUtils.isEmpty(childFilter)) {
			String childFilterHashCode = String.format("%08x", childFilter.hashCode());
			builder.append(childFilterHashCode);
		}

		// Include rp datasource id into cachekey
		if (requestProcessor.getConfig() != null && requestProcessor.getConfig().getDatasource() != null) {
			builder.append(requestProcessor.getConfig().getDatasource().getId());
		}

		return builder.toString();
	}

}
