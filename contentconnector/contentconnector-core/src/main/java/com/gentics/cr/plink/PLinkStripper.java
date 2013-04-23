package com.gentics.cr.plink;

import com.gentics.api.portalnode.connector.PLinkInformation;
import com.gentics.api.portalnode.connector.PLinkReplacer;

/**
 *  Helper Class to be passed to PortalConnectorHelper.replacePLinks.
 *  Replaces PLinks with "" => useful for indexing
 *
 * 
 * Last changed: $Date: 2009-06-22 17:49:58 +0200 (Mo, 22 Jun 2009) $
 * @version $Revision: 95 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class PLinkStripper implements PLinkReplacer {

	/**
	 * Returns an empty String to remove PLinks in the Content.
	 * @param arg0 
	 */
	public String replacePLink(PLinkInformation arg0) {
		return "";
	}

}
