package com.gentics.cr.template;

import java.util.HashMap;
import java.util.Iterator;

import com.gentics.api.portalnode.portlet.GenticsPortlet;
import com.gentics.api.portalnode.templateengine.PrivateKeyException;
import com.gentics.api.portalnode.templateengine.TemplateNotFoundException;
import com.gentics.api.portalnode.templateengine.TemplateProcessor;
import com.gentics.cr.CRException;

/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class PortalNodeTemplateManager implements ITemplateManager {

	private GenticsPortlet portlet;
	private HashMap<String, Object> contextObjects;
	
	public PortalNodeTemplateManager(GenticsPortlet portlet)
	{
		this.portlet = portlet;
		this.contextObjects = new HashMap<String,Object>();
	}
	
	/* (non-Javadoc)
	 * @see com.gentics.cr.template.ITemplateManager#put(java.lang.String, java.lang.Object)
	 */
	public void put(String key, Object value) {
		this.contextObjects.put(key, value);

	}

	/* (non-Javadoc)
	 * @see com.gentics.cr.template.ITemplateManager#render(java.lang.String, java.lang.String, java.lang.Object, java.lang.Object)
	 */
	@SuppressWarnings("deprecation")
	public String render(String templatename, String templatesource) throws CRException {
		String renderedTemplate=null;
		
		//TODO get undepricated method from DEV
		TemplateProcessor processor = this.portlet.getTemplateProcessor(null, null);
		
		try {
			Iterator<String> it = this.contextObjects.keySet().iterator();
			while(it.hasNext())
			{
				String key = it.next();
				processor.put(key, this.contextObjects.get(key));
			}
			renderedTemplate = processor.getOutputForSource(templatesource, this.portlet);
		} catch (TemplateNotFoundException e) {
			throw new CRException(e);
		} catch (PrivateKeyException e) {
			throw new CRException(e);
		}
		finally
		{
			this.portlet.getGenticsPortletContext().returnTemplateProcessor(processor);
		}
		
		
		
		return renderedTemplate;
	}

}
