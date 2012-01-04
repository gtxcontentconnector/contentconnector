package com.gentics.cr.util.indexing.update.filesystem;

import static com.gentics.cr.util.indexing.update.filesystem.FileSystemUpdateChecker.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.util.indexing.AbstractUpdateCheckerJob;
import com.gentics.cr.util.indexing.IndexLocation;

public class FileSystemUpdateJob extends AbstractUpdateCheckerJob {

	File directory;
	
	boolean ignorePubDir;
	
	public FileSystemUpdateJob(CRConfig config,
			IndexLocation indexLoc,
			Hashtable<String, CRConfigUtil> updateCheckerConfigmap) throws FileNotFoundException {
		super(config, indexLoc, updateCheckerConfigmap);
		
		try {
			rp = config.getNewRequestProcessorInstance(1);
		} catch (CRException e) {
			log.error("Could not create RequestProcessor instance." 
					+ config.getName(), e);
		}
		
		indexUpdateChecker = new FileSystemUpdateChecker(config.getSubConfig("updatejob"));
		ignorePubDir = config.getBoolean("updatejob.ignorePubDir");
		directory = new File(config.getString("updatejob.directory"));
		if(config.getBoolean("updatejob.createNonExistentDirectory") && !directory.exists()) {
			if(!directory.mkdirs()) {
				throw new FileNotFoundException("The directory " + directory + " cannot be created.");
			}
		}
		if(!directory.exists()) {
			throw new FileNotFoundException("The directory " + directory + " cannot be found.");
		}
	}
	
	RequestProcessor rp;
	
	FileSystemUpdateChecker indexUpdateChecker;
	
	

	@Override
	protected void indexCR(IndexLocation indexLocation, CRConfigUtil config)
			throws CRException {
		Collection<CRResolvableBean> objectsToIndex = null;
		try {
			CRRequest req = new CRRequest();
			req.setRequestFilter("1==1");
			status.setCurrentStatusString("Get objects to update in the index ...");
				objectsToIndex = getObjectsToUpdate(req, rp, false, indexUpdateChecker);
			} catch (Exception e) {
				log.error("ERROR while cleaning index", e);
			}
			
			for(CRResolvableBean bean : objectsToIndex) {
				if(!"10002".equals(bean.getObj_type())) {
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
					if(file.isDirectory()) {
						file.delete();
					}
					try {
						if(!file.exists()) {
							file.createNewFile();
						}
						FileWriter writer = new FileWriter(file);
						String contentAttribute;
						if("10007".equals(bean.getObj_type())) {
							contentAttribute = "content";
						} else {
							contentAttribute = "binarycontent";
						}
						writer.write(bean.getString(contentAttribute));
						writer.close();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				} else if(!ignorePubDir) {
					//it would just make no sense to check for check for folders existence if the pub_dir attribute is ignored
					String publicationDirectory = bean.getString("pub_dir");
					File file = new File(directory, publicationDirectory);
					file.mkdirs();
				}
			}

	}

}
