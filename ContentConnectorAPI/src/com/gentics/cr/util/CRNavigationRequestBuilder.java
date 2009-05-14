package com.gentics.cr.util;

import javax.servlet.http.HttpServletRequest;

import com.gentics.cr.CRRequest;

public class CRNavigationRequestBuilder extends CRRequestBuilder {

	protected String childfilter;
	
	public CRNavigationRequestBuilder(HttpServletRequest request) {
		super(request);
		this.childfilter = this.createPermissionsRule((String) request.getParameter("childfilter"),this.permissions);
		String rootfilter = (String) request.getParameter("rootfilter");
		if(rootfilter!=null && !rootfilter.equals(""))
		{
			this.filter = this.createPermissionsRule(rootfilter, this.permissions);
		}
	}
	
		
	public CRRequest getNavigationRequest()
	{
		CRRequest req = this.getCRRequest();
		req.setChildFilter(childfilter);
		return req;
	}
	
	

}
