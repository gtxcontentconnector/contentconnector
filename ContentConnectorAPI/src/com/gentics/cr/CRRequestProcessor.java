package com.gentics.cr;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import com.gentics.api.lib.datasource.DatasourceException;
import com.gentics.api.lib.exception.ParserException;
import com.gentics.api.lib.expressionparser.ExpressionParserException;
import com.gentics.api.lib.expressionparser.filtergenerator.DatasourceFilter;
import com.gentics.api.lib.resolving.Resolvable;

public class CRRequestProcessor extends RequestProcessor{

	private HashMap<String, Resolvable> resolvables = null;
	
	/**
	 * Create a new instance of CRRequestProcessor
	 * @param config
	 * @throws CRException
	 */
	public CRRequestProcessor(CRConfig config) throws CRException {
		super(config);
	}
	
	
	
	/**
	 * 
	 * Fetch the matching objects using the given CRRequest. 
	 * @param request CRRequest
	 * @param doNavigation defines if to fetch child elements
	 * @return resulting objects
	 */
	public Collection<CRResolvableBean> getObjects(CRRequest request, boolean doNavigation) throws CRException
	{
		
		DatasourceFilter dsFilter;
		Vector<CRResolvableBean> collection = new Vector<CRResolvableBean>();
		if (request != null) {
			
			// Parse the given expression and create a datsource filter
			try {
				
				if (this.config.getDatasource() == null) {
					throw (new DatasourceException("No Datasource available."));
				}

				dsFilter = request.getPreparedFilter(config);
				
				// add base resolvables
				if (this.resolvables != null) {
					for (Iterator<String> it = this.resolvables.keySet().iterator(); it
							.hasNext();) {
						String name = it.next();
						dsFilter.addBaseResolvable(name,
								this.resolvables.get(name));
					}
				}

				// do the query
				Collection<Resolvable> col = this.toResolvableCollection(this.config.getDatasource().getResult(
						dsFilter, request.getAttributeArray(), request.getStart().intValue(),
						request.getCount().intValue(), request.getSorting()));

				// convert all objects to serializeable beans
				if (col != null) {
					for (Iterator<Resolvable> it = col.iterator(); it.hasNext();) {
						CRResolvableBean crBean = new CRResolvableBean(
								it.next(), request.getAttributeArray());
						if(this.config.getFolderType().equals(crBean.getObj_type())&& doNavigation)
						{
							//Process child elements
							String fltr="object.folder_id=='"+crBean.getContentid()+"'";
							if(request.getChildFilter()!=null)
								fltr+="AND ("+request.getChildFilter()+")";
							//If object is a folder => retrieve the childs of the object
							CRRequest childReq = request.Clone();
							
							childReq.setRequestFilter(fltr);
							crBean.fillChildRepository(this.getNavigation(childReq));
						}
						
						collection.add(this.replacePlinks(crBean,request));
					}
				}

			} catch (ParserException e) {
				e.printStackTrace();
				throw new CRException("ParserException",e.getMessage());
			} catch (ExpressionParserException e) {
				e.printStackTrace();
				throw new CRException("ExpressionParserException",e.getMessage());
			} catch (DatasourceException e) {
				e.printStackTrace();
				throw new CRException("DatasourceException",e.getMessage());
			}
		}
		return collection;
	}

}
