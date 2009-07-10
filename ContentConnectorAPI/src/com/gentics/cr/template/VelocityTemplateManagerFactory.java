package com.gentics.cr.template;

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

/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class VelocityTemplateManagerFactory {
	
	private static Logger log = Logger.getLogger(VelocityTemplateManagerFactory.class);
	
	private static boolean configured = false;
	
	private static JCS cache;

	/**
	 * Get a configured VelocityTemplateManager
	 * @param encoding if null defaults to utf-8
	 * @param macropath
	 * @return
	 * @throws Exception
	 */
	public static synchronized VelocityTemplateManager getConfiguredVelocityTemplateManagerInstance(String encoding, String macropath) throws Exception
	{
		if(encoding==null)encoding="utf-8";
		if(!configured)
		{
			configure(encoding, macropath);
			configured=true;
		}
		return( new VelocityTemplateManager(encoding));
		
	}
	
	/**
	 * Create a Velocity template with the given name and source
	 * @param name
	 * @param source 
	 * @param encoding encoding as string or null => defaults to utf-8
	 * @return
	 */
	public static Template getTemplate(String name, String source, String encoding)
	{
		if(encoding==null)encoding="utf-8";
		try {
					
			cache = JCS.getInstance("gentics-cr-velocitytemplates");
			log.debug("Initialized cache zone for \"gentics-cr-velocitytemplates\".");
			

		} catch (CacheException e) {

			log.warn("Could not initialize Cache for Velocity templates.");
			e.printStackTrace();
		}
		
		
		Template template=null;
		
		if(cache!=null)
		{
			template = (Template)cache.get(name+source);
		}
		
		if(template==null)
		{
			StringResourceRepository rep = StringResourceLoader.getRepository();
			rep.setEncoding(encoding);
			rep.putStringResource(name, source);
			try {
				template = Velocity.getTemplate(name);
			} catch (ResourceNotFoundException e1) {
				log.warn("Could not create Velocity Template.");
				e1.printStackTrace();
			} catch (ParseErrorException e1) {
				log.warn("Could not create Velocity Template.");
				e1.printStackTrace();
			} catch (Exception e1) {
				log.warn("Could not create Velocity Template.");
				e1.printStackTrace();
			}
			rep.removeStringResource(name);
			if(cache!=null)
			{
				try {
					cache.put(name+source, template);
				} catch (CacheException e) {
					log.warn("Could not put Velocity Template to cache.");
					e.printStackTrace();
				}
			}
		}	
		return(template);
	}
	
	private static void configure(String encoding, String macropath) throws Exception
	{
		Properties props = new Properties();
		props.setProperty("string.loader.description","String Resource Loader");
		props.setProperty("string.resource.loader.class","org.apache.velocity.runtime.resource.loader.StringResourceLoader");
		props.setProperty("resource.loader","string");
		
		if(macropath!=null){
			//Configure file resource loader when we have to load velocimacro library
			//TODO: load file resource loader when no confpath is given to allow the users to include their templates in runtime
			props.setProperty("file.loader.description", "File Resource Loader");
			props.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
			props.setProperty("file.resource.loader.path",macropath);
			props.setProperty("resource.loader","string,file");
			//TODO: autodetect velocimacro library using *.vm files in confpath
			props.setProperty("velocimacro.library", "velocitymacros.vm");
		}
		//Configure Log4J logging for velocity
		props.put("runtime.log.logsystem.class","org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
		props.put("runtime.log.logsystem.log4j.category","org.apache.velocity");
		
		props.put("input.encoding", encoding);
		props.put("output.encoding", encoding);
		Velocity.init(props);
	}
}
