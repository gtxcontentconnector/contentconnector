package com.gentics.cr.util;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import com.gentics.cr.CRRequest;

/**
 * 
 * Last changed: $Date: 2010-04-01 15:24:41 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 543 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class CRBinaryRequestBuilder extends CRRequestBuilder {

	protected boolean doreplacePlinks = true;
	protected String url;
	protected boolean isurlrequest;

	/**
	 * Create Instance
	 * @param request
	 */
	public CRBinaryRequestBuilder(HttpServletRequest request) {
		super(request);
		doreplacePlinks = (request.getParameter("donotreplaceplinks") == null || "".equals(request
				.getParameter("donotreplaceplinks")));
		if ((this.contentid == null || contentid.equals("")) && (this.filter == null || this.filter.equals(""))) {
			this.isurlrequest = true;
			// get Servlet URI
			String uri = request.getRequestURI();
			// remove Servlet specific parts
			uri = uri.replaceAll(request.getContextPath() + request.getServletPath(), "");
			this.url = uri;
		}
	}

	/**
	 * Create Instance
	 * @param request
	 */
	public CRBinaryRequestBuilder(PortletRequest request) {
		super(request);
		doreplacePlinks = (request.getParameter("donotreplaceplinks") == null || "".equals(request
				.getParameter("donotreplaceplinks")));
		if ((this.contentid == null || this.contentid.equals("")) && (this.filter == null || this.filter.equals(""))) {
			this.isurlrequest = true;
			// get Servlet URI
			//TODO getRequestURI
			String uri = "";
			// remove Servlet specific parts
			uri = uri.replaceAll(request.getContextPath(), "");
			this.url = uri;
		}

	}

	/**
	 * Create Instance
	 * @param request
	 * @param contentid
	 */
	public CRBinaryRequestBuilder(PortletRequest request, String contentid) {
		super(request);
		doreplacePlinks = (request.getParameter("donotreplaceplinks") == null || "".equals(request
				.getParameter("donotreplaceplinks")));
		if ((contentid == null || contentid.equals(""))) {
			this.isurlrequest = true;
			// get Servlet URI
			//TODO getRequestURI
			String uri = "";
			// remove Servlet specific parts
			uri = uri.replaceAll(request.getContextPath(), "");
			this.url = uri;
		} else {
			this.contentid = contentid;
			this.filter = "object.contentid ==" + contentid;
		}
	}

	/**
	 * Create BinaryRequest
	 * @return
	 */
	public CRRequest getBinaryRequest() {
		CRRequest req = this.getCRRequest();
		req.setDoReplacePlinks(this.doreplacePlinks);
		//TODO parameterize doVelocity
		req.setDoVelocity(true);
		if (this.isurlrequest)
			req.setUrl(this.url);
		return req;
	}

}
