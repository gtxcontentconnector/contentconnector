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
	 * Create an instance with the given config
	 * @param config config
	 * @throws CRException
	 */
	public NavigationCacheRequestProcessor(CRConfig config) throws CRException {
		super(config);
		wrapped = config.getNewRequestProcessorInstance(1);
	}

	@Override
	public Collection<CRResolvableBean> getObjects(CRRequest request, boolean doNavigation) throws CRException {
		if (doNavigation) {
			Collection<CRResolvableBean> cached = NavigationCache.get(config).getCachedNavigationObject(wrapped, request);
			if (cached == null) {
				cached = NavigationCache.get(config).fetchAndCacheNavigationObject(wrapped, request);
			}
			return cached;
		} else {
			return wrapped.getObjects(request, doNavigation);
		}
	}

	@Override
	public void finalize() {
	}
}
