package com.gentics.cr.util.response;

/**
 * 
 * Last changed: $Date: 2010-04-01 15:25:30 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 544 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public interface IResponseTypeSetter {

	/**
	 * Sets the contenttype to a given response. This may be a HttpServletResponse, a PortletResponse,...
	 * @param type
	 */
	public void setContentType(String type);
}
