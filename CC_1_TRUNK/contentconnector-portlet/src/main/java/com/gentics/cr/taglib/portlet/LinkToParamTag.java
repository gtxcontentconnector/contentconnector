/*
 * @author norbert
 * @date 26.07.2005
 * @version $Id: ParamTag.java,v 1.1 2005/08/11 15:46:45 norbert Exp $
 */
package com.gentics.cr.taglib.portlet;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;


/**
 * 
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 545 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class LinkToParamTag extends SimpleTagSupport {
    /**
     * name of the parameter
     */
    protected String name;
    /**
     * property of the parameter
     */
    protected String property;

    /**
     * 
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     * @param property
     */
    public void setProperty(String property) {
        this.property = property;
    }

    /**
     * @see javax.servlet.jsp.tagext.SimpleTagSupport#doTag()
     */
    public void doTag() throws JspException, IOException {
//      Get the parent MessageTag
        LinkToTag parentTag = null;
        try {
            parentTag = (LinkToTag)findAncestorWithClass(this, LinkToTag.class);
        } catch (ClassCastException e) {
            ;
        }

        if ( parentTag == null ) {
            throw new JspException(
                "linkparam tag must be used inside a linkto tag");
        }

        // set the parameter to the parent tag
        parentTag.setParameter(name, property);
    }
}
