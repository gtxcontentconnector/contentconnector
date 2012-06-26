package com.gentics.cr.util.indexing.update.filesystem;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRResolvableBean;

public abstract class AbstractFileSystemUpdateCheckerTest {
	
	protected FileSystemUpdateChecker checker;

	private File newFile;

	private File outdatedFile;

	protected File upToDateFile;

	private File removeFile;
	
	File directory;
	
	@Before
	public void prepare() throws IOException, URISyntaxException {
		prepareDirectory();
		CRConfigUtil config = new CRConfigUtil();
		config.set("directory", directory.getPath());
		//TODO add test for ignorePubDir false
		config.set("ignorePubDir", "false");
		//This is important if you delete the filter the test may delete your .class files.
		config.set("filter", ".*\\.file");
		config = prepareConfig(config);
		checker = new FileSystemUpdateChecker(config);
	}

	/**
	 * prepared the flat directory to contain nothing else than an outdated.file, uptodate.file and remove.file.
	 * @throws URISyntaxException - should never happen
	 * @throws IOException - in case we cannot access the filesystem correctly
	 */
	private void prepareDirectory() throws URISyntaxException, IOException {
		URL outdatedFileURL = FileSystemUpdateCheckerTestWithPubDirectory.class.getResource(
				"flatdirectory" + File.separator + "outdated.file");
		if (outdatedFileURL != null) {
			directory = new File(outdatedFileURL.toURI()).getParentFile();
		} else {
			URL currentDirectory = FileSystemUpdateCheckerTestWithPubDirectory.class.getResource(".");
			directory = new File(new File(currentDirectory.toURI()), "flatdirectory");
			directory.mkdirs();
		}
		newFile = new File(directory, "new.file");
		outdatedFile = new File(directory, "outdated.file");
		if (!outdatedFile.exists()) {
			outdatedFile.createNewFile();
		}
		upToDateFile = new File(directory, "uptodate.file");
		if (!upToDateFile.exists()) {
			upToDateFile.createNewFile();
		}
		removeFile = new File(directory, "remove.file");
		if (!removeFile.exists()) {
			removeFile.createNewFile();
		}
	}
	
	protected abstract CRConfigUtil prepareConfig(CRConfigUtil config);

	@Test
	public void testNewFile() {

		boolean result = checkUpToDate(newFile);
		assertFalse("the filesystem update checker didn't return false for a nonexistant file (" + newFile.getPath()
				+ ")", result);
	}

	@Test
	public void testOutdatedFile() {
		boolean result = checkUpToDate(outdatedFile, 100);
		assertFalse("the filesystem update checker didn't return false for an outdated file (" + outdatedFile.getPath()
				+ ")", result);
	}

	@Test
	public void testUpToDateFile() {
		boolean result = checkUpToDate(upToDateFile, -100);
		assertTrue("the filesystem update checker didn't return true for an up to date file (" + upToDateFile.getPath()
				+ ")", result);
	}

	@Test
	public void testRemoveFile() {
		assertTrue("Remove file doesn't exist, therefore it can't be removed.", removeFile.exists());
		checkUpToDate(outdatedFile);
		checkUpToDate(upToDateFile);
		checker.deleteStaleObjects();
		assertFalse("Not checked remove file wasn't deleted in deleteStaleObjects()", removeFile.exists());
	}

	@Test
	public void testAssertNotNull() {
		String notNull = "";
		String isNull = null;
		String nullMessage = "Object is null";
		try {
			FileSystemUpdateChecker.assertNotNull("assertNotNull should not return an exception for a not null object", notNull);
		} catch (Throwable t) {
			throw new AssertionError(t);
		}

		boolean catched = false;
		try {
			FileSystemUpdateChecker.assertNotNull(nullMessage, isNull);
		} catch (Throwable t) {
			catched = true;
			if (!t.getCause().getMessage().equals(nullMessage)) {
				throw new AssertionError("The assertNotNull function didn't return the correct message.");
			}
		}
		if (!catched) {
			throw new AssertionError("The assertNotNull function didn't throw a throwable for a null object.");
		}

	}
	
	
	/**
	 * Check if the file is up to date with the {@link #checker}.
	 * @param file - file to check
	 * @param timeDifference - difference in seconds to the files modify timestamp
	 * @return <code>true</code> if the file is up to date. <code>false</code> otherwhise.
	 */
	protected boolean checkUpToDate(final File file, final int timeDifference) {
		Integer timestamp = ((int) (file.lastModified() / 1000)) + timeDifference;
		CRResolvableBean resolvable = prepareResolvable(file, timestamp);
		return checker.checkUpToDate("new", timestamp, "timestamp", resolvable);
	}

	protected CRResolvableBean prepareResolvable(final File file,
			Integer timestamp) {
		CRResolvableBean resolvable = new CRResolvableBean();
		resolvable.set("timestamp", timestamp);
		resolvable.set("filename", file.getName());
		resolvable.set("pub_dir", directory.toURI().relativize(file.getParentFile().toURI()));
		return resolvable;
	}

	protected boolean checkUpToDate(File file) {
		return checkUpToDate(file, 0);
	}
}
