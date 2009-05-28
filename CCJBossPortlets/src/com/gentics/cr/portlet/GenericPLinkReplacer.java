package com.gentics.cr.portlet;

import javax.portlet.MimeResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;
import javax.portlet.ResourceURL;

import org.apache.log4j.Logger;

import com.gentics.api.lib.expressionparser.Expression;
import com.gentics.api.lib.expressionparser.ExpressionEvaluator;
import com.gentics.api.portalnode.connector.PLinkInformation;
import com.gentics.api.portalnode.connector.PLinkReplacer;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;

/**
 * Generic implementation of a PLinkReplacer
 */
public class GenericPLinkReplacer implements PLinkReplacer {

	/**
	 * current render request
	 */
	protected PortletRequest request;

	/**
	 * current render response
	 */
	protected MimeResponse response;

	/**
	 * instance of the expression evaluator (for finding out, whether the
	 * linked object is a resource or a page)
	 */
	protected ExpressionEvaluator evaluator;

	/**
	 * Request processor
	 */
	protected RequestProcessor rp;

	/**
	 * expression to determine, whether an object is a resource or a normal page
	 */
	protected Expression resourceRule;
	
	
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	/**
	 * Create an instance of the PLinkReplacer
	 * 
	 * @param request
	 *            render request
	 * @param response
	 *            render response
	 */
	public GenericPLinkReplacer(PortletRequest request, MimeResponse response, RequestProcessor rp, Expression resourceRule) {
		this.request = request;
		this.response = response;
		evaluator = new ExpressionEvaluator();
		this.rp = rp;
		this.resourceRule = resourceRule;
	}
	
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gentics.api.portalnode.connector.PLinkReplacer#replacePLink(com.gentics.api.portalnode.connector.PLinkInformation)
	 */
	public String replacePLink(PLinkInformation plink) {
		try {
			
			// if the plink has no mentionable attributes
			// get the linked object and display a link to
			// it
			CRRequest crReq = new CRRequest();
			crReq.setContentid(plink.getContentId());
			
			CRResolvableBean object = rp.getFirstMatchingResolvable(crReq);

			// no object -> no link
			if (object == null) {
				return "#";
			}
			
			
			// check whether to create a resource url, or an action url
			if (evaluator.match(resourceRule, object)){
				PortletURL actionURL = response.createActionURL();
				actionURL.setParameter("contentid", object.getContentid());
				
				return actionURL.toString();
			}
			else
			{
				ResourceURL resourceURL = response.createResourceURL();
				resourceURL.setParameter("contentid", object.getContentid());
				
				//resourceURL.setCacheability(ResourceURL.FULL);
				return resourceURL.toString();
			}
			
			
			
		} catch (Exception e) {
			// TODO log the error here
			System.out.println("error while replaceing");
			e.printStackTrace();
			return "#";
		}
	}
	
}
