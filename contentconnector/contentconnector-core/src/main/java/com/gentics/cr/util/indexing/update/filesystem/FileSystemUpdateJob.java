package com.gentics.cr.util.indexing.update.filesystem;

import static com.gentics.cr.util.indexing.update.filesystem.FileSystemUpdateChecker.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Hashtable;

import org.apache.commons.io.output.FileWriterWithEncoding;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.util.indexing.AbstractUpdateCheckerJob;
import com.gentics.cr.util.indexing.DummyIndexLocation;
import com.gentics.cr.util.indexing.IndexLocation;

/**
 * Update the files in a directory
 * @author bigbear3001
 *
 */
public class FileSystemUpdateJob extends AbstractUpdateCheckerJob {

	/**
	 * directory to put the files in.
	 */
	File directory;

	/**
	 * defines if we should ignore the pub_dir attribute of the resolvables. if <code>true</code> all files are put into one directory, therefore files with the same filename can override each other.
	 */
	boolean ignorePubDir;

	/**
	 * Create a new FileSystemUpdateJob
	 * @param config - configuration of the job.
	 * @param indexLoc - index location in this case just a {@link DummyIndexLocation} as currently the directory is gotten from the config parameter "updatejob.directory"
	 * @param updateCheckerConfigmap - map with all configured index parts
	 * @throws FileNotFoundException in case the directory doesn't exist and creation of the directory is deactivated or the directory cannot be created.
	 */
	public FileSystemUpdateJob(CRConfig config, IndexLocation indexLoc,
		Hashtable<String, CRConfigUtil> updateCheckerConfigmap) throws FileNotFoundException {
		super(config, indexLoc, updateCheckerConfigmap);

		try {
			rp = config.getNewRequestProcessorInstance(1);
		} catch (CRException e) {
			log.error("Could not create RequestProcessor instance." + config.getName(), e);
		}

		indexUpdateChecker = new FileSystemUpdateChecker(config.getSubConfig("updatejob"));
		ignorePubDir = config.getBoolean("updatejob.ignorePubDir");
		directory = new File(config.getString("updatejob.directory"));
		if (config.getBoolean("updatejob.createNonExistentDirectory") && !directory.exists()) {
			if (!directory.mkdirs()) {
				throw new FileNotFoundException("The directory " + directory + " cannot be created.");
			}
		}
		if (!directory.exists()) {
			throw new FileNotFoundException("The directory " + directory + " cannot be found.");
		}
	}

	/**
	 * RequestProcessor to get the objects from.
	 */
	RequestProcessor rp;

	/**
	 * the update checker that checks if the file has to be updated in the directory
	 */
	FileSystemUpdateChecker indexUpdateChecker;

	/**
	 * get the objects to update and update them in the directory. deletion of old/stale objects is handled by the update checker
	 */
	@Override
	protected void indexCR(IndexLocation indexLocation, CRConfigUtil config) throws CRException {
		Collection<CRResolvableBean> objectsToIndex = null;
		try {
			CRRequest req = new CRRequest();
			req.setRequestFilter(config.getString("rule", "1==1"));
			status.setCurrentStatusString("Get objects to update in the directory ...");
			objectsToIndex = getObjectsToUpdate(req, rp, false, indexUpdateChecker);
		} catch (Exception e) {
			log.error("ERROR while cleaning index", e);
		}
		status.setCurrentStatusString("Update the objects in the directory ...");
		for (CRResolvableBean bean : objectsToIndex) {
			if (!"10002".equals(bean.getObj_type())) {
				String publicationDirectory;
				if (ignorePubDir) {
					publicationDirectory = "";
				} else {
					publicationDirectory = bean.getString("pub_dir");
				}
				String filename = bean.getString("filename");
				assertNotNull("Bean " + bean.getContentid() + " has no attribute pub_dir.", publicationDirectory);
				assertNotNull("Bean " + bean.getContentid() + " has no attribute filename.", filename);
				File file = new File(new File(directory, publicationDirectory), filename);
				if (file.isDirectory()) {
					file.delete();
				}
				try {
					if (!file.exists()) {
						file.createNewFile();
					}
					if ("10007".equals(bean.getObj_type())) {
						FileWriterWithEncoding writer = new FileWriterWithEncoding(file, "UTF-8");
						writer.write(bean.getContent());
						writer.close();
					} else {
						FileOutputStream os = new FileOutputStream(file);
						//TODO use a stream to consume lower memory for large files
						os.write(bean.getBinaryContent());
						os.close();
					}
				} catch (Exception e) {
					throw new CRException("Cannot update the index.", e);
				}

			} else if (!ignorePubDir) {
				//it would just make no sense to check for check for folders existence if the pub_dir attribute is ignored
				String publicationDirectory = bean.getString("pub_dir");
				File file = new File(directory, publicationDirectory);
				file.mkdirs();
			}
		}

	}

}
