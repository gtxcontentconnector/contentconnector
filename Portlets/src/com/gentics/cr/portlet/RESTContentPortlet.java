package com.gentics.cr.portlet;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.apache.log4j.Logger;

import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.cr.rest.RESTBinaryContainer;
import com.gentics.cr.util.BeanWrapper;
import com.gentics.cr.util.CRBinaryRequestBuilder;
/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class RESTContentPortlet extends GenericPortlet {
	
	private Logger log;
	private RESTBinaryContainer container;
	public final static String EVENT_NAME = "GENTICS_REST_CONTENT";
	public final static String CONTENTID_NAME = "RESTWindowContentID";
	
	/**
	 * 
	 */
	public void init()
	{
		this.log = Logger.getLogger("com.gentics.cr");
		CRPortletConfig crConf = new CRPortletConfig(this.getPortletConfig());
		container = new RESTBinaryContainer(crConf);
	}
	
	/**
     * doView will be called in the modules render phase prints "Hello World"
     * text to the renderResponse PrintWriter
     */
    protected void doView(RenderRequest renderRequest, RenderResponse renderResponse)
            throws PortletException, IOException {

    	// starttime
		long s = new Date().getTime();
		
		HashMap<String,Resolvable> objects = new HashMap<String,Resolvable>();
		objects.put("request", new BeanWrapper(renderRequest));
		objects.put("portlet", new BeanWrapper(this));
		
		//objects.put("session", new HttpSessionWrapper(request.getSession()));
		String contentid = getContentidFromSession(renderRequest.getPortletSession());
		
		container.processService(new CRBinaryRequestBuilder(renderRequest,contentid), objects, renderResponse.getPortletOutputStream());
		renderResponse.setContentType(container.getContentType());
		renderResponse.getPortletOutputStream().flush();
		renderResponse.getPortletOutputStream().close();
		// endtime
		long e = new Date().getTime();
		this.log.debug("Executiontime for RESTContentPortlet:" + (e - s));
    }
    
    public void serveResource(ResourceRequest request, ResourceResponse response)
    throws PortletException, IOException {
    	
    	// starttime
		long s = new Date().getTime();
		
		HashMap<String,Resolvable> objects = new HashMap<String,Resolvable>();
		objects.put("request", new BeanWrapper(request));
		//objects.put("session", new HttpSessionWrapper(request.getSession()));
		 
		container.processService(new CRBinaryRequestBuilder(request), objects, response.getPortletOutputStream());
		response.setContentType(container.getContentType());
		String contentDisposition=request.getParameter("contentdisposition");
		
		if(contentDisposition!=null && contentDisposition!="")
		{
			//response.addHeader("Content-Disposition","attachment; filename=\""+contentDisposition+"\"");
			//TODO DO CONTENT DISPOSITION
		}
		
		// endtime
		long e = new Date().getTime();
		this.log.debug("Executiontime for RESTContentPortlet:" + (e - s));
		
	}
    
    public void processEvent(EventRequest request, EventResponse response)
    throws PortletException, IOException {
		
    	if (RESTContentPortlet.EVENT_NAME.equals(request.getEvent().getName())) {
		    // get the events counter from the session
		    PortletSession portletSession = request.getPortletSession();
		    String contentid = getContentidFromSession(portletSession);
		    //GET CONTENTID FROM EVENT
		    String newcont = request.getEvent().getValue().toString();
		    if(!contentid.equals(newcont))contentid=newcont;
		    // store the new value back
		    portletSession.setAttribute(RESTContentPortlet.CONTENTID_NAME, contentid, PortletSession.PORTLET_SCOPE);
		}
	}
    
    /**
     * Get the events count from the session
     * @param portletSession portlet session
     * @return events count
     */
    protected String getContentidFromSession(PortletSession portletSession) {
        String contentid_string = "";

        // get the event counter from the session
        Object contentid = portletSession.getAttribute(RESTContentPortlet.CONTENTID_NAME,PortletSession.PORTLET_SCOPE);
        if (contentid instanceof String) {
            contentid_string = (String) contentid;
        }

        return contentid_string;
    }
    
    
}
