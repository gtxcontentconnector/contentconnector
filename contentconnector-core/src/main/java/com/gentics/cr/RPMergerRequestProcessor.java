package com.gentics.cr;

import java.util.ArrayList;
import java.util.Collection;

import com.gentics.cr.exceptions.CRException;
/**
 * {@link RPMergerRequestProcessor} initializes 2 child
 * {@link RequestProcessor}s and enriches the objects from the first
 * {@link RequestProcessor} with objects from the second
 * {@link RequestProcessor}. Both {@link RequestProcessor}s must have an
 * attribute in their objects that is unique within the {@link RequestProcessor}
 * and identical for both {@link RequestProcessor}s.
 * Last changed: $Date: 2010-04-01 15:24:02 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 541 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class RPMergerRequestProcessor extends RequestProcessor {

	/**
	 * First {@link RequestProcessor} witch gets the objects primarily.
	 */
	private RequestProcessor rp1;
	/**
	 * Second {@link RequestProcessor} witch enriches the objects from the first
	 * {@link RequestProcessor}.
	 */
	private RequestProcessor rp2;
	/**
	 * Attribute to join the objects from both {@link RequestProcessor}s.
	 */
	private String mergeattribute;

	/**
	 * Configuration key for defining the merge attribute.
	 */
	private static final String MERGE_ATTRIBUTE_KEY = "mergeattribute";

	/**
	 * Default value for the merge attribute if it is not definied in the
	 * configuration.
	 */
	private static final String MERGE_ATTRIBUTE_DEFAULT = "contentid";

	/**
	 * Initialize the RequestProcessorMergerRequestProcessor.
	 * @param config Configuration for the RequestProcessor
	 * @throws CRException in case one of the RequestProcessors could not be
	 * initialized.
	 */
	public RPMergerRequestProcessor(final CRConfig config) throws CRException {
		super(config);
		rp1 = config.getNewRequestProcessorInstance(1);
		rp2 = config.getNewRequestProcessorInstance(2);
		mergeattribute =
			config.getString(MERGE_ATTRIBUTE_KEY, MERGE_ATTRIBUTE_DEFAULT);
	}



	@Override
	public final Collection<CRResolvableBean> getObjects(final CRRequest request,
			final boolean doNavigation) throws CRException {
		ArrayList<CRResolvableBean> coll = null;
		coll = (ArrayList<CRResolvableBean>) RequestProcessorMerger
				.merge(mergeattribute, rp1, rp2, request);
		return coll;
	}

	/**
	 * clean up all RequestProcessors we initialized.
	 */
	@Override
	public final void finalize() {
		rp1.finalize();
		rp2.finalize();
	}


}
