package com.gentics.cr.util.indexing.update.filesystem;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRResolvableBean;


//TODO add tests for subdirectories
public class FileSystemUpdateCheckerTestWithPubDirectory extends AbstractFileSystemUpdateCheckerTest {

	@Override
	protected CRConfigUtil prepareConfig(final CRConfigUtil config) {
		config.set("ignorePubDir", "false");
		return config;
	}
	
	@Test
	public void testUpToDateFileWithSlashDirectory() {
		Integer timestamp = ((int) (upToDateFile.lastModified() / 1000)) - 100;
		CRResolvableBean resolvable = prepareResolvable(upToDateFile, timestamp);
		resolvable.set("pub_dir", "/");
		boolean result = checker.checkUpToDate("new", timestamp, "timestamp", resolvable);
		assertTrue("the filesystem update checker didn't return true for an up to date file (" + upToDateFile.getPath()
				+ ")", result);
		checker.deleteStaleObjects();
		assertTrue("up to date file was deleted after if was decided its up to date.", upToDateFile.exists());
	}

}
