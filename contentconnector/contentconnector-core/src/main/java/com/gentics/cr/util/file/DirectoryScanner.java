package com.gentics.cr.util.file;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.oro.io.Perl5FilenameFilter;

/**
 * Utility to scan a directory for files.
 * @author bigbear3001
 *
 */
public class DirectoryScanner {

	/**
	 * since there is no way arrays can be manipulated we don't have to generate lots of empty arrays.
	 */
	private static final File[] EMPTY_RESULT = new File[] {};

	/**
	 * since there is no way arrays can be manipulated we don't have to generate lots of empty arrays.
	 */
	private static final String[] EMPTY_STRING_RESULT = new String[] {};

	/**
	 * Helper method that doesn't return null if no files where found. instead it returns an empty array which makes it easier to handle.
	 * @param directory - directory to scan for files.
	 * @param filterExpression - expression the returned files have to match. if this parameter is null all files will be returned.
	 * @return Array of files in the directory matching the expresssion. Empty Array if no files match the expression.
	 */
	public static File[] listFiles(final File directory, final String filterExpression) {
		ArrayList<File> files = new ArrayList<File>();
		File[] baseFiles = directory.listFiles(generateFilter(filterExpression));
		if (baseFiles != null) {
			files.addAll(Arrays.asList(baseFiles));
			for (File file : baseFiles) {
				if (file.isDirectory()) {
					File[] children = listFiles(file, filterExpression);
					files.addAll(Arrays.asList(children));
					if (filterExpression != null && !file.getName().matches(filterExpression)) {
						files.remove(file);
					}
				}
			}
			return files.toArray(new File[files.size()]);
		}
		return EMPTY_RESULT;
	}

	/**
	 * Helper method that doesn't return null if no files where found. instead it returns an empty array which makes it easier to handle.
	 * @param directory - directory to scan for files.
	 * @param filterExpression - expression the returned files have to match. if this parameter is null all files will be returned.
	 * @return Array of Strings with the names of the files in the directory matching the expresssion. Empty Array if no files match the 
	 * expression.
	 */
	public static String[] list(final File directory, final String filterExpression) {
		ArrayList<String> files = new ArrayList<String>();
		String[] baseFiles = directory.list(generateFilter(filterExpression));
		if (baseFiles != null) {
			files.addAll(Arrays.asList(baseFiles));
			for (String filename : baseFiles) {
				File file = new File(directory, filename);
				if (file.isDirectory()) {
					String[] children = list(file, filterExpression);
					for (String child : children) {
						files.add(filename + File.separator + child);
					}
					if (filterExpression != null && !filename.matches(filterExpression)) {
						files.remove(filename);
					}
				}
			}
			return files.toArray(new String[files.size()]);
		}
		return EMPTY_STRING_RESULT;
	}

	/**
	 * @param filterExpression - expression that we should generate the filename filter for.
	 * @return FilenameFilter for the expression, in case the expression was null a filter that matches all files is returned.
	 */
	private static FilenameFilter generateFilter(final String filterExpression) {
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(final File dir, final String name) {
				return true;
			}
		};
		if (filterExpression != null) {
			filter = new DirectoryIgnoringFilenameFilter(filterExpression);
		}
		return filter;
	}
	
	private static class DirectoryIgnoringFilenameFilter extends Perl5FilenameFilter {
		
		public DirectoryIgnoringFilenameFilter(String filterExpression) {
			super(filterExpression);
		}

		@Override
		public boolean accept(File dir, String filename) {
			if(new File(dir, filename).isDirectory()) {
				return true;
			}
			return super.accept(dir, filename);
		}
	}
}
