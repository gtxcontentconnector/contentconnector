package com.gentics.cr.portlet.taglib;

import java.io.IOException;

import javax.portlet.PortletURL;
import javax.portlet.RenderResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import com.gentics.cr.CRResolvableBean;

/**
 * @author supnig@constantinopel.at
 * 
 */
public class GenericLink extends SimpleTagSupport {
	
	/**
	 * Rendered object
	 */
	protected CRResolvableBean object;
	protected String linkattribute;

	/**
	 * Set the object to be rendered. Must be an instance of {@link CRResolvableBean}.
	 * @param object rendered object
	 */
	public void setObject(Object object) {
		if (object instanceof CRResolvableBean) {
			this.object = (CRResolvableBean) object;
		}
	}

	/**
	 * Set the content attribute to be rendered
	 * @param contentAttribute name of the rendered content attribute
	 */
	public void setLinkAttribute(String contentAttribute) {
		this.linkattribute = contentAttribute;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.jsp.tagext.SimpleTagSupport#doTag()
	 */
	public void doTag() throws JspException, IOException {
		// get the ContentRenderer
		RenderResponse renderResponse = getRenderResponse();

		if (object != null) {
			String attr = "";
			Object o = object.get(this.linkattribute);
			if(o != null)
			{
				if (o instanceof String)
				{
					attr = (String)o;
				}
				else
				{
					attr = o.toString();
				}
				PortletURL url = renderResponse.createActionURL();
				url.setParameter("contentid", attr);
				getJspContext().getOut().write(url.toString());
			}
			else
			{
				getJspContext().getOut().write("#");
			}
			
		} else {
			getJspContext().getOut().write(" -- no object set --");
		}
	}

	

	/**
	 * Get the render response
	 * 
	 * @return render response
	 * @throws JspException when the render response could not be found
	 */
	protected RenderResponse getRenderResponse() throws JspException {
		Object renderResponseObject = getJspContext().findAttribute(
				"javax.portlet.response");

		if (renderResponseObject instanceof RenderResponse) {
			return (RenderResponse) renderResponseObject;
		} else {
			throw new JspException(
					"Error while rendering tag: could not find javax.portlet.response");
		}

	}}
