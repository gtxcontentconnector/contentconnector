package com.gentics.cr.template;
/**
 * 
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 545 $
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
