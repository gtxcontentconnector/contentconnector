package com.gentics.cr.portlet.taglib;

import java.io.IOException;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import com.gentics.api.portalnode.connector.PLinkReplacer;
import com.gentics.cr.CRException;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.rendering.ContentRenderer;

/**
 * @author norbert
 * Implementation of a tag that renders content with plink replacing and velocity
 */
public class RenderContentTag extends SimpleTagSupport {
	/**
	 * Name of the render request attribute for the instance of {@link ContentRenderer}
	 */
	public final static String RENDERER_PARAM = "rendercontenttag.renderer";

	/**
	 * Name of the render request attribute for the instance of {@link PLinkReplacer}
	 */
	public final static String PLINK_PARAM = "rendercontenttag.plinkreplacer";
	
	/**
	 * 
	 */
	public final static String REQUEST_OBJECT_PARAM = "itemtorender";

	/**
	 * Rendered object
	 */
	protected CRResolvableBean object;

	/**
	 * name of the rendered attribute
	 */
	protected String contentAttribute = "content";

	/**
	 * Set the object to be rendered. Must be an instance of {@link CRResolvableBean}.
	 * @param object rendered object
	 */
	public void setObject(Object object) {
		System.out.println("Object set to "+object.getClass().getName()+" - "+object);
		if (object instanceof CRResolvableBean) {
			this.object = (CRResolvableBean) object;
		}
	}

	/**
	 * Set the content attribute to be rendered
	 * @param contentAttribute name of the rendered content attribute
	 */
	public void setContentAttribute(String contentAttribute) {
		this.contentAttribute = contentAttribute;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.jsp.tagext.SimpleTagSupport#doTag()
	 */
	public void doTag() throws JspException, IOException {
		// get the ContentRenderer
		RenderRequest renderRequest = getRenderRequest();

		ContentRenderer renderer = (ContentRenderer)renderRequest.getAttribute(RENDERER_PARAM);
		PLinkReplacer pLinkReplacer = (PLinkReplacer)renderRequest.getAttribute(PLINK_PARAM);
		if (object == null)
		{
			object = (CRResolvableBean)renderRequest.getAttribute(REQUEST_OBJECT_PARAM);
		}
		if (object != null) {
			try {
				renderer.renderContent(getJspContext().getOut(), object, contentAttribute, true, pLinkReplacer, false, null);
			} catch (CRException e) {
				throw new JspException("Error while rendering object "
						+ object.getContentid(), e);
			}
		} else {
			getJspContext().getOut().write(" -- no object set --");
		}
	}

	/**
	 * Get the render request
	 * 
	 * @return render request
	 * @throws JspException
	 *             when the render request could not be found
	 */
	protected RenderRequest getRenderRequest() throws JspException {
		Object renderRequestObject = getJspContext().findAttribute(
				"javax.portlet.request");

		if (renderRequestObject instanceof RenderRequest) {
			return (RenderRequest) renderRequestObject;
		} else {
			throw new JspException(
					"Error while rendering tag: could not find javax.portlet.request");
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
