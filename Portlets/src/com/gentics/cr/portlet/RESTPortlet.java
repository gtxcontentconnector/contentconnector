package com.gentics.cr.portlet;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.ResourceURL;

import org.apache.log4j.Logger;

import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.cr.rest.RESTSimpleContainer;
import com.gentics.cr.util.BeanWrapper;
import com.gentics.cr.util.CRRequestBuilder;
/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class RESTPortlet extends GenericPortlet {
	
	private Logger log;
	private RESTSimpleContainer container;
	
	
	public void init()
	{
		this.log = Logger.getLogger("com.gentics.cr");
		CRPortletConfig crConf = new CRPortletConfig(this.getPortletConfig());
		container = new RESTSimpleContainer(crConf);
	}
	
	/**
     * doView will be called in the modules render phase prints "Hello World"
     * text to the renderResponse PrintWriter
     */
    protected void doView(RenderRequest renderRequest, RenderResponse renderResponse)
            throws PortletException, IOException {

        //print "hello world" into the render response's printwriter
        renderResponse.getWriter().println("This is the REST Portlet");
        ResourceURL resourceUrl = renderResponse.createResourceURL();
        resourceUrl.setResourceID("rest");
        resourceUrl.setParameter("filter", "object.obj_type==10002");
        
        renderResponse.getWriter().println("<a href=\""+resourceUrl.toString()+"\">Klick here for REST</a>");

    }
    
    public void serveResource(ResourceRequest request, ResourceResponse response)
    throws PortletException, IOException {
    	
		// starttime
		long s = new Date().getTime();
		// get the objects
		
		HashMap<String,Resolvable> objects = new HashMap<String,Resolvable>();
		objects.put("request", new BeanWrapper(request));
		//objects.put("session", new HttpSessionWrapper(request.getSession()));
		container.processService(new CRRequestBuilder(request), objects, response.getPortletOutputStream());
		response.setContentType(container.getContentType());
		// endtime
		long e = new Date().getTime();
		this.log.info("Executiontime for RESTPortlet:" + (e - s));
		
	}
    
    
}
