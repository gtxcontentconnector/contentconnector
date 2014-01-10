package com.gentics.cr.util.indexing;

import com.gentics.cr.CRConfig;

/**
 * 
 * @author markus.burchhart@s-itsolutions.at
 *
 */
public class ReIndexNoSkipStrategy implements IReIndexStrategy {

	public ReIndexNoSkipStrategy(final CRConfig config) {
	}

	public boolean skipReIndex(IndexLocation indexLocation) {
		return false;
	}
}
