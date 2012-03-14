package com.gentics.cr.util.indexing;

import com.gentics.cr.CRConfig;

/**
 * this is a dummy index location that allows you to retrieve updates of contentrepository objects
 * but is missing the part that keeps the filesystem up to date with the memory in multiple jvms
 * @author bigbear3001
 */
public class DummyIndexLocation extends IndexLocation {

	protected DummyIndexLocation(CRConfig givenConfig) {
		super(givenConfig);
	}

	@Override
	public void createReopenFile() {
		//do nothing
	}

	@Override
	public void checkLock() throws Exception {
		//do nothing
	}

	@Override
	public boolean isOptimized() {
		return false;
	}

	@Override
	public boolean isLocked() {
		return false;
	}

	@Override
	protected void finalize() {
		//do nothing
	}

}
