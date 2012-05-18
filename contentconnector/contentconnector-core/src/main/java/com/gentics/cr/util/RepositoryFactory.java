package com.gentics.cr.util;

import java.util.concurrent.ConcurrentHashMap;

import com.gentics.cr.rest.ContentRepository;

/**
 * {@link RepositoryFactory} provides an index over all available
 * ContentRepositories and their shortname.
 * @author bigbear3001
 *
 */
public class RepositoryFactory {

	/**
	 * additional repositories that are registered by external classes.
	 */
	private static ConcurrentHashMap<String, String> registeredClasses = new ConcurrentHashMap<String, String>();

	/**
	 * Enumeration containing the values of the repository types.
	 * @author bigbear3001
	 */
	public enum RepositoryType {

		/**
		 * RepositoryType for
		 * {@link com.gentics.cr.rest.xml.XmlContentRepository}.
		 */
		XML,
		/**
		 * RepositoryType for
		 * {@link com.gentics.cr.rest.json.JSONContentRepository}.
		 */
		JSON,
		/**
		 * RepositoryType for
		 * {@link com.gentics.cr.rest.php.PHPContentRepository}.
		 */
		PHP,
		/**
		 * RepositoryType for
		 * {@link com.gentics.cr.rest.javaxml.JavaXmlContentRepository}.
		 */
		JAVAXML,
		/**
		 * TODO javadoc. Binding is missing at the moment.
		 */
		RSS,
		/**
		 * RepositoryType for
		 * {@link com.gentics.cr.rest.xml.MnogosearchXmlContentRepository}.
		 */
		MNOGOSEARCHXML,
		/**
		 * RepositoryType for
		 * {@link com.gentics.cr.rest.javabin.JavaBinContentRepository}.
		 */
		JAVABIN,
		/**
		 * RepositoryType for
		 * {@link com.gentics.cr.rest.velocity.VelocityContentRepository}.
		 */
		VELOCITY,
		/**
		 * RepositoryType for
		 * {@link com.gentics.cr.rest.xml.CSSitemapContentRepository}.
		 */
		CSSITEMAP
	}

	/**
	 * private constructor to prevent instantiation.
	 */
	private RepositoryFactory() {
	}

	/**
	 * {@link ConcurrentHashMap} containing the assignment of the
	 * {@link RepositoryType}s to their classes.
	 * @see #init()
	 */
	private static ConcurrentHashMap<RepositoryType, Class<? extends ContentRepository>> classmap = null;

	/**
	 * get a {@link ConcurrentHashMap} containing the assignment of
	 * {@link RepositoryType}s as strings to their implementation classe names.
	 * @return {@link ConcurrentHashMap} with the implementation classe names as strings
	 */
	public static ConcurrentHashMap<String, String> getStringClassMap() {
		init();
		ConcurrentHashMap<String, String> result = new ConcurrentHashMap<String, String>(classmap.size());
		for (RepositoryType type : classmap.keySet()) {
			result.put(type.name(), classmap.get(type).getName());
		}
		for (String type : registeredClasses.keySet()) {
			result.put(type, registeredClasses.get(type));
		}
		return result;
	}

	/**
	 * get a {@link ConcurrentHashMap} containing the assignment of
	 * {@link RepositoryType}s to their implementation classes.
	 * @return {@link ConcurrentHashMap} with the implementation classes.
	 */
	public static ConcurrentHashMap<RepositoryType, Class<? extends ContentRepository>> getContentRepositoryMap() {
		init();
		return classmap;
	}

	/**
	 * register the external repository class in the repository factory.
	 * @param key - type of the repository to be registered
	 * @param clazz - class of the repositor to be registered
	 */
	public static void registerAdditionalRepository(final String key, final String clazz) {
		registeredClasses.put(key, clazz);
	}

	/**
	 * initialize the HashMap containing the assignment of
	 * {@link RepositoryType}s to the specific Repository classes.
	 */
	private static synchronized void init() {
		if (classmap == null) {
			int size = RepositoryType.values().length;
			classmap = new ConcurrentHashMap<RepositoryType, Class<? extends ContentRepository>>(size);
			//ADD DEFAULT ENTRIES
			//CLASSMAP.put(RepositoryType.JSON, com.gentics.cr.rest.json.JSONContentRepository.class);
			classmap.put(RepositoryType.PHP, com.gentics.cr.rest.php.PHPContentRepository.class);
			classmap.put(RepositoryType.JAVAXML, com.gentics.cr.rest.javaxml.JavaXmlContentRepository.class);
			classmap.put(RepositoryType.JAVABIN, com.gentics.cr.rest.javabin.JavaBinContentRepository.class);
			classmap.put(RepositoryType.VELOCITY, com.gentics.cr.rest.velocity.VelocityContentRepository.class);
			classmap.put(RepositoryType.XML, com.gentics.cr.rest.xml.XmlContentRepository.class);
			classmap.put(RepositoryType.CSSITEMAP, com.gentics.cr.rest.xml.CSSitemapContentRepository.class);
			classmap.put(RepositoryType.MNOGOSEARCHXML, com.gentics.cr.rest.xml.MnogosearchXmlContentRepository.class);

		}
	}
}
