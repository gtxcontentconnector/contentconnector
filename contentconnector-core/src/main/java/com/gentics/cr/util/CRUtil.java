package com.gentics.cr.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.gentics.api.lib.datasource.Datasource;
import com.gentics.api.lib.datasource.Datasource.Sorting;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.exceptions.ConfpathConfigurationException;
import com.gentics.cr.util.resolver.CRUtilResolver;

/**
 * 
 * Last changed: $Date: 2010-04-01 15:24:41 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 543 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class CRUtil {

	/**
	 * Utility class.
	 */
	private CRUtil() {
		
	}
	
	/**
	 * marker for properties that shouldn't be resolved.
	 */
	private static final String DONOTRESOLVE_MARKER = "DONOTRESOLVE";
	/**
	 * system property holding the Gentics Portal.Node confpath.
	 */
	public static final String PORTALNODE_CONFPATH = "com.gentics.portalnode.confpath";

	/**
	 * Log4J Logger for debugging purposes.
	 */
	private static final Logger LOGGER = Logger.getLogger(CRUtil.class);
	
	/**
	 * File protocol handler.
	 */
	private static final String FILE_PROTOCOL_IDENTIFIER = "file:/";
	
	/**
	 * Classpath protocol handler.
	 */
	private static final String CLASSPATH_PROTOCOL_IDENTIFIER = "classpath:";


	/**
	 * Convert a String like "contentid:asc,name:desc" into an Sorting Array.
	 * Errorhandling or errorrcognition is poor at that time.
	 * 
	 * @param sortingString like "contentid:asc,name:desc"
	 * @return a Sorting[] Array suitable for contentrepository queries
	 */
	public static final Sorting[] convertSorting(final String sortingString) {

		String[] sortingArray;
		HashSet<Sorting> sortingColl = new HashSet<Sorting>();

		if (sortingString != null) {

			// split sorting attributes on ,.
			sortingArray = sortingString.split(",");

			// look at each attribute for sort direction
			for (int i = 0; i < sortingArray.length; i++) {

				// split attribute on :. First element is attribute name the
				// second is the direction
				String[] sort = sortingArray[i].split(":");

				if (sort[0] != null) {
					if ("asc".equals(sort[1].toLowerCase())) {
						sortingColl.add(new Sorting(sort[0], Datasource.SORTORDER_ASC));
					} else if ("desc".equals(sort[1].toLowerCase())) {
						sortingColl.add(new Sorting(sort[0], Datasource.SORTORDER_DESC));
					} else {
						sortingColl.add(new Sorting(sort[0], Datasource.SORTORDER_NONE));
					}
				}
			}
		}

		// convert hashmap to uncomfortable Array :-/ and return that
		return sortingColl.toArray(new Sorting[sortingColl.size()]);
	}

	/**
	 * Convert a HTML-conform sorting request multivalue parameter array
	 * to a Sorting[], suitable for the contentrepository queries.
	 * 
	 * @param sortingArray like ["contentid:asc","name:desc"]
	 * @return a Sorting[] Array suitable for contentrepository queries
	 */
	public final static Sorting[] convertSorting(final String[] sortingArray) {
		Sorting[] ret = new Sorting[0];
		ArrayList<Sorting> sortingColl = new ArrayList<Sorting>();
		if (sortingArray != null) {
			int arrlen = sortingArray.length;

			for (int i = 0; i < arrlen; i++) {

				// split attribute on :. First element is attribute name the
				// second is the direction
				String[] sort = sortingArray[i].split(":");

				if (sort[0] != null) {
					if ("asc".equals(sort[1].toLowerCase())) {
						sortingColl.add(new Sorting(sort[0], Datasource.SORTORDER_ASC));
					} else if ("desc".equals(sort[1].toLowerCase())) {
						sortingColl.add(new Sorting(sort[0], Datasource.SORTORDER_DESC));
					} else {
						sortingColl.add(new Sorting(sort[0], Datasource.SORTORDER_NONE));
					}
				}
			}
		}
		return sortingColl.toArray(ret);
	}

	/**
	 * Convert an array of strings to an array that can be usen in a filter rule.
	 * @param arr
	 * @return rule ready string
	 */
	public static String prepareParameterArrayForRule(final String[] arr) {
		StringBuilder ret = new StringBuilder("[");
		int arrlen = arr.length;
		if (arrlen > 0) {
			ret.append("'");
			ret.append(arr[0]);
			ret.append("'");
			if (arrlen > 1) {
				for (int i = 1; i < arrlen; i++) {
					ret.append(",'");
					ret.append(arr[i]);
					ret.append("'");
				}
			}
		}
		ret.append("]");
		return ret.toString();
	}

	/**
	 * Normalizes the portalnode confpath.(Expands "file:// " and "classpath://" urls to absolute file system paths)
	 */
	public static void normalizeConfpath() {
		String confpath = System.getProperty(PORTALNODE_CONFPATH);
		//init com.gentics.portalnode.confpath if it isn't set
		if (confpath == null || confpath.equals("")) {
			String defaultConfPath = System.getProperty("catalina.base") + File.separator + "conf" + File.separator + "gentics"
					+ File.separator;
			System.setProperty(PORTALNODE_CONFPATH, defaultConfPath);
		} else if (confpath.startsWith(FILE_PROTOCOL_IDENTIFIER)) {
			File confFile = null;
			try {
				confFile = new File(new URI(confpath));
			} catch (URISyntaxException e) {
				String message = "Could not convert PORTALNODE_CONFPATH (" + confpath + ") to an absolutePath";
				LOGGER.error(message, e);
				throw new ConfpathConfigurationException(message, e);
			}
			String confFilePath = confFile.getAbsolutePath();
			System.setProperty(PORTALNODE_CONFPATH, confFilePath);
		} else if (confpath.startsWith(CLASSPATH_PROTOCOL_IDENTIFIER)) {
			File confFile = null;
			String classpath = confpath.substring(CLASSPATH_PROTOCOL_IDENTIFIER.length());
			URL classpathURL = Thread.currentThread().getContextClassLoader().getResource(classpath);
			if(classpathURL == null) {
				throw new ConfpathConfigurationException("Could not find configured confpath '" + confpath + "' in classpath.");
			}
			try {
				confFile = new File(classpathURL.toURI());
			} catch (URISyntaxException e) {
				throw new ConfpathConfigurationException("Could not open directory in '" + classpathURL.toString() + "' for configured confpath: " + confpath, e);
			}
			System.setProperty(PORTALNODE_CONFPATH, confFile.getAbsolutePath());
		}
	}

	/**
	 * FIXME copied from com.gentics.lib.etc.StringUtils - move it to API ?
	 * Resolve system properties encoded in the string as ${property.name}
	 * @param string string holding encoded system properties
	 * @return string with the system properties resolved
	 */
	public static String resolveSystemProperties(final String string) {
		// avoid NPE here
		if (string == null) {
			return null;
		} else if (string.startsWith(DONOTRESOLVE_MARKER)) {
			return string.replaceAll("^" + DONOTRESOLVE_MARKER, "");
		}
		// ensure that the confpath system property is normalized before resolving system properties
		normalizeConfpath();
		String result = CRUtilResolver.resolveSystemProperties(string);
		result = CRUtilResolver.resolveContentConnectorProperties(result);
		return result;
	}

	/**
	 * Slurps a InputStream into a String.
	 * @param in
	 * @throws IOException
	 */
	public static String inputStreamToString(final InputStream in) throws IOException {
		StringBuffer out = new StringBuffer();
		byte[] b = new byte[4096];
		for (int n; (n = in.read(b)) != -1;) {
			out.append(new String(b, 0, n));
		}
		return out.toString();
	}

	/**
	 * Slurps a Reader into a String.
	 * @param in
	 * @throws IOException
	 */
	public static String readerToString(final Reader in) throws IOException {
		StringBuffer out = new StringBuffer();
		char[] b = new char[4096];
		for (int n; (n = in.read(b)) != -1;) {
			out.append(new String(b, 0, n));
		}
		return out.toString();
	}

	/**
	 * Slurps a Reader into a String and strips the character 0.
	 * @param in
	 * @throws IOException
	 */
	public static String readerToPrintableCharString(final Reader in) throws IOException {
		StringBuffer out = new StringBuffer();
		int n;
		while ((n = in.read()) != -1) {
			if (n != 0) {
				char c = (char) n;
				out.append(c);
			}
		}
		return out.toString();
	}

	/**
	 * Checks if a string is empty (null, length == 0 &amp;&amp; equals "").
	 * @param string variable to check
	 * @return if all conditions are true.
	 */
	public static boolean isEmpty(final String string) {
		return (string == null || string.length() == 0 || string.equals(""));
	}

	public static File createTempDir() throws IOException {
		final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
		File newTempDir;
		final int maxAttempts = 9;
		int attemptCount = 0;
		do {
			attemptCount++;
			if (attemptCount > maxAttempts) {
				throw new IOException("The highly improbable has occurred! Failed to " + "create a unique temporary directory after "
						+ maxAttempts + " attempts.");
			}
			String dirName = UUID.randomUUID().toString();
			newTempDir = new File(sysTempDir, dirName);
		} while (newTempDir.exists());

		if (newTempDir.mkdirs()) {
			return newTempDir;
		} else {
			throw new IOException("Failed to create temp dir named " + newTempDir.getAbsolutePath());
		}
	}
	
	/**
	 * Convert a properties instance to a map of strings.
	 * @param props
	 * @return Map of strings.
	 */
	public static Map<String, String> propertiesToMap(Properties props) {
		if (props != null) {
			return new HashMap<String, String>((Map)props);
		}
		return null;
	}
	
	/**
	 * Sorts a collection in memory with the given sorting object.
	 * 
	 * @param collection
	 *            the collection
	 * @param sorting
	 *            the sorting
	 */
	public static void sortCollection(List<CRResolvableBean> collection, Sorting sorting) {
		
		if(CollectionUtils.isEmpty(collection)) {
			return;
		}
		
		if (sorting != null) {
			String columnName = sorting.getColumnName();
			int order = sorting.getSortOrder();
			Collections.sort(collection, new PNSortingComparator<CRResolvableBean>(columnName, order));
		}
	}
	
	/**
	 * Sorts a collection in memory with the given sorting array
	 * 
	 * @param collection
	 *            the collection
	 * @param sorting
	 *            the sorting as sorting array
	 */
	public static void sortCollection(List<CRResolvableBean> collection, Sorting[] sorting) {
		
		if(CollectionUtils.isEmpty(collection)) {
			return;
		}
		
		if (sorting != null) {
			
			for (int i = (sorting.length - 1); i >= 0; --i) {
				sortCollection(collection, sorting[i]);
			}
		}
	}
}
