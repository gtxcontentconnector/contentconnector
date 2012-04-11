package com.gentics.cr.util.file;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import com.gentics.cr.CRResolvableBean;

/**
 * Utility class to detect the obj_type attribute of a file when represented in a {@link CRResolvableBean}
 * @author bigbear3001
 *
 */
public class FileTypeDetector {

	/**
	 * this is the collection of all extensions that should be page objects.
	 */
	final static Collection<String> PAGE_EXTENSIONS = Arrays.asList(new String[] { "html", "htm", "inc", "php" });

	/**
	 * @param file - file to get the obj_type for
	 * @return the obj_type attribute of an {@link CRResolvableBean} representing this file.
	 * <ul>
	 * <li>"10007" if it is supposed to be a page</li>
	 * <li>"10008" if it is supposed to be a file</li>
	 * <li>"10002" if it is supposed to be a folder/directory</li>
	 * </ul> 
	 */
	public static String getObjType(File file) {
		if (file.isDirectory()) {
			return "10002";
		} else {
			String extension = file.getName().replaceAll("^.*\\.", "");
			if (PAGE_EXTENSIONS.contains(extension)) {
				return "10007";
			} else {
				return "10008";
			}
		}
	}
}
