package com.gentics.cr;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.gentics.api.lib.datasource.Datasource;
import com.gentics.api.lib.datasource.Datasource.Sorting;
import com.gentics.api.lib.datasource.DatasourceException;
import com.gentics.api.lib.etc.ObjectTransformer;
import com.gentics.api.lib.exception.NodeException;
import com.gentics.api.lib.exception.ParserException;
import com.gentics.api.lib.expressionparser.ExpressionParserException;
import com.gentics.api.lib.expressionparser.filtergenerator.DatasourceFilter;
import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.api.portalnode.connector.PortalConnectorFactory;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.util.ArrayHelper;
import com.gentics.cr.util.CRUtil;
import com.gentics.lib.log.NodeLogger;

/**
 * <p>
 * This request processor is optimized for good scaling navigation tree
 * building. Especially for large datasets the navigation object building can be
 * very expensive in database calls. With this request processor the amount of
 * database calls is always fixed.
 * </p>
 * <p>
 * Consider the child request filter to be as restrictive as possible to avoid
 * unnecessary fetched database rows. For example for projects with many nodes
 * you should add a child filter for
 * </p>
 * <p>
 * For automatic inclusion of the <code>node_id</code> in the child filter to
 * fetch only children of the same node there is a configuration parameter:
 * <code>usenodeidinchildrule</code>.
 * </p>
 * <p>
 * Basically only the method
 * {@link OptimisticNavigationRequestProcessor#getObjects(CRRequest, boolean)}
 * method is optimized when the parameter doNavigation is true. The rest is
 * adapted from the {@link CRRequestProcessor}.
 * </p>
 * <p>
 * Example configuration:
 * </p>
 * 
 * <pre>
 * rp.1.rpClass=com.gentics.cr.OptimisticNavigationRequestProcessor
 * rp.1.usenodeidinchildrule=true
 * </pre>
 * 
 * @author l.osang@gentics.com, c.supnig@gentics.com, s.vogel@gentics.com
 * 
 */
public class OptimisticNavigationRequestProcessor extends RequestProcessor {

	/** The logger. */
	private static NodeLogger logger = NodeLogger.getNodeLogger(OptimisticNavigationRequestProcessor.class);

	/**
	 * Key for.
	 * {@link OptimisticNavigationRequestProcessor#folderIdContentmapName} in
	 * the config
	 */
	private static final String FOLDER_ID_KEY = "folder_id.key";

	private static final String NODE_ID_CHILDREN_FEATURE_KEY = "usenodeidinchildrule";

	/** String of content map folder id column, default: "folder_id". */
	private String folderIdContentmapName = "folder_id";
	/** String of content map node id column, default: "node_id". */
	private String nodeIdContentMapName = "node_id";
	/** Boolean, if node id should be recognized for childfilter */
	private boolean usenodeidsinchildrule = false;

	/**
	 * Create a new instance of CRRequestProcessor.
	 * 
	 * @param config
	 *            the config
	 * @throws CRException
	 *             the cR exception
	 */
	public OptimisticNavigationRequestProcessor(CRConfig config) throws CRException {
		super(config);

		logger.debug("Initializing new " + this.getClass().getSimpleName() + " instance ...");

		if (!StringUtils.isEmpty(config.getString(FOLDER_ID_KEY))) {
			folderIdContentmapName = config.getString(FOLDER_ID_KEY);
		}

		usenodeidsinchildrule = ObjectTransformer.getBoolean(config.getString(NODE_ID_CHILDREN_FEATURE_KEY), false);
		logger.debug("Using node id in child rule: " + usenodeidsinchildrule);
	}

	/**
	 * 
	 * Fetch the matching objects using the given CRRequest.
	 * 
	 * @param request
	 *            CRRequest
	 * @param doNavigation
	 *            defines if to fetch children
	 * @return resulting objects
	 * @throws CRException
	 *             TODO javadocs
	 */
	public Collection<CRResolvableBean> getObjects(final CRRequest request, final boolean doNavigation)
			throws CRException {
		Datasource ds = null;
		DatasourceFilter dsFilter;
		Vector<CRResolvableBean> collection = new Vector<CRResolvableBean>();
		long start = 0;

		if (logger.isDebugEnabled()) {
			// starttime
			start = System.currentTimeMillis();
		}

		// for storing all possible nodes for children
		Set<String> nodeIds = new HashSet<String>();

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

				if (logger.isDebugEnabled()) {
					logger.debug("dsFilter: " + dsFilter.getExpressionString());
				}

				String[] prefillAttributes = request.getAttributeArray();
				prefillAttributes = ArrayHelper.removeElements(prefillAttributes, "contentid", "updatetimestamp");
				// do the query
				Collection<Resolvable> col = this.toResolvableCollection(ds.getResult(dsFilter, prefillAttributes,
						request.getStart().intValue(), request.getCount().intValue(), request.getSorting()));

				if (logger.isDebugEnabled()) {
					logger.debug("Getting columns for filter " + dsFilter.getExpressionString() + " took "
							+ (System.currentTimeMillis() - start) + " ms");
					start = System.currentTimeMillis();
				}

				// convert all objects to serializeable beans
				if (col != null) {
					for (Iterator<Resolvable> it = col.iterator(); it.hasNext();) {
						CRResolvableBean crBean = new CRResolvableBean(it.next(), request.getAttributeArray());
						collection.add(this.replacePlinks(crBean, request));

						if (usenodeidsinchildrule && doNavigation) {
							// get all node ids of root elements
							String nodeId = crBean.getString(nodeIdContentMapName, null);
							if (!StringUtils.isEmpty(nodeId)) {
								nodeIds.add(nodeId);
							}
						}
					}
					// IF NAVIGAION WE PROCESS THE FAST NAVIGATION ALGORITHM
					if (doNavigation) {

						// get original sorting order for child sorting
						// sort childrepositories with that
						Sorting[] sorting = request.getSorting();

						CRRequest childFilter = buildChildFilter(request, nodeIds);

						Collection<CRResolvableBean> children = getObjects(childFilter, false);

						if (logger.isDebugEnabled()) {
							logger.debug("Getting children for filter " + childFilter.getRequestFilter() + " took "
									+ (System.currentTimeMillis() - start) + " ms");
							start = System.currentTimeMillis();
						}

						// those Resolvables will be filled with specified
						// attributes
						Map<Resolvable, CRResolvableBean> itemsToPrefetch = new HashMap<Resolvable, CRResolvableBean>();
						HashMap<String, Vector<CRResolvableBean>> prepareFolderMap = prepareFolderMap(children);

						for (CRResolvableBean item : collection) {
							// build the tree
							recursiveTreeBuild(item, prepareFolderMap, sorting, itemsToPrefetch);
						}

						if (logger.isDebugEnabled()) {
							logger.debug("Tree building took " + (System.currentTimeMillis() - start) + " ms");
							start = System.currentTimeMillis();
						}

						// prefetch all necessary attribute that are specified
						// in the request
						try {
							if (!ArrayUtils.isEmpty(prefillAttributes)) {
								PortalConnectorFactory.prefillAttributes(ds, itemsToPrefetch.keySet(),
										Arrays.asList(prefillAttributes));
							}
						} catch (NullPointerException e) {
							logger.warn("Portal Connector threw a NullPointerException, we will silently ignore this",
									e);
						}

						if (logger.isDebugEnabled()) {
							logger.debug("Prefilling attibutes " + prefillAttributes + " took "
									+ (System.currentTimeMillis() - start) + " ms");
							start = System.currentTimeMillis();
						}

						// update the fetched attributes in the cr beans
						for (CRResolvableBean crBean : itemsToPrefetch.values()) {
							crBean.updateCRResolvableBeanAfterAttributePrefetch(prefillAttributes);
						}

						if (logger.isDebugEnabled()) {
							logger.debug("Update Resolvables after prefetch took "
									+ (System.currentTimeMillis() - start) + " ms");
							start = System.currentTimeMillis();
						}
					}
				}
			} catch (ParserException e) {
				logger.error("Error getting filter for Datasource.", e);
				throw new CRException(e);
			} catch (ExpressionParserException e) {
				logger.error("Error getting filter for Datasource.", e);
				throw new CRException(e);
			} catch (DatasourceException e) {
				logger.error("Error getting result from Datasource.", e);
				throw new CRException(e);
			} catch (NodeException e) {
				logger.error("Error getting result from Datasource.", e);
				throw new CRException(e);
			} catch (Exception e) {
				logger.error("Error getting result from Datasource.", e);
				throw new CRException(e);
			} finally {
				CRDatabaseFactory.releaseDatasource(ds);
			}
		}
		return collection;
	}

	private CRRequest buildChildFilter(CRRequest request, Set<String> nodeIds) {
		// get original sorting order for child sorting
		// sort childrepositories with that
		Sorting[] sorting = request.getSorting();
		// Build the request to fetch all possible children
		CRRequest childReq = new CRRequest();
		// set children attributes (folder_id)
		String[] fetchAttributesForChildren = { folderIdContentmapName, nodeIdContentMapName };

		// add all attributes to fetch in order to sort
		// correctly
		if (!ArrayUtils.isEmpty(sorting)) {
			for (int i = 0; i < sorting.length; i++) {
				Sorting sortingElement = sorting[i];
				fetchAttributesForChildren = (String[]) ArrayUtils.add(fetchAttributesForChildren,
						sortingElement.getColumnName());
			}
		}

		childReq.setAttributeArray(fetchAttributesForChildren);

		StringBuilder childFilter = new StringBuilder();

		if (!StringUtils.isEmpty(request.getChildFilter())) {
			childFilter.append(request.getChildFilter());
		}

		if (usenodeidsinchildrule && !CollectionUtils.isEmpty(nodeIds)) {
			if (!StringUtils.isEmpty(request.getChildFilter())) {
				childFilter.append(" AND ");
			}
			childFilter.append("object." + nodeIdContentMapName + " CONTAINSONEOF [ " + StringUtils.join(nodeIds, ',')
					+ " ] ");
		}

		childReq.setRequestFilter(childFilter.toString());
		childReq.setSortArray(new String[] { folderIdContentmapName + ":asc" });

		return childReq;
	}

	/**
	 * prepare the fetched children objects and put them to a prepared map with
	 * this format: <code>(folder_id, Collection children)</code>.
	 * 
	 * @param children
	 *            the children
	 * @return the prepared HashMap
	 */
	private HashMap<String, Vector<CRResolvableBean>> prepareFolderMap(Collection<CRResolvableBean> children) {

		HashMap<String, Vector<CRResolvableBean>> map = new HashMap<String, Vector<CRResolvableBean>>();

		for (CRResolvableBean crResolvableBean : children) {

			String folder_id = crResolvableBean.getString(folderIdContentmapName);

			if (StringUtils.isNotEmpty(folder_id)) {
				Vector<CRResolvableBean> col = map.get(folder_id);

				if (col == null) {
					col = new Vector<CRResolvableBean>();
					map.put(folder_id, col);
				}

				col.add(crResolvableBean);
			}
		}

		return map;
	}

	/**
	 * Builds the tree and fills the children of the root element.
	 * 
	 * @param root
	 *            the element that will be filled with children
	 * @param folderMap
	 *            the folder map
	 * @param sorting
	 *            the sorting
	 * @param itemsToPrefetch
	 *            the items to prefetch
	 */
	private void recursiveTreeBuild(CRResolvableBean root, HashMap<String, Vector<CRResolvableBean>> folderMap,
			Sorting[] sorting, Map<Resolvable, CRResolvableBean> itemsToPrefetch) {

		// fill to items that should be filled with attributes
		itemsToPrefetch.put(root.getResolvable(), root);

		// get items from folderMap
		Vector<CRResolvableBean> children = folderMap.get(root.getContentid());

		// brake condition, there are no children for this tree node
		if (CollectionUtils.isEmpty(children)) {
			return;
		}

		// do the sorting
		if (!ArrayUtils.isEmpty(sorting)) {
			CRUtil.sortCollection(children, sorting);
		}

		// fill the actual object with children
		root.fillChildRepository(children);

		for (CRResolvableBean crResolvableBean : children) {

			// recursive call to build children map
			recursiveTreeBuild(crResolvableBean, folderMap, sorting, itemsToPrefetch);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gentics.cr.RequestProcessor#finalize()
	 */
	@Override
	public void finalize() {
	}

}
