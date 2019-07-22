package com.gentics.cr.cache;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.gentics.api.lib.cache.PortalCache;
import com.gentics.api.lib.cache.PortalCacheAttributes;
import com.gentics.api.lib.cache.PortalCacheException;
import com.gentics.cr.CRConfig;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRRequest.DEFAULT_ATTRIBUTES;
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
	private final static int MIN_SCHEDULE_TIME = 60;

	/** The seconds before cache. */
	private final static int SECONDS_BEFORE_CACHE = 30;

	/** The cores. */
	private final static int THREADS = 2;

	/** Key for the cache zone. */
	public static final String CACHEZONE_KEY = "gentics-cr-navigation";

	/**
	 * Request processor
	 */
	private RequestProcessor rp;

	/** The cache. */
	private PortalCache cache;

	/** The schedule time. */
	private Long scheduleTime;

	/** The scheduler. */
	private ScheduledExecutorService scheduler;

	/** The cached keys. */
	private Set<String> cachedKeys = new HashSet<String>();

	/**
	 * Instantiates a new navigation cache.
	 * @param rp request processor
	 * @param crConf configuration
	 */
	NavigationCache(RequestProcessor rp, CRConfig crConf) {

		log.debug("Initializing new " + this.getClass().getSimpleName() + " instance ..");
		this.rp = rp;

		int threads = THREADS;
		int secondsBeforeCache = SECONDS_BEFORE_CACHE;
		int minScheduleTime = MIN_SCHEDULE_TIME;

		// read configuration
		if (crConf != null) {
			threads = crConf.getInteger("threads", threads);
			secondsBeforeCache = crConf.getInteger("secondsbeforecache", secondsBeforeCache);
			minScheduleTime = crConf.getInteger("minscheduletime", minScheduleTime);
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

				int possibleScheduleTime = maxLifeSeconds - secondsBeforeCache;
				scheduleTime = (long) ((possibleScheduleTime > minScheduleTime) ? possibleScheduleTime
						: minScheduleTime);
			} else {
				scheduleTime = new Long(minScheduleTime);
			}

			log.info("Computed scheduleTime: " + scheduleTime);

			scheduler = Executors.newScheduledThreadPool(threads);
			log.info("Initialized thread pool executor with " + threads + " threads in threadpool.");
		} catch (PortalCacheException e) {
			log.error("Could not initialize the PortalCache!", e);
		} catch (InstantiationException e) {
			log.error("Could not initialize the PortalCache!", e);
		}
	}

	/**
	 * Gets the cached navigation object.
	 * @param request request
	 * 
	 * @return the cached navigation object
	 */
	public Collection<CRResolvableBean> getCachedNavigationObject(CRRequest request) {
		return getCachedNavigationObject(getCacheKey(request));
	}

	/**
	 * Gets the cached navigation object.
	 * 
	 * @param cacheKey
	 *            the cache key
	 * @return the cached navigation object
	 */
	@SuppressWarnings("unchecked")
	private Collection<CRResolvableBean> getCachedNavigationObject(String cacheKey) {

		Collection<CRResolvableBean> crBeanCollection = null;

		if (cache != null) {
			Object obj = null;
			try {
				obj = cache.get(cacheKey);
			} catch (PortalCacheException e) {
				;
			}

			if (obj != null) {
				if (obj instanceof Collection<?>) {

					crBeanCollection = (Collection<CRResolvableBean>) obj;
					if (log.isDebugEnabled()) {
						log.debug("Loaded from cache: " + crBeanCollection);
					}
				} else {
					log.error("Not the right collection in cache!! " + obj.toString());
				}
			}
		}

		return crBeanCollection;
	}

	/**
	 * Fetch and cache navigation object.
	 * @param requestProcessor
	 *            the request processor
	 * @param req
	 *            the request
	 * 
	 * @return collection of resolvable beans
	 */
	public synchronized Collection<CRResolvableBean> fetchAndCacheNavigationObject(CRRequest req) {

		String cacheKey = getCacheKey(req);
		Collection<CRResolvableBean> crBeanCollection = getCachedNavigationObject(cacheKey);

		if (crBeanCollection != null) {
			log.info("Thread was waiting until synchronized fetch and caching was done");
			// do nothing and return crBean early
			return crBeanCollection;
		}

		if (cachedKeys.contains(cacheKey)) {
			log.error("Request for navigation object that should be in cache! This should not happen, check parameters!");
			// avoid double job generation
			return null;
		}

		// first generate a new update job for this navigation object
		NavigationUpdateJob job = new NavigationUpdateJob(cacheKey, rp, req, cache);
		// run it the first time so now it will be in cache
		job.run();
		// fetch the object from cache
		crBeanCollection = getCachedNavigationObject(cacheKey);
		// schedule task for periodical execution
		scheduler.scheduleWithFixedDelay(job, scheduleTime, scheduleTime, TimeUnit.SECONDS);
		// remember this cache key to check for double jobs
		cachedKeys.add(cacheKey);

		return crBeanCollection;
	}

	/**
	 * Gets the cache key. The cache key is constructed from the hash code of the request and the datasource ID from the request processor
	 * @param requestProcessor request processor
	 * @param request request
	 *
	 * @return the cache key
	 */
	public String getCacheKey(CRRequest request) {
		StringBuilder builder = new StringBuilder();
		int h = 0;
		// we use all attributes (but not request and request_wrapper, since they will change too often).
		// arrays will be transformed to lists
		for (DEFAULT_ATTRIBUTES attr : CRRequest.DEFAULT_ATTRIBUTES.values()) {
			switch (attr) {
			case REQUEST:
			case REQUEST_WRAPPER:
				break;
			default:
				Object value = request.get(attr.toString());
				if (value != null) {
					if (value instanceof Object[]) {
						Object[] valueArray = (Object[])value;
						if (valueArray.length > 0) {
							value = Arrays.asList(valueArray);
						} else {
							value = 0;
						}
					}
					h += value.hashCode();
				}
				break;
			}
		}
		builder.append(String.format("%08x", h));

		// Include rp datasource id into cachekey
		if (rp.getConfig() != null && rp.getConfig().getDatasource() != null) {
			builder.append(rp.getConfig().getDatasource().getId());
		}

		return builder.toString();
	}

}
