package com.gentics.cr.util;

import javax.servlet.http.HttpServletRequest;

import com.gentics.cr.CRRequest;
import com.gentics.cr.configuration.GenericConfiguration;

/**
 * {@link CRRequestBuilder} to generate CrRequests needed for building a
 * navigation.
 * Last changed: $Date: 2010-04-01 15:24:41 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 543 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class CRNavigationRequestBuilder extends CRRequestBuilder {

	/**
	 * String containing the filter for the children.
	 */
	private String childfilter;

	/**
	 * Create new Instance of the CRRequestBuilder.
	 * @param request TODO javadoc.
	 */
	public CRNavigationRequestBuilder(final HttpServletRequest request) {
		this(request, null);
	}

	/**
	 * Create a new instance of the CRRequestBuilder.
	 * @param request {@link HttpServletRequest} to create the CRRequestBuilder
	 * for
	 * @param crConf configuration containing the default parameters.
	 */
	public CRNavigationRequestBuilder(final HttpServletRequest request, final GenericConfiguration crConf) {
		super(request, crConf);
		childfilter = this.createPermissionsRule((String) request.getParameter("childfilter"), this.permissions);
		String rootfilter = (String) request.getParameter("rootfilter");
		if (rootfilter != null && !rootfilter.equals("")) {
			this.filter = this.createPermissionsRule(rootfilter, this.permissions);
		}
	}

	/**
	 * Creates a CRRequest from the configuration.
	 * @return CRReauest to build a navigation
	 */
	public final CRRequest getNavigationRequest() {
		CRRequest req = this.getCRRequest();
		req.setChildFilter(childfilter);
		return req;
	}

}
