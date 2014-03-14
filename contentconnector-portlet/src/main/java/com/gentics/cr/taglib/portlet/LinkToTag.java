/**
 * 
 */
package com.gentics.cr.taglib.portlet;

import javax.portlet.PortletURL;
import javax.portlet.RenderResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.tagext.VariableInfo;

import com.gentics.cr.CRResolvableBean;

/**
 * 
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 545 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class LinkToTag extends TagSupport {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7815690024785086349L;

	/**
	 * 
	 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
	 * @version $Revision: 545 $
	 * @author $Author: supnig@constantinopel.at $
	 *
	 */
	public static class TEI extends TagExtraInfo {
		/**
		 * @see javax.servlet.jsp.tagext.TagExtraInfo#getVariableInfo(javax.servlet.jsp.tagext.TagData)
		 */
		public VariableInfo[] getVariableInfo(TagData tagData) {
			VariableInfo[] vi = null;
			String var = tagData.getAttributeString("var");
			if (var != null) {
				vi = new VariableInfo[1];
				vi[0] = new VariableInfo(var, "java.lang.String", true, VariableInfo.AT_BEGIN);
			}
			return vi;
		}
	}

	/**
	 * name of the variable, which will receive the rendered URL
	 */
	protected String var;

	/**
	 * Rendered object
	 */
	protected CRResolvableBean object;

	/**
	 * The portlet url
	 */
	protected PortletURL portletURL;

	/**
	 * Set the name of the variable
	 * 
	 * @param var
	 *            name of the variable
	 */
	public void setVar(String var) {
		this.var = var;
	}

	/**
	 * Set the object to be rendered. Must be an instance of
	 * {@link CRResolvableBean}.
	 * 
	 * @param object
	 *            rendered object
	 */
	public void setObject(Object object) {
		if (object instanceof CRResolvableBean) {
			this.object = (CRResolvableBean) object;
		}
	}

	/**
	 *
	 * 
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	public int doStartTag() throws JspException {
		Object renderResponseObject = pageContext.findAttribute("javax.portlet.response");
		if (renderResponseObject instanceof RenderResponse) {
			portletURL = ((RenderResponse) renderResponseObject).createActionURL();
		} else {
			throw new JspException("error while creation of actionURL: could not find renderResponse");
		}
		return Tag.EVAL_BODY_INCLUDE;
	}

	/** 
	 * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
	 */
	public int doEndTag() throws JspException {
		try {
			if (portletURL != null) {
				if (var != null) {
					pageContext.setAttribute(var, portletURL.toString(), PageContext.REQUEST_SCOPE);
				} else {
					pageContext.getOut().print(portletURL.toString());
				}
			}
		} catch (Exception ex) {
			throw new JspException(ex);
		}
		return super.doEndTag();
	}

	/**
	 * set a parameter to the portletURL
	 * 
	 * @param name
	 *            name of the parameter to set
	 * @param key
	 *            key of the parameter to set
	 */
	public void setParameter(String name, String key) {
		if (portletURL != null && object != null) {
			Object value = object.get(key);
			if (value != null) {
				portletURL.setParameter(name, value.toString());
			}
		}
	}
}
