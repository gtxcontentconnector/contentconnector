package com.gentics.cr.template;

import com.gentics.cr.CRException;

public interface ITemplateManager {

	public void put(String key,Object value);
	
	public String render(String templatename, String templatesource) throws CRException;
	
}
