package com.gentics.cr.util.indexing;

import com.gentics.cr.CRConfig;
/**
 * 
 * Last changed: $Date: 2009-07-10 10:49:03 +0200 (Fr, 10 Jul 2009) $
 * @version $Revision: 131 $
 * @author $Author: supnig@constantinopel.at $
 *
 */

public class DefaultIndexLocation extends IndexLocation {

	protected DefaultIndexLocation(CRConfig config) {
		super(config);
	}

	@Override
	public void checkLock() throws Exception {
		throw new UnsupportedOperationException();

	}


	@Override
	public void createReopenFile() {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public void finalize() {
		// TODO Auto-generated method stub
		
	}

}
