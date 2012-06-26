package com.gentics.cr.util.indexing.update.filesystem;

import com.gentics.cr.CRConfigUtil;

public class FileSystemUpdateCheckerTest extends AbstractFileSystemUpdateCheckerTest {
	
	@Override
	protected CRConfigUtil prepareConfig(final CRConfigUtil config) {
		config.set("ignorePubDir", "true");
		return config;
	}

}
