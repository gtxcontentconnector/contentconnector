package com.gentics.cr.template;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import com.gentics.cr.exceptions.CRException;
import com.gentics.lib.log.NodeLogger;

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
	private static final NodeLogger log = NodeLogger.getNodeLogger(VelocityTemplateManager.class);
	private final String encoding;
	private HashMap<String, Object> objectstoput;

	/**
	 * Create Instance.
	 * 
	 * @param encoding
	 */
	public VelocityTemplateManager(final String encoding) {
		this.encoding = encoding;
		this.objectstoput = new HashMap<String, Object>();
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
	@Override
	public String render(String templateName, String templateSource) throws CRException {
		String renderedTemplate = null;
		long s1 = System.currentTimeMillis();

		try {
			Template template = VelocityTemplateManagerFactory.getTemplate(templateName, templateSource, this.encoding);

			VelocityContext context = new VelocityContext();
			Iterator<String> it = this.objectstoput.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				context.put(key, this.objectstoput.get(key));
			}

			StringWriter ret = new StringWriter();
			template.merge(context, ret);
			renderedTemplate = ret.toString();

		} catch (Exception e) {
			// convert all expections thrown during rendering of the velocity template to CRExceptions
			throw new CRException(e);
		} finally {
			this.objectstoput = new HashMap<String, Object>();
		}
		if (log.isDebugEnabled()) {
		    log.debug("Velocity has been rendered in " + (System.currentTimeMillis() - s1) + "ms");
		}
		return renderedTemplate;
	}

}
