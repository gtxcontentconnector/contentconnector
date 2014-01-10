package com.gentics.cr.util.indexing;

import com.gentics.cr.CRConfig;

/**
 * DummyIndexLocationFactory to the the DummyIndexLocation directly in the Tests
 * @author bigbear3001
 *
 */
public class DummyIndexLocationFactory {

	/**
	 * @param config - configuration for the DummyIndexLocation.
	 * @return the configured DummyIndexLocation.
	 */
	public static DummyIndexLocation getDummyIndexLocation(CRConfig config) {
		return new DummyIndexLocation(config);
	}
}
