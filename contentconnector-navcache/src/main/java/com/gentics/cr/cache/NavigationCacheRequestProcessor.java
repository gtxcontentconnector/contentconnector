package com.gentics.cr.cache;

import java.util.Collection;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.exceptions.CRException;

/**
 * NavigationCacheRequestProcessor
 */
public class NavigationCacheRequestProcessor extends RequestProcessor {
	/**
	 * Wrapped request processor
	 */
	protected RequestProcessor wrapped;

	/**
	 * Navigation cache
	 */
	protected NavigationCache navigationCache;

	/**
	 * Create an instance with the given config
	 * @param config config
	 * @throws CRException
	 */
	public NavigationCacheRequestProcessor(CRConfig config) throws CRException {
		super(config);
		wrapped = config.getNewRequestProcessorInstance(1);
		navigationCache = new NavigationCache(wrapped, config);
	}

	@Override
	public Collection<CRResolvableBean> getObjects(CRRequest request, boolean doNavigation) throws CRException {
		if (doNavigation) {
			Collection<CRResolvableBean> cached = navigationCache.getCachedNavigationObject(request);
			if (cached == null) {
				cached = navigationCache.fetchAndCacheNavigationObject(request);
			}
			// if navigation is still empty, we just forward the call to the wrapped requestprocessor
			if (cached == null) {
				return wrapped.getObjects(request, doNavigation);
			}
			return cached;
		} else {
			return wrapped.getObjects(request, doNavigation);
		}
	}

	/**
	 * Get the navigation cache
	 * @return navigation cache
	 */
	public NavigationCache getNavigationCache() {
		return navigationCache;
	}

	@Override
	public void finalize() {
	}
}
