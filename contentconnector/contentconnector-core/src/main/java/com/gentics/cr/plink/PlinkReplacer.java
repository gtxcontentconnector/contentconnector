package com.gentics.cr.plink;

import com.gentics.api.portalnode.connector.PLinkInformation;
import com.gentics.api.portalnode.connector.PLinkReplacer;
import com.gentics.cr.CRRequest;

/**
 *  Helper Class to be passed to PortalConnectorHelper.replacePLinks.
 *  PlinkProcessor passed to constructor does the real work.
 *
 * 
 * Last changed: $Date: 2010-01-18 14:08:30 +0100 (Mo, 18 Jän 2010) $
 * @version $Revision: 398 $
 * @author $Author: bigbear.ap $
 *
 */
public class PlinkReplacer implements PLinkReplacer {

	protected PlinkProcessor plinkProc;
	protected CRRequest request;

	/**
	 * Create instance.
	 * @param proc {@link PlinkProcessor} to handle the PLinks
	 * @param request {@link CRRequest} from the servlet to build an url back to the servlet
	 */
	public PlinkReplacer(final PlinkProcessor proc, final CRRequest request) {
		this.plinkProc = proc;
		this.plinkProc.deployObjects(request.getObjectsToDeploy());
		this.request = request;
	}

	/**
	 * Create an instance without a request.
	 * @param proc {@link PlinkProcessor} to handle the PLinks
	 */
	public PlinkReplacer(final PlinkProcessor proc) {
		this.plinkProc = proc;
	}

	/**
	 * Replace plinks.
	 * @param plink 
	 */
	public String replacePLink(final PLinkInformation plink) {
		return this.plinkProc.getLink(plink, request);
	}
}
