package com.gentics.cr.mccr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.gentics.api.lib.datasource.ChannelTree;
import com.gentics.api.lib.datasource.ChannelTreeNode;
import com.gentics.api.lib.datasource.Datasource.Sorting;
import com.gentics.api.lib.datasource.DatasourceException;
import com.gentics.api.lib.etc.ObjectTransformer;
import com.gentics.api.lib.exception.NodeException;
import com.gentics.api.lib.exception.ParserException;
import com.gentics.api.lib.expressionparser.ExpressionParserException;
import com.gentics.api.lib.expressionparser.filtergenerator.DatasourceFilter;
import com.gentics.api.lib.expressionparser.filtergenerator.FilterGeneratorException;
import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.api.portalnode.connector.PortalConnectorFactory;
import com.gentics.cr.CRConfig;
import com.gentics.cr.CRDatabaseFactory;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRRequestProcessor;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.util.ArrayHelper;
import com.gentics.lib.datasource.mccr.MCCRDatasource;

/**
 * The MCCRAllChannelsRequestProcessor can be used to fetch all objects from all channels that match the given request filter.
 * This is especially useful for indexing MCCRs with a channel structure. 
 * @author Christopher
 *
 */
public class MCCRAllChannelsRequestProcessor extends RequestProcessor {

	private static Logger logger = Logger.getLogger(CRRequestProcessor.class);
	
	public static final String UNIQUE_ID_KEY = "uniqueid";
	public static final String CHANNEL_ID_KEY = "channel_id";
	public static final String CHANNELSET_ID_KEY = "channelset_id";
	
	public MCCRAllChannelsRequestProcessor(CRConfig config) throws CRException {
		super(config);
	}

	/**
	 * 
	 */
	@Override
	public Collection<CRResolvableBean> getObjects(CRRequest request,
			boolean doNavigation) throws CRException {
		MCCRDatasource ds = null;
		DatasourceFilter dsFilter;
		Vector<CRResolvableBean> collection = new Vector<CRResolvableBean>();
		if (request != null) {

			// Parse the given expression and create a datasource filter
			try {
				ds = (MCCRDatasource) this.config.getDatasource();
				if (ds == null) {
					throw (new DatasourceException("No Datasource available."));
				}
				CRRequest nodeRequest = request.Clone();
				nodeRequest.setRequestFilter("(" + request.getRequestFilter() + ") AND object.channel_id == channelinfo.channelid");
				
				dsFilter = nodeRequest.getPreparedFilter(config, ds);
				
				//Check Sorting for nodeRequest (we do not have the column uniqueid => we have to remove the sorting)
				Sorting[] nodeRequestSorting = nodeRequest.getSorting();
				ArrayList<Sorting> newSorting = new ArrayList<Sorting>();
				for(Sorting s:nodeRequestSorting) {
					if (!UNIQUE_ID_KEY.equalsIgnoreCase(s.getColumnName())) {
						newSorting.add(s);
					}
				}
				Sorting[] newSortingArray = newSorting.toArray(new Sorting[0]);
				nodeRequest.setSorting(newSortingArray);

				// add base resolvables
				if (this.resolvables != null) {
					for (Iterator<String> it = this.resolvables.keySet().iterator(); it.hasNext();) {
						String name = it.next();
						dsFilter.addBaseResolvable(name, this.resolvables.get(name));
					}
				}

				String[] prefillAttributes = request.getAttributeArray();
				prefillAttributes = ArrayHelper.removeElements(prefillAttributes, "contentid", "updatetimestamp");
				
				//Load Channel Information
				ChannelTree tree = ds.getChannelStructure();
				
				Collection<Resolvable> col = new Vector<Resolvable>();
				
				fillCollectionWithChannelResults(col, ds, dsFilter,
						prefillAttributes, nodeRequest, tree.getChildren());

				// convert all objects to serializeable beans
				if (col != null) {
					for (Iterator<Resolvable> it = col.iterator(); it.hasNext();) {
						CRResolvableBean crBean = new CRResolvableBean(it.next(), request.getAttributeArray());
						if (config.getFolderType().equals(crBean.getObj_type()) && doNavigation) {
							//Process child elements
							String fltr = "object.folder_id=='" + crBean.getContentid() + "'";
							if (request.getChildFilter() != null) {
								fltr += "AND (" + request.getChildFilter() + ")";
							}
							//If object is a folder => retrieve the children of the object
							CRRequest childReq = request.Clone();
							childReq.setRequestFilter(fltr);
							crBean.fillChildRepository(getNavigation(childReq));
						}
						//ADD UNIQUE ID
						crBean.set(UNIQUE_ID_KEY, crBean.get(CHANNEL_ID_KEY) + "." + crBean.get(CHANNELSET_ID_KEY));
						collection.add(this.replacePlinks(crBean, request));
					}
				}
				//SORT THE COLLECTION BY THE UNIQUE ID
				Sorting[] sorting = request.getSorting();
				if (sorting != null && sorting.length > 0) {
					sortCollection(collection, sorting[0]);
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
			} catch (ClassCastException e) {
				logger.error("Error getting result from Datasource. Is your datasource a MCCR Datasource? Did you set mccr=true for your datasource properties?", e);
				throw new CRException(e);
			} finally {
				CRDatabaseFactory.releaseDatasource(ds);
			}
		}
		return collection;
	}
	
	/**
	 * We do the sorting in memory because we cannot fetch the objects sorted from the database.
	 * @param collection
	 * @param sorting
	 */
	private void sortCollection(List<CRResolvableBean> collection, Sorting sorting) {
		if (sorting != null) {
			String columnName = sorting.getColumnName();
			int order = sorting.getSortOrder();
			Collections.sort(collection, new PNSortingComparator<CRResolvableBean>(columnName, order));
		}
	}

	/**
	 * Traverses the channel structure and fetches the objects from each channel.
	 * @param col
	 * @param ds
	 * @param dsFilter
	 * @param prefillAttributes
	 * @param request
	 * @param nodes
	 * @throws DatasourceException
	 * @throws FilterGeneratorException
	 */
	private void fillCollectionWithChannelResults(
			Collection<Resolvable> col, MCCRDatasource ds,
			DatasourceFilter dsFilter, String[] prefillAttributes, 
			CRRequest request,
			List<ChannelTreeNode> nodes)
			throws DatasourceException, FilterGeneratorException {
		
		for (ChannelTreeNode node : nodes) {
			CRResolvableBean channelinfo = new CRResolvableBean();
			channelinfo.set("channelid", node.getChannel().getId());
			ds.setChannel(node.getChannel().getId());
			dsFilter.addBaseResolvable("channelinfo", channelinfo);
			//Build Node Request
			Collection<Resolvable> nodeResult = ds.getResult(
				dsFilter,
				prefillAttributes,
				request.getStart().intValue(),
				request.getCount().intValue(),
				request.getSorting());
			if (nodeResult != null) {
				col.addAll(nodeResult);
			}
			
			//Recursion
			fillCollectionWithChannelResults(col, ds, dsFilter, prefillAttributes, request, node.getChildren());
		}
	}
	
	/**
	 * We need to use the proper prefill method provided by the API.
	 */
	@Override
	public void fillAttributes(Collection<CRResolvableBean> col, CRRequest request, String idAttribute)
			throws CRException {
		String[] attributes = request.getAttributeArray();
		if (ObjectTransformer.isEmpty(col) || ObjectTransformer.isEmpty(attributes)) {
			return;
		}
		List<Resolvable> resos = new Vector<Resolvable>(col.size());
		for (CRResolvableBean bean : col) {
			resos.add(bean.getResolvable());
		}
		try {
			PortalConnectorFactory.prefillAttributes((MCCRDatasource) this.config.getDatasource(), resos, Arrays.asList(attributes));
		} catch (NodeException e) {
			logger.error("Error while prefilling attributes.", e);
		}
	}
	

	@Override
	public void finalize() {
		// TODO Auto-generated method stub

	}

}
