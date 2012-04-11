package com.gentics.cr.template;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;

import com.gentics.cr.util.CRUtil;

/**
 * 
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 545 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class VelocityTemplateManagerFactory {

	private static Logger log = Logger.getLogger(VelocityTemplateManagerFactory.class);

	private static final String VELOCITYMACRO_FILENAME = "velocitymacros.vm";

	private static boolean configured = false;

	private static JCS cache;

	/**
	 * Get a configured VelocityTemplateManager
	 * @param encoding if null defaults to utf-8
	 * @param macropath
	 * @return
	 * @throws Exception
	 */
	public static synchronized VelocityTemplateManager getConfiguredVelocityTemplateManagerInstance(String encoding,
			String macropath) throws Exception {
		return VelocityTemplateManagerFactory.getConfiguredVelocityTemplateManagerInstance(encoding, macropath, "");

	}

	/**
	 * Get a configured VelocityTemplateManager and 
	 * @param encoding if null defaults to utf-8
	 * @param macropath
	 * @param propFile
	 * @return
	 * @throws Exception
	 */
	public static synchronized VelocityTemplateManager getConfiguredVelocityTemplateManagerInstance(String encoding,
			String macropath, String propFile) throws Exception {
		if (encoding == null)
			encoding = "utf-8";
		if (!configured) {
			configure(encoding, macropath, propFile);
			configured = true;
		}
		return (new VelocityTemplateManager(encoding));

	}

	/**
	 * Create a Velocity template with the given name and source
	 * @param name
	 * @param source 
	 * @param encoding encoding as string or null => defaults to utf-8
	 * @return
	 */
	public static Template getTemplate(String name, String source, String encoding) {
		if (encoding == null)
			encoding = "utf-8";
		try {

			cache = JCS.getInstance("gentics-cr-velocitytemplates");
			log.debug("Initialized cache zone for \"gentics-cr-velocitytemplates\".");

		} catch (CacheException e) {

			log.warn("Could not initialize Cache for Velocity templates.", e);
		}

		Template template = null;

		if (cache != null) {
			template = (Template) cache.get(name + source);
		}

		if (template == null) {
			StringResourceRepository rep = StringResourceLoader.getRepository();
			rep.setEncoding(encoding);
			rep.putStringResource(name, source);
			try {
				template = Velocity.getTemplate(name);
			} catch (ResourceNotFoundException e1) {
				log.warn("Could not create Velocity Template.", e1);
			} catch (ParseErrorException e1) {
				log.warn("Could not create Velocity Template.", e1);
			} catch (Exception e1) {
				log.warn("Could not create Velocity Template.", e1);
			}
			rep.removeStringResource(name);
			if (cache != null) {
				try {
					cache.put(name + source, template);
				} catch (CacheException e) {
					log.warn("Could not put Velocity Template to cache.");
					e.printStackTrace();
				}
			}
		}
		return (template);
	}

	private static void configure(String encoding, String macropath) throws Exception {
		configure(encoding, macropath, "");
	}

	private static void configure(String encoding, String macropath, String propFile) throws Exception {
		Properties props = new Properties();

		// no file with properties given, set default properties
		if (CRUtil.isEmpty(propFile)) {
			props.setProperty("string.loader.description", "String Resource Loader");
			props.setProperty(
				"string.resource.loader.class",
				"org.apache.velocity.runtime.resource.loader.StringResourceLoader");
			props.setProperty("resource.loader", "file,string");

			if (macropath != null) {
			}

			// if a properties file is given, use this one to set the vtl-properties
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
			//Configure file resource loader when we have to load velocimacro library
			//TODO: load file resource loader when no confpath is given to allow the users to include their templates in runtime
			if (!props.containsKey("file.loader.description")) {
				props.setProperty("file.loader.description", "File Resource Loader");
			}
			if (!props.containsKey("file.resource.loader.class")) {
				props.setProperty(
					"file.resource.loader.class",
					"org.apache.velocity.runtime.resource.loader.FileResourceLoader");
			}
			if (!props.containsKey("file.resource.loader.path")) {
				props.setProperty("file.resource.loader.path", macropath);
			}
			if (!props.containsKey("resource.loader")) {
				props.setProperty("resource.loader", "string,file");
			}

			if (!props.containsKey("velocimacro.library")) {
				//CHECK IF VELICITYMACROS EXISTS AND CREATE EMPTY FILE IF IT DOES NOT
				File macro_file = new File(macropath + VELOCITYMACRO_FILENAME);
				macro_file.createNewFile();

				//TODO: autodetect velocimacro library using *.vm files in confpath
				props.setProperty("velocimacro.library", VELOCITYMACRO_FILENAME);
			}
		}

		//Configure Log4J logging for velocity
		props.put("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
		props.put("runtime.log.logsystem.log4j.category", "org.apache.velocity");

		props.put("input.encoding", encoding);
		props.put("output.encoding", encoding);

		Velocity.init(props);
	}
}
