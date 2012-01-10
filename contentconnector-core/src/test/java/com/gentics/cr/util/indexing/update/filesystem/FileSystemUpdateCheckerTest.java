package com.gentics.cr.util.indexing.update.filesystem;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRResolvableBean;

public class FileSystemUpdateCheckerTest {
	
	File directory;
	
	CRConfigUtil config;
	
	FileSystemUpdateChecker checker;
	
	CRResolvableBean resolvable;

	private File newFile;

	private File outdatedFile;

	private File upToDateFile;
	
	private File removeFile;
	

	@Before
	public void setUp() throws URISyntaxException, IOException {
		directory = new File(FileSystemUpdateCheckerTest.class.getResource("outdated.file").toURI()).getParentFile();
		newFile = new File(directory, "new.file");
		outdatedFile = new File(directory, "outdated.file");
		upToDateFile = new File(directory, "uptodate.file");
		removeFile = new File(directory, "remove.file");
		if (!removeFile.exists()) {
			removeFile.createNewFile();
		}
		config = new CRConfigUtil();
		config.set("directory", directory.getPath());
		//TODO add test for ignorePubDir false
		config.set("ignorePubDir", "true");
		checker = new FileSystemUpdateChecker(config);
		resolvable = new CRResolvableBean();
	}
	
	@Test
	public void testNewFile() {
		
		boolean result = checkUpToDate(newFile);
		assertFalse("the filesystem update checker didn't return false for a nonexistant file (" + newFile.getPath() + ")", result);
	}
	
	@Test
	public void testOutdatedFile() {
		boolean result = checkUpToDate(outdatedFile, 100);
		assertFalse("the filesystem update checker didn't return false for an outdated file (" + outdatedFile.getPath() + ")", result);
	}
	
	@Test
	public void testUpToDateFile() {
		boolean result = checkUpToDate(upToDateFile, -100);
		assertTrue("the filesystem update checker didn't return true for an up to date file (" + upToDateFile.getPath() + ")", result);
	}
	
	
	@Test
	public void testRemoveFile() {
		assertTrue("Remove file doesn't exist, therefore it can't be removed.", removeFile.exists());
		checkUpToDate(outdatedFile);
		checkUpToDate(upToDateFile);
		checker.deleteStaleObjects();
		assertFalse("Not checked remove file wasn't deleted in deleteStaleObjects()", removeFile.exists());
	}
	
	

	/**
	 * Check if the file is up to date with the {@link #checker}
	 * @param file - file to check
	 * @param timeDifference - difference in seconds to the files modify timestamp
	 * @return <code>true</code> if the file is up to date. <code>false</code> otherwhise.
	 */
	private boolean checkUpToDate(File file, int timeDifference) {
		Integer timestamp = ((int) (file.lastModified() / 1000)) + timeDifference;
		resolvable.set("timestamp", timestamp);
		resolvable.set("filename", file.getName());
		return checker.checkUpToDate("new", timestamp, "timestamp", resolvable);
	}

	private boolean checkUpToDate(File file) {
		return checkUpToDate(file, 0);
	}
}
