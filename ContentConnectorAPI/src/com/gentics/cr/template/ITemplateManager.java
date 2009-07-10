package com.gentics.cr.template;

import com.gentics.cr.CRException;
/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public interface ITemplateManager {

	/**
	 * Deploy and Object into the Render Context
	 * @param key
	 * @param value
	 */
	public void put(String key,Object value);
	
	/**
	 * Render the given template into a String
	 * @param templatename
	 * @param templatesource
	 * @return
	 * @throws CRException
	 */
	public String render(String templatename, String templatesource) throws CRException;
	
}
