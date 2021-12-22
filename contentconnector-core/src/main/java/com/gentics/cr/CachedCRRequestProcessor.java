package com.gentics.cr;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import com.gentics.api.lib.cache.PortalCache;
import com.gentics.api.lib.cache.PortalCacheException;
import com.gentics.api.lib.datasource.Datasource;
import com.gentics.api.lib.datasource.Datasource.Sorting;
import com.gentics.api.lib.datasource.DatasourceException;
import com.gentics.api.lib.exception.ParserException;
import com.gentics.api.lib.expressionparser.ExpressionParserException;
import com.gentics.api.lib.expressionparser.filtergenerator.DatasourceFilter;
import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.monitoring.MonitorFactory;
import com.gentics.cr.monitoring.UseCase;
import com.gentics.lib.log.NodeLogger;

/**
 * CachedCRRequestProcessor fixes a bug in Gentics PortalConnector which
 * prevents the PortalConnector from caching prefill attributes. Gentics Ticket
 * ID is #38183.
 * Last changed: $Date: 2010-04-01 15:24:02 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 541 $
 * @author $Author: supnig@constantinopel.at $
 */
public class CachedCRRequestProcessor extends RequestProcessor {

	/**
	 * Log4j logger for error and debug messages.
	 */
	private static NodeLogger logger = NodeLogger.getNodeLogger(CachedCRRequestProcessor.class);

	/**
	 * JCS cache for our results.
	 */
	private static PortalCache resultCache;

	/**
	 * Map with base {@link Resolvable}s for the filter.
	 */
	private HashMap<String, Resolvable> resolvables = null;

	/**
	 * Attribute to check if an object is up to date. So the attribute should
	 * change whenever the object is changed.
	 */
	private static final String UPDATEATTRIBUTE = "updatetimestamp";

	/**
	 * Create a new instance of CRRequestProcessor.
	 * @param config TODO javadoc
	 * @throws CRException TODO javadoc
	 */
	public CachedCRRequestProcessor(final CRConfig config) throws CRException {
		super(config);
	}

	/**
	 * Fetch the matching objects using the given CRRequest.
	 * @param request TODO javadoc
	 * @param doNavigation defines if to fetch child elements
	 * @return resulting objects
	 * @throws CRException TODO javadoc
	 */
	public final Collection<CRResolvableBean> getObjects(final CRRequest request, final boolean doNavigation)
			throws CRException {
		UseCase getObjectCase = MonitorFactory.startUseCase("CRRequestProcessor.getObjects(" + config.getName() + ")");
		Datasource ds = null;
		DatasourceFilter dsFilter;
		Vector<CRResolvableBean> collection = new Vector<CRResolvableBean>();
		if (request != null) {
			// Parse the given expression and create a datasource filter
			try {
				ds = this.config.getDatasource();
				if (ds == null) {
					throw (new DatasourceException("No Datasource available."));
				}

				dsFilter = request.getPreparedFilter(config, ds);
				// add base resolvables
				if (this.resolvables != null) {
					for (Iterator<String> it = this.resolvables.keySet().iterator(); it.hasNext();) {
						String name = it.next();
						dsFilter.addBaseResolvable(name, this.resolvables.get(name));
					}
				}

				String[] prefillAttributes = request.getAttributeArray();
				int first = request.getStart().intValue();
				int last = request.getCount().intValue();
				Sorting[] sorting = request.getSorting();
				// do the query
				Collection<Resolvable> col = getResult(ds, dsFilter, prefillAttributes, first, last, sorting);

				// convert all objects to serializeable beans
				if (col != null) {
					for (Iterator<Resolvable> it = col.iterator(); it.hasNext();) {
						CRResolvableBean crBean = new CRResolvableBean(it.next(), request.getAttributeArray());
						if (this.config.getFolderType().equals(crBean.getObj_type()) && doNavigation) {
							// Process child elements
							String fltr = "object.folder_id=='" + crBean.getContentid() + "'";
							if (request.getChildFilter() != null) {
								fltr += "AND (" + request.getChildFilter() + ")";
							}
							// If object is a folder => retrieve the childs of the object
							CRRequest childReq = request.Clone();
							childReq.setRequestFilter(fltr);
							crBean.fillChildRepository(this.getNavigation(childReq));
						}
						collection.add(this.replacePlinks(crBean, request));
					}
				}

			} catch (ParserException e) {
				e.printStackTrace();
				throw new CRException("ParserException", e.getMessage());
			} catch (ExpressionParserException e) {
				e.printStackTrace();
				throw new CRException("ExpressionParserException", e.getMessage());
			} catch (DatasourceException e) {
				e.printStackTrace();
				throw new CRException("DatasourceException", e.getMessage());
			} finally {
				CRDatabaseFactory.releaseDatasource(ds);
				getObjectCase.stop();
			}
		}
		return collection;
	}

	/**
	 * This Method implements a caching due to a lack of performance in the
	 * CNDatasource when getting a large result with prefilled Attributes.
	 * When the Issue is solved we can remove this method. Gentics Ticket ID is
	 * #38183.
	 * @param ds {@link Datasource} to get the objects from
	 * @param dsFilter filter describing the objects to fetch from the
	 * {@link Datasource}
	 * @param prefillAttributes attributes that should be prefilled with the
	 * result.
	 * @param first index of the first item to fetch
	 * @param last index of the last item to fetch. Use <code>-1</code> to get all
	 * objects
	 * @param sorting {@link Sorting} that should be used.
	 * @return {@link Collection} of {@link Resolvable}s described by the filter.
	 */
	private Collection<Resolvable> getResult(final Datasource ds, final DatasourceFilter dsFilter,
			final String[] prefillAttributes, final int first, final int last, final Sorting[] sorting) {
		UseCase getResultCase = MonitorFactory.startUseCase("CRRequestProcessor.getResult(" + config.getName() + ")");
		Collection<?> result;
		Collection<Resolvable> checkCacheResult;
		Collection<Resolvable> collection = null;
		Collection<Resolvable> cachedResult;
		String cacheKey = config.getName() + "-" + dsFilter.getExpressionString();

		try {
			UseCase getResultFastCase = MonitorFactory.startUseCase("CRRequestProcessor.getResult(" + config.getName()
					+ ")#fastResult");
			checkCacheResult = toResolvableCollection(ds.getResult(dsFilter, new String[] {}, first, last, sorting));
			getResultFastCase.stop();

			UseCase getResultCacheCase = MonitorFactory.startUseCase("CRRequestProcessor.getResult(" + config.getName()
					+ ")#cache");
			cachedResult = getCachedResult(cacheKey);
			getResultCacheCase.stop();

			boolean up2date = compare(cachedResult, checkCacheResult);

			if (up2date) {
				collection = cachedResult;
			} else {
				UseCase getResultUncachedCase = MonitorFactory.startUseCase("CRRequestProcessor.getResult("
						+ config.getName() + ")#uncached");
				result = ds.getResult(dsFilter, prefillAttributes, first, last, sorting);
				collection = toResolvableCollection(result);
				saveResult(cacheKey, collection);
				getResultUncachedCase.stop();
			}
		} catch (DatasourceException e) {
			logger.error("Cannot get the result from the Datasource.", e);
		}
		getResultCase.stop();
		return collection;
	}

	/**
	 * Compares two resultsets for their updateTime.
	 * @param left first Resolvable
	 * @param right second Resolvable
	 * @return returns false if they are not equal
	 */
	public static boolean compare(final Collection<Resolvable> left, final Collection<Resolvable> right) {
		UseCase getResultVerifyCase = MonitorFactory.startUseCase("CRRequestProcessor.compare()#verify");
		if (left.size() != right.size()) {
			return false;
		} else {
			int leftUpdatetime = 0;
			for (Resolvable leftResolvable : left) {
				leftUpdatetime = checkUpdatetime(leftResolvable, leftUpdatetime);
			}
			int rightUpdatetime = 0;
			for (Resolvable rightResolvable : right) {
				rightUpdatetime = checkUpdatetime(rightResolvable, rightUpdatetime);
			}
			getResultVerifyCase.stop();
			if (rightUpdatetime != leftUpdatetime) {
				return false;
			}
		}
		return true;
	}

	/**
	 * check if updatetime of resolvable is newer than given updatetime.
	 * @param resolvable {@link Resolvable} to check the updatetime for.
	 * @param updatetime timestamp.
	 * @return newest updatetime, can be the updatetime of the resolvable or the
	 * given updatetime
	 */
	private static int checkUpdatetime(final Resolvable resolvable, final int updatetime) {
		Object updatetimestampObject = resolvable.get(UPDATEATTRIBUTE);
		if (updatetimestampObject instanceof Integer) {
			int updatetimestamp = ((Integer) updatetimestampObject).intValue();
			if (updatetimestamp > updatetime) {
				return updatetimestamp;
			}
		}
		return updatetime;
	}

	/**
	 * get the result from the cache.
	 * @param cacheKey key to get the result for
	 * @return result for the cacheKey
	 */
	private Collection<Resolvable> getCachedResult(final String cacheKey) {
		initCache();
		Object cacheResultObject = null;
		try {
			cacheResultObject = resultCache.get(cacheKey);
		} catch (PortalCacheException e) {
		   ;
		}
		if (cacheResultObject != null) {
			return toResolvableCollection(cacheResultObject);
		} else {
			return new Vector<Resolvable>(0);
		}
	}

	/**
	 * save the result to the cache.
	 * @param cacheKey key to store the result under
	 * @param result result to cache
	 */
	private void saveResult(final String cacheKey, final Collection<Resolvable> result) {
		initCache();
		try {
			resultCache.put(cacheKey, result);
		} catch (PortalCacheException e) {
			logger.error("Cannot save the result in cache", e);
		}
	}

	/**
	 * initialize the result cache.
	 */
	private void initCache() {
		if (resultCache == null) {
			try {
				resultCache = PortalCache.getCache("gentics-cr-CRRequestProcessor-results");
			} catch (PortalCacheException e) {
				logger.error("Cannot initialize the result cache", e);
			}
		}
	}

	@Override
	public void finalize() {
		// TODO Auto-generated method stub
	}

}
