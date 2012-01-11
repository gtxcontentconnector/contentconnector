package com.gentics.cr.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.util.file.DirectoryScanner;
import com.gentics.cr.util.file.FileTypeDetector;

/**
 * a simple file system request processor that just returns the list of files in a directory as {@link CRResolvableBean}s
 * @author bigbear3001
 *
 */
public class SimpleFileSystemRequestProcessor extends RequestProcessor {

	
	/**
	 * Initialize a new instance
	 * @param config - configuration for the request processor. these properties are required in the config:
	 * <ul>
	 * <li>directory - the directory to scan for files, absolute or relative to the jvm start directory</li>
	 * <li>filter - regular expression for the filenames of the files to list, if not given all files are listed</li>
	 * </ul>
	 * @throws CRException - in case no configuration is given or the PlinkProcessor cannot be initialized
	 */
	public SimpleFileSystemRequestProcessor(CRConfig config) throws CRException {
		super(config);
	}

	@Override
	public Collection<CRResolvableBean> getObjects(CRRequest request,
			boolean doNavigation) throws CRException {
		File directory = new File(config.getString("directory"));
		String filterExpression = config.getString("filter");
		File[] files = DirectoryScanner.listFiles(directory, filterExpression);
		if(files != null) {
			ArrayList<CRResolvableBean> result = new ArrayList<CRResolvableBean>();
			for(File file : files) {
				CRResolvableBean bean = new CRResolvableBean();
				bean.set("filename", file.getName());
				bean.set("pub_dir", directory.toURI().relativize(file.toURI()));
				bean.set("obj_type", FileTypeDetector.getObjType(file));
				bean.set("timestamp", file.lastModified() / 1000);
				//TODO add the content of the file as stream in the right attribute
			}
			return result;
		}
		return new ArrayList<CRResolvableBean>(0);
	}

	@Override
	public void finalize() { }

}
