package com.gentics.cr.template;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.apache.velocity.runtime.resource.util.StringResourceRepositoryImpl;

import com.gentics.cr.exceptions.CRException;

/**
 * 
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * 
 * @version $Revision: 545 $
 * @author $Author: supnig@constantinopel.at $
 * 
 */
public class VelocityTemplateManager implements ITemplateManager {

	/**
	 * Log4j Logger.
	 */
	private static Logger log = Logger.getLogger(VelocityTemplateManager.class);
	private String encoding;
	private HashMap<String, Object> objectstoput;

	/**
	 * Templatecache.
	 */
	private HashMap<String, Template> templates;

	/**
	 * Create Instance.
	 * 
	 * @param encoding
	 */
	public VelocityTemplateManager(final String encoding) {
		this.encoding = encoding;
		this.objectstoput = new HashMap<String, Object>();
		this.templates = new HashMap<String, Template>();
	}

	/**
	 * implements
	 * {@link com.gentics.cr.template.ITemplateManager#put(String, Object)}.
	 */
	public void put(final String key, final Object value) {
		if (value != null) {
			this.objectstoput.put(key, value);
		}
	}

	/**
	 * implements
	 * {@link com.gentics.cr.template.ITemplateManager#render(String, String)}
	 */
	public String render(String templateName, String templateSource) throws CRException {
		String renderedTemplate = null;
		long s1 = System.currentTimeMillis();

		try {

			// gets the template from the VelocityTemplateManagerFactory, in
			// order to be able to cache the template
			Template template = VelocityTemplateManagerFactory.getTemplate(templateName, templateSource, this.encoding);

			// generate the velocity context
			VelocityContext context = new VelocityContext();
			Iterator<String> it = this.objectstoput.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				context.put(key, this.objectstoput.get(key));
			}

			// evaluate the template
			StringWriter ret = new StringWriter();
			template.merge(context, ret);
			renderedTemplate = ret.toString();

		} catch (ResourceNotFoundException e) {
			throw new CRException(e);
		} catch (ParseErrorException e) {
			throw new CRException(e);
		} catch (Exception e) {
			throw new CRException(e);
		} finally {
			this.objectstoput = new HashMap<String, Object>();
		}
		log.debug("Velocity has been rendered in " + (System.currentTimeMillis() - s1) + "ms");
		return renderedTemplate;
	}

}
