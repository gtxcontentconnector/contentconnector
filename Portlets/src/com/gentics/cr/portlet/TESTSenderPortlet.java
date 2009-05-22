/*
 * @author norbert
 * @date 28.08.2008
 * @version $Id: EventSenderPortlet.java,v 1.1 2008/08/28 14:43:36 norbert Exp $
 */
package com.gentics.cr.portlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class TESTSenderPortlet extends GenericPortlet {
    /**
     * name of the event
     */
    
    /* (non-Javadoc)
     * @see javax.portlet.GenericPortlet#doView(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
     */
    protected void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        PrintWriter writer = response.getWriter();
        PortletURL actionURL = response.createActionURL();
        writer.println("<form name=\"ContentPortletForm"+request.getWindowID()+"\" action=\""+actionURL.toString()+"\" method=\"post\">");
        writer.println("<input type=\"text\" name=\"contentid\"/><br/><input type=\"submit\" value=\"Send\" name=\"submit\"/></form>");
    }

    /* (non-Javadoc)
     * @see javax.portlet.GenericPortlet#processAction(javax.portlet.ActionRequest, javax.portlet.ActionResponse)
     */
    public void processAction(ActionRequest request, ActionResponse response)
            throws PortletException, IOException {
        // every time, the action is processed, we trigger the event
    	String contentid="";
    	contentid+=request.getParameter("contentid");
        response.setEvent(RESTContentPortlet.EVENT_NAME, contentid);
    }
}
