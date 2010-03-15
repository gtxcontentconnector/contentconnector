package com.gentics.cr;

import java.util.ArrayList;
import java.util.Collection;

import com.gentics.cr.exceptions.CRException;
/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class RPMergerRequestProcessor extends RequestProcessor {

	private RequestProcessor rp1;
	private RequestProcessor rp2;
	private String mergeattribute;
	
	private static final String MERGE_ATTRIBUTE_KEY="mergeattribute";
	
	/**
	 * 
	 * @param config
	 * @throws CRException
	 */
	public RPMergerRequestProcessor(CRConfig config) throws CRException {
		super(config);
		this.rp1 = config.getNewRequestProcessorInstance(1);
		this.rp2 = config.getNewRequestProcessorInstance(2);
		this.mergeattribute = (String)config.get(MERGE_ATTRIBUTE_KEY);
	}

	
	/**
	 * Merges two reuquest processor instances configured in the config
	 */
	@Override
	public Collection<CRResolvableBean> getObjects(CRRequest request,
			boolean doNavigation) throws CRException {
		ArrayList<CRResolvableBean> coll = null;
		
		coll = (ArrayList<CRResolvableBean>) RequestProcessorMerger.merge(this.mergeattribute, this.rp1, this.rp2, request);
		
		return coll;
	}


}
