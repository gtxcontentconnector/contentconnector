package com.gentics.cr.util.file;

import java.io.File;
import java.io.FilenameFilter;

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
	public static File[] listFiles(File directory, String filterExpression) {
		File[] files = directory.listFiles(generateFilter(filterExpression));
		if (files != null) {
			return files;
		}
		return EMPTY_RESULT;
	}

	/**
	 * Helper method that doesn't return null if no files where found. instead it returns an empty array which makes it easier to handle.
	 * @param directory - directory to scan for files.
	 * @param filterExpression - expression the returned files have to match. if this parameter is null all files will be returned.
	 * @return Array of Strings with the names of the files in the directory matching the expresssion. Empty Array if no files match the expression.
	 */
	public static String[] list(File directory, String filterExpression) {
		String[] files = directory.list(generateFilter(filterExpression));
		if (files != null) {
			return files;
		}
		return EMPTY_STRING_RESULT;
	}

	/**
	 * @param filterExpression - expression that we should generate the filename filter for.
	 * @return FilenameFilter for the expression, in case the expression was null a filter that matches all files is returned.
	 */
	private static FilenameFilter generateFilter(String filterExpression) {
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return true;
			}
		};
		if (filterExpression != null) {
			filter = new Perl5FilenameFilter(filterExpression);
		}
		return filter;
	}
}
