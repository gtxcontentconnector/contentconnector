package com.gentics.cr.util.indexing.update.filesystem;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.util.indexing.IndexUpdateChecker;

public class FileSystemUpdateChecker extends IndexUpdateChecker {

	File directory;
	
	boolean ignorePubDir;
	
	List<String> files;
	
	public FileSystemUpdateChecker(GenericConfiguration config) {
		directory = new File(config.getString("directory"));
		ignorePubDir = config.getBoolean("ignorePubDir");
		files = new ArrayList<String>(Arrays.asList(directory.list()));
	}

	@Override
	protected boolean checkUpToDate(String identifyer, Object timestamp,
			String timestampattribute, Resolvable object) {
		CRResolvableBean bean = new CRResolvableBean(object);
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
			Integer updatetimestamp = (Integer) timestamp;
			File file = new File(new File(directory, publicationDirectory), filename);
			files.remove(publicationDirectory + filename);
			if(file.exists() && file.isFile() && (file.lastModified() / 1000) >= updatetimestamp) {
				return true;
			}
		} else if(!ignorePubDir) {
			//it would just make no sense to check for check for folders existence if the pub_dir attribute is ignored
			String publicationDirectory = bean.getString("pub_dir");
			File file = new File(directory, publicationDirectory);
			files.remove(publicationDirectory);
			if (file.exists() && file.isDirectory()) {
				return true;
			}
		} else {
			return true;
		}
		return false;
	}

	protected static void assertNotNull(String message, Object object) {
		if (object == null) {
			throw new RuntimeException(new CRException(message));
		}
	}

	@Override
	public void deleteStaleObjects() {
		for(String filename : files) {
			File file = new File(directory, filename);
			file.delete();
		}
	}

}
