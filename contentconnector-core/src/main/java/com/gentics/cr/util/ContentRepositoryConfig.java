package com.gentics.cr.util;

import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.rest.ContentRepository;
import com.gentics.lib.log.NodeLogger;

/**
 * Reads contentrepository config and returns the appropriate type.
 * Great parts of this class have been refactored out of CRRequestBuilder.
 * 
 * @author Friedreich Bernhard
 */
public class ContentRepositoryConfig {

	/**
	 * Type of the repository (e.g.: XML, VELOCITY, ...).
	 */
	private String repotype;

	/**
	 * Fields to be used of the contentrepository.
	 */
	private String[] attributes;

	/**
	 * Key representing a contentrepository in configs.
	 */
	private static final String REPOSITORIES_KEY = "cr";

	/**
	 * Log4j logger.
	 */
	private static NodeLogger logger = NodeLogger.getNodeLogger(ContentRepositoryConfig.class);

	/**
	 * Configuration providing the parameters of the contentrepository.
	 */
	private GenericConfiguration config = null;

	/**
	 * Initialize a default config.
	 * @param conf read configured values.
	 */
	public ContentRepositoryConfig(final GenericConfiguration conf) {
		this.config = conf;
		getDefaultParameters();
	}

	/**
	 * Returns String Array of Attributes to request.
	 * 
	 * @return string array with the attributes
	 */
	public final String[] getAttributeArray() {
		return this.attributes;
	}

	/**
	 * @param prepareAttributesArray attributes to use for querying.
	 */
	public void setAttributeArray(final String[] prepareAttributesArray) {
		this.attributes = prepareAttributesArray;
	}

	/**
	 * Get Type of ContentRepository.
	 * Default: XML
	 * 
	 * @return type of the contentrepository
	 */
	public final String getRepositoryType() {
		if (this.repotype == null) {
			Properties props = this.getConfiguredContentRepositories();
			if (props != null) {
				String v = props.getProperty("DEFAULT");
				if (v != null) {
					this.repotype = v;
				}
			}
			if (this.repotype == null) {
				this.repotype = "XML";
			}
		}
		return this.repotype;
	}

	/**
	 * @param type Set the type of contentrepository. 
	 */
	public void setRepositoryType(final String type) {
		// Initialize RepositoryType
		this.repotype = type;
	}

	/**
	 * @return returns a map of known contentrepositories.
	 */
	private ConcurrentHashMap<String, String> getRepositoryClassMap() {

		ConcurrentHashMap<String, String> classmap = RepositoryFactory.getStringClassMap();

		// values from other projects
		// TODO this should be moved to the packages adding additional
		// ContentRepositories
		classmap.put("JSON", "com.gentics.cr.rest.json.JSONContentRepository");

		Properties confs = getConfiguredContentRepositories();
		if (confs != null) {
			for (Entry<Object, Object> e : confs.entrySet()) {
				String key = (String) e.getKey();
				if (!"default".equalsIgnoreCase(key)) {
					classmap.put(key.toUpperCase(), (String) e.getValue());
				}
			}
		}

		return classmap;
	}

	/**
	 * Create the ContentRepository for this request and give it the
	 * configuration. This is needed for the VelocityContentRepository
	 * 
	 * @param encoding Output encoding should be used
	 * @param configUtil Config to get the Velocity Engine from
	 * @return ContentRepository with the given settings.
	 */

	public ContentRepository getContentRepository(final String encoding, final CRConfigUtil configUtil) {
		ContentRepository cr = null;

		ConcurrentHashMap<String, String> classmap = getRepositoryClassMap();

		String cls = classmap.get(this.getRepositoryType().toUpperCase());
		if (cls != null) {
			try {
				cr = (ContentRepository) Class.forName(cls).getConstructor(new Class[] { String[].class, String.class })
						.newInstance(this.getAttributeArray(), encoding);
			} catch (Exception e) {
				try {
					cr = (ContentRepository) Class.forName(cls)
							.getConstructor(new Class[] { String[].class, String.class, CRConfigUtil.class })
							.newInstance(this.getAttributeArray(), encoding, configUtil);
				} catch (Exception ex) {
					try {
						cr = (ContentRepository) Class.forName(cls)
								.getConstructor(new Class[] { String[].class, String.class, String[].class, CRConfigUtil.class })
								.newInstance(this.getAttributeArray(), encoding, null, configUtil);
					} catch (Exception exc) {
						logger.error("Could not create ContentRepository instance from class: " + cls, exc);
					}
				}
			}
		} else {
			logger.error("Could not create ContentRepository instance. No Type is set to the RequestBuilder");
		}
		return cr;
	}

	/**
	 * Read out which contentrepository is configured.
	 * @return all properties set to the contentrepository.
	 */
	private final Properties getConfiguredContentRepositories() {
		if (config != null) {
			Object crs = this.config.get(REPOSITORIES_KEY);
			if (crs != null && crs instanceof GenericConfiguration) {
				GenericConfiguration crConf = (GenericConfiguration) crs;
				Properties crProperties = crConf.getProperties();
				return crProperties;
			}
		} else {
			logger.debug("Cannot find my config.");
		}
		return null;
	}

	/**
	 * done in the same way as the parameter initialisation in the constructor
	 * to avoid repeated code.
	 */
	public void getDefaultParameters() {
		GenericConfiguration defaultparameters = null;
		if (this.config != null) {
			defaultparameters = (GenericConfiguration) this.config.get(CRRequestBuilder.DEFAULPARAMETERS_KEY);
		}
		if (defaultparameters != null) {
			if (repotype == null) {
				repotype = defaultparameters.getString("type");
			}
			if (getAttributeArray() == null || getAttributeArray().length == 0) {
				String defaultAttributes = (String) defaultparameters.get("attributes");
				if (defaultAttributes != null) {
					setAttributeArray(defaultAttributes.split(",[ ]*"));
				}
			}
		}
	}

}
