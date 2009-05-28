package com.gentics.cr.template;
/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public interface ITemplate {
	/**
	 * gets the key of the template. usually a md5 hash
	 * @return key
	 */
	public String getKey();
	
	/**
	 * gets the source of the template
	 * @return source
	 */
	public String getSource();
}
