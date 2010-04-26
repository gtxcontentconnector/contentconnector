package com.gentics.cr;

import java.util.ArrayList;
import java.util.Collection;

import com.gentics.cr.exceptions.CRException;
/**
 * 
 * Last changed: $Date: 2010-04-01 15:24:02 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 541 $
 * @author $Author: supnig@constantinopel.at $
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
