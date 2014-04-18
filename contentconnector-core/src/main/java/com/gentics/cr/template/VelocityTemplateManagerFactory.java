package com.gentics.cr.template;

import com.gentics.cr.exceptions.CRException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.apache.velocity.runtime.resource.util.StringResourceRepositoryImpl;

import com.gentics.cr.util.CRUtil;
import org.apache.commons.lang.CharEncoding;

import org.apache.log4j.Logger;
/**
 * 
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * 
 * @version $Revision: 545 $
 * @author $Author: supnig@constantinopel.at $
 * 
 */
public class VelocityTemplateManagerFactory {

	private static final String DEFAULT_ENCODING = CharEncoding.UTF_8;
	
	private static final String CACHE_KEY_SEPARATOR = "|";
	
	private static final String VELOCITYMACRO_FILENAME = "velocitymacros.vm";
	
	/** The message for the exception when the template name is null */
	public static final String EXCEPTION_MESSAGE_NAME_NULL = "Template name cannot be null";
	
	/** The message for the exception when the template source is null */
	public static final String EXCEPTION_MESSAGE_SOURCE_NULL = "Template source cannot be null";
	
	/** The cache zone key for the velocity template cache zone */
	public static final String VELOCITY_TEMPLATE_CACHEZONE_KEY = "gentics-cr-velocitytemplates";
    
	private static Logger log = Logger.getLogger(VelocityTemplateManagerFactory.class);

	private static boolean configured = false;

	private static JCS cache;
	
	/**
	 * Get a configured VelocityTemplateManager.
	 * 
	 * @param encoding if null defaults to utf-8
	 * @param macropath
	 * @return {@link com.gentics.cr.template.VelocityTemplateManagerFactory #getConfiguredVelocityTemplateManagerInstance(String, String, String)}
	 * @throws Exception
	 */
	public static synchronized VelocityTemplateManager getConfiguredVelocityTemplateManagerInstance(String encoding,
			String macropath) throws Exception {
		return VelocityTemplateManagerFactory.getConfiguredVelocityTemplateManagerInstance(encoding, macropath, "");

	}

	/**
	 * Get a configured VelocityTemplateManager.
	 * 
	 * @param encoding if null defaults to utf-8
	 * @param macropath
	 * @param propFile
	 * @return new instance of
	 *         {@link com.gentics.cr.template.VelocityTemplateManager} with the
	 *         specified encoding.
	 * @throws Exception
	 */
	public static synchronized VelocityTemplateManager getConfiguredVelocityTemplateManagerInstance(String encoding,
			String macropath, String propFile) throws Exception {
		if (encoding == null) {
			encoding = DEFAULT_ENCODING;
		}
		if (!configured) {
			configure(encoding, macropath, propFile);
			configured = true;
		}
		return (new VelocityTemplateManager(encoding));

	}

	/**
	 * Creates a unique cache key which is used to store templates in cache. To create the cash key the name, the 
	 * hash-code of the source and the encoding are concatenated using a separator.
	 * 
	 * @param name the name of the template
	 * @param source the velocity source-code of the template
	 * @param encoding encoding as string
	 * @return the cache key 
	 */
	public static String createCacheKey(String name, String source, String encoding) {
		StringBuilder cacheKey = new StringBuilder();
		cacheKey.append(name)
			.append(CACHE_KEY_SEPARATOR)
			.append(source.hashCode())
			.append(CACHE_KEY_SEPARATOR)
			.append(encoding);
		return cacheKey.toString();
	}
	
	/**
	 * Create a Velocity template with the given name and source and store it into JCS cache. If a template with a
	 * was found in the cache, the cached template will be returned instead of a newly created.
	 * <p>
	 * to generate the cache key the method {@link #createCacheKey(java.lang.String, java.lang.String, java.lang.String)} 
	 * is used
	 * 
	 * @param name the name of the template
	 * @param source the velocity source-code of the template
	 * @param encoding
	 *            encoding as string or null => defaults to utf-8
	 * @return template (either a cached one, found using the generated cache key,
	 *         or a newly created one).
	 * @throws com.gentics.cr.exceptions.CRException when name or source are null or when the template 
	 *		could not be created
	 */
	public static Template getTemplate(String name, String source, String encoding) throws CRException {
		// fail fast if name or source is null
		if (name == null) {
		    throw new CRException(EXCEPTION_MESSAGE_NAME_NULL);
		}
		if (source == null) {
		    throw new CRException(EXCEPTION_MESSAGE_SOURCE_NULL);
		}
		if (cache == null) {
		    try {
			    cache = JCS.getInstance(VELOCITY_TEMPLATE_CACHEZONE_KEY);
			    if (log.isDebugEnabled()) {
				log.debug("Initialized cache zone for \"" + VELOCITY_TEMPLATE_CACHEZONE_KEY + "\".");
			    }
		    } catch (CacheException e) {
			    log.warn("Could not initialize Cache for Velocity templates.", e);
		    }
		}
		if (encoding == null) {
			encoding = DEFAULT_ENCODING;
		}
		
		VelocityTemplateWrapper wrapper = null;
		String cacheKey = null;
		if (cache != null) {
			cacheKey = VelocityTemplateManagerFactory.createCacheKey(name, source, encoding);
			Object obj = cache.get(cacheKey);
			// check if obj is really a template wrapper to avoid cast exceptions when two caches accidentally 
			// use the same cache zone
			if (obj instanceof VelocityTemplateWrapper) {
			    wrapper = (VelocityTemplateWrapper) obj;
			}
		}
		// the cache key is built using String.hashCode() - there could be collisions so make sure that 
		// the source of the cached template matches the current source
		if (wrapper == null || !source.equals(wrapper.getSource())) {

			// fetching and evaluating the template may only be done once at
			// a time
			synchronized (VelocityTemplateManagerFactory.class) {

				// recheck cache after entering the synchronized area. For threads
				// that were waiting the cache may now be filled.
				if (cache != null) {
					wrapper = (VelocityTemplateWrapper) cache.get(cacheKey);
					if (wrapper != null && source.equals(wrapper.getSource())) {
						return wrapper.getTemplate();
					}
				}

				StringResourceRepository rep = StringResourceLoader.getRepository();

				if (rep == null) {
					rep = new StringResourceRepositoryImpl();
					StringResourceLoader.setRepository(StringResourceLoader.REPOSITORY_NAME_DEFAULT, rep);
				}

				rep.setEncoding(encoding);
				rep.putStringResource(name, source);

				try {

					wrapper = new VelocityTemplateWrapper(Velocity.getTemplate(name), source);

				} catch (Exception e) {
					log.error("Could not create Velocity Template.", e);
					throw new CRException(e);
				} finally {
				    rep.removeStringResource(name);
				}
				
				if (cache != null) {
					try {
						cache.put(cacheKey, wrapper);
					} catch (CacheException e) {
						log.warn("Could not put Velocity Template to cache.", e);
					}
				}
			}
		}
		return (wrapper.getTemplate());
	}

	private static void configure(String encoding, String macropath) throws Exception {
		configure(encoding, macropath, "");
	}

	private static void configure(String encoding, String macropath, String propFile) throws Exception {
		Properties props = new Properties();

		// no file with properties given, set default properties
		if (CRUtil.isEmpty(propFile)) {
			props.setProperty("string.loader.description", "String Resource Loader");
			props.setProperty("string.resource.loader.class",
					"org.apache.velocity.runtime.resource.loader.StringResourceLoader");
			props.setProperty("resource.loader", "file,string");

			if (macropath != null) {
			}

			// if a properties file is given, use this one to set the
			// vtl-properties
		} else {
			try {
				FileInputStream fis = new FileInputStream(CRUtil.resolveSystemProperties(propFile));
				props.load(fis);
				fis.close();
			} catch (FileNotFoundException e) {
				log.error("The velocity-properties file \"" + propFile + "\" does not exist!");
			}
		}

		if (macropath != null) {
			// Configure file resource loader when we have to load velocimacro
			// library
			// TODO: load file resource loader when no confpath is given to
			// allow the users to include their templates in runtime
			if (!props.containsKey("file.loader.description")) {
				props.setProperty("file.loader.description", "File Resource Loader");
			}
			if (!props.containsKey("file.resource.loader.class")) {
				props.setProperty("file.resource.loader.class",
						"org.apache.velocity.runtime.resource.loader.FileResourceLoader");
			}

			if (!props.containsKey("resource.loader")) {
				props.setProperty("resource.loader", "string,file");
			}

			if (!props.containsKey("file.resource.loader.path")) {
				props.setProperty("file.resource.loader.path", macropath);
			}
			// This property, which has possible values of true or false, 
			// determines whether Velocimacros can be defined in regular 
			// templates. The default, true, allows template designers to 
			// define Velocimacros in the templates themselves.
			if (!props.containsKey("velocimacro.permissions.allow.inline")) {
			    props.setProperty("velocimacro.permissions.allow.inline", "true");
			}
			// This property, with possible values of true or false, defaulting
			// to false, controls if Velocimacros defined inline are 'visible'
			// only to the defining template. In other words, with this property
			// set to true, a template can define inline VMs that are usable
			// only by the defining template. You can use this for fancy VM
			// tricks - if a global VM calls another global VM, with inline
			// scope, a template can define a private implementation of the
			// second VM that will be called by the first VM when invoked by
			// that template. All other templates are unaffected.
			if (!props.containsKey("velocimacro.permissions.allow.inline.local.scope")) {
			    props.setProperty("velocimacro.permissions.allow.inline.local.scope", "true");
			}

			if (!props.containsKey("velocimacro.library")) {
				try {

					File macroFile = new File(macropath + VELOCITYMACRO_FILENAME);
					log.debug("Trying to create a macrofile for velocity in " + macropath + VELOCITYMACRO_FILENAME);
					macroFile.createNewFile();

					// TODO: autodetect velocimacro library
					// using *.vm files in confpath
					props.setProperty("velocimacro.library", VELOCITYMACRO_FILENAME);
				} catch (IOException e) {
					log.error("Could not find or create macro file " + "for velocity template manager.", e);
				}
			}
		}

		// Configure Log4J logging for velocity
		props.put("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
		props.put("runtime.log.logsystem.log4j.category", "org.apache.velocity");

		props.put("input.encoding", encoding);
		props.put("output.encoding", encoding);
		Velocity.init(props);
	}
}
