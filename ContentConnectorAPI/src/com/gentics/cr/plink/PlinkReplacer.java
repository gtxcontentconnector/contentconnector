package com.gentics.cr.plink;

import com.gentics.api.portalnode.connector.PLinkInformation;
import com.gentics.api.portalnode.connector.PLinkReplacer;
import com.gentics.cr.CRRequest;

/**
 *  Helper Class to be passed to PortalConnectorHelper.replacePLinks.
 *  PlinkProcessor passed to constructor does the real work.
 *
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class PlinkReplacer implements PLinkReplacer {

	protected PlinkProcessor plinkProc;
	protected CRRequest request;
	public PlinkReplacer(PlinkProcessor proc, CRRequest request) {
		this.plinkProc = proc;
		this.plinkProc.deployObjects(request.getObjectsToDeploy());
		this.request = request;
	}

	public String replacePLink(PLinkInformation plink) {
		return this.plinkProc.getLink(plink,request);
	}
}
