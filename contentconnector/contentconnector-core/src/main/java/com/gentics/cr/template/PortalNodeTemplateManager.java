package com.gentics.cr.template;

import java.util.HashMap;
import java.util.Iterator;

import com.gentics.api.portalnode.portlet.GenticsPortlet;
import com.gentics.api.portalnode.templateengine.PrivateKeyException;
import com.gentics.api.portalnode.templateengine.TemplateNotFoundException;
import com.gentics.api.portalnode.templateengine.TemplateProcessor;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.portalnode.PortalNodeInteractor;

/**
 * 
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 545 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class PortalNodeTemplateManager implements ITemplateManager {

	private GenticsPortlet portlet;
	private HashMap<String, Object> contextObjects;

	/**
	 * Create Instance.
	 * @param portlet
	 */
	public PortalNodeTemplateManager(GenticsPortlet portlet) {
		this.portlet = portlet;
		this.contextObjects = new HashMap<String, Object>();
	}

	/**
	 * @see com.gentics.cr.template.ITemplateManager#put(java.lang.String, java.lang.Object)
	 */
	public void put(final String key, final Object value) {
		this.contextObjects.put(key, value);

	}

	/**
	 * implements {@link com.gentics.cr.template.ITemplateManager#render(String, String)}
	 */
	public String render(final String templatename, final String templatesource) throws CRException {
		String renderedTemplate = null;

		TemplateProcessor processor = PortalNodeInteractor.getPortletTemplateProcessor(this.portlet);

		try {
			Iterator<String> it = this.contextObjects.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				processor.put(key, this.contextObjects.get(key));
			}
			renderedTemplate = processor.getOutputForSource(templatesource, this.portlet);
		} catch (TemplateNotFoundException e) {
			throw new CRException(e);
		} catch (PrivateKeyException e) {
			throw new CRException(e);
		} finally {
			this.portlet.getGenticsPortletContext().returnTemplateProcessor(processor);
		}

		return renderedTemplate;
	}

}
