package com.gentics.cr.template;
/**
 * 
 * Last changed: $Date: 2009-05-18 17:31:58 +0200 (Mo, 18 Mai 2009) $
 * @version $Revision: 27 $
 * @author $Author: supnig@constantinopel.at $
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
