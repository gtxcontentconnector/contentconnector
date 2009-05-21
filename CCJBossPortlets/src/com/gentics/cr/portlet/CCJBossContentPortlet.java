package com.gentics.cr.portlet;

import java.io.IOException;
import java.util.Date;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.ResourceServingPortlet;

import org.apache.log4j.Logger;

import com.gentics.cr.CRException;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRRequestProcessor;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;

public class CCJBossContentPortlet extends GenericPortlet implements
		ResourceServingPortlet {
	
	private Logger log;
	private RequestProcessor rp;
	private CCPortletConfig crConf;
	public void init()
	{
		this.log = Logger.getLogger("com.gentics.cr");
		crConf = new CCPortletConfig(this.getPortletConfig());
		try {
			this.rp = new CRRequestProcessor(crConf.getRequestProcessorConfig("1"));
		} catch (CRException e) {
			CRException ex = new CRException(e);
			this.log.error("FAILED TO INITIALIZE REQUEST PROCESSOR... "+ex.getStringStackTrace());
		}
	}
	
	public void render(RenderRequest request, RenderResponse response)
    throws PortletException, IOException{
            response.setContentType("text/html");
            response.getWriter().write("<p>Hello Portlet 2.0 World</p><br/>");
            response.getWriter().write("<a onClick=\"window.open('"+response.createResourceURL()+"','mywindow','width=400,height=200');return  false;\">Click Me</a>");
      
      	
		
		
		// starttime
		long s = new Date().getTime();
		try {
			CRRequest req = new CRRequest();
			req.setContentid("10007.938");
			CRResolvableBean crBean = rp.getContent(req);
			String contenttype = crBean.getMimetype();
			if(contenttype==null)contenttype="text/html";
			response.setContentType(contenttype);
			
			response.getWriter().write(crBean.getContent());
			
		} catch (CRException e1) {
			this.log.error(e1.getMessage()+" - "+e1.getStringStackTrace());
			e1.printStackTrace();
		}

		// endtime
		long e = new Date().getTime();
		this.log.debug("Executiontime for "+this.crConf.getName()+":" + (e - s));
            
}

	public void serveResource(ResourceRequest request, ResourceResponse response) 
    throws PortletException, IOException{
    
    response.setContentType("text/html");
    response.getWriter().write("Output From Serve Resource");
    
    
}


}
