package com.gentics.cr.util.indexing;

import java.util.concurrent.ConcurrentHashMap;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRConfigUtil;

/**
 * DummyIndexLocation for Testing
 * 
 * @author patrickhoefer
 */
public class DummyIndexLocation2 extends IndexLocation {

	
	/**
	 * @param givenConfig configuration for DummyIndexLocation
	 */
	protected DummyIndexLocation2(CRConfig givenConfig) {
		super(givenConfig);
	}

	@Override
	public void createReopenFile() {
		// do nothing
	}

	@Override
	public void checkLock() throws Exception {
		// do nothing
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
		// do nothing
	}

	/**
	 * Overrides createCRIndexJob and print out which index Job was created. Is used in CreateAllCRIndexJobsOrderTest.
	 */
	public boolean createCRIndexJob(CRConfig config,
			ConcurrentHashMap<String, CRConfigUtil> configmap) {
		System.out.print("Create Job: " + config.getName()+" ");
		return true;
	}

}
