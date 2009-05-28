package com.gentics.cr.portlet;

import java.io.IOException;
import java.util.Date;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.ResourceServingPortlet;

import org.apache.log4j.Logger;

import com.gentics.api.lib.exception.ParserException;
import com.gentics.api.lib.expressionparser.Expression;
import com.gentics.api.lib.expressionparser.ExpressionParser;
import com.gentics.cr.CRException;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRRequestProcessor;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.portlet.taglib.RenderContentTag;
import com.gentics.cr.rendering.ContentRenderer;

public class CCJBossContentPortlet extends GenericPortlet implements
		ResourceServingPortlet {
	
	public final static String EVENT_NAME = "GENTICS_REST_CONTENT";
	public final static String CONTENTID_NAME = "RESTWindowContentID";
	
	
	/**
	 * this is the rule to find out, whether an object is a resource (image,
	 * css, js) or not
	 */
	public final static String PAGE_RULE = "object.obj_type == 10007";
	
	
	private Expression pageRule;
	private Logger log;
	private RequestProcessor rp;
	private CCPortletConfig crConf;
	private ContentRenderer renderer;
	private String startpage = "";
	private String jspTemplate;
	
	
	public void init()
	{
		PortletConfig conf = this.getPortletConfig();
		this.log = Logger.getLogger("com.gentics.cr");
		crConf = new CCPortletConfig(conf);
		this.startpage = conf.getInitParameter("startpage");
		this.jspTemplate = conf.getInitParameter("template");
		try {
			this.pageRule = ExpressionParser.getInstance().parse(PAGE_RULE);
		} catch (ParserException e1) {
			e1.printStackTrace();
		}
		
		try {
			this.rp = new CRRequestProcessor(crConf.getRequestProcessorConfig("1"));
			this.renderer = new ContentRenderer(crConf.getRequestProcessorConfig("1"));
		} catch (CRException e) {
			CRException ex = new CRException(e);
			this.log.error("FAILED TO INITIALIZE REQUEST PROCESSOR... "+ex.getStringStackTrace());
		}
	}
	
	
	public void processAction(ActionRequest request, ActionResponse response)
    throws PortletException, IOException {
		// every time, the action is processed, we trigger the event
		String contentid="";
		contentid+=request.getParameter("contentid");
		setContentidToSession(request.getPortletSession(),contentid);
	}
			
	public void processEvent(EventRequest request, EventResponse response)
    throws PortletException, IOException {
		
    	if (CCJBossContentPortlet.EVENT_NAME.equals(request.getEvent().getName())) {
		    // get the events counter from the session
		    PortletSession portletSession = request.getPortletSession();
		    //GET CONTENTID FROM EVENT
		    String newcont = request.getEvent().getValue().toString();
		    setContentidToSession(portletSession,newcont);
		}
	}
	
	public void render(RenderRequest request, RenderResponse response)
    throws PortletException, IOException{
            response.setContentType("text/html");
            	
		// starttime
		long s = new Date().getTime();
		try {
			CRRequest req = new CRRequest();
			String contentid = getContentidFromSession(request.getPortletSession());
			req.setContentid(contentid);
			req.setAttributeArray(new String[]{"content","binarycontent","mimetype"});
			CRResolvableBean crBean = rp.getContent(req);
			String contenttype = crBean.getMimetype();
			if(contenttype==null)contenttype="text/html";
			response.setContentType(contenttype);
			request.setAttribute("item", crBean);
						
		} catch (CRException e1) {
			this.log.error(e1.getMessage()+" - "+e1.getStringStackTrace());
			e1.printStackTrace();
		}
		
		request.setAttribute(RenderContentTag.RENDERER_PARAM, renderer);
		request.setAttribute(RenderContentTag.PLINK_PARAM, new GenericPLinkReplacer(request, response, rp, pageRule));
		PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher(jspTemplate);
		dispatcher.include(request, response);

		// endtime
		long e = new Date().getTime();
		this.log.debug("Executiontime for "+this.crConf.getName()+":" + (e - s));
            
	}
	
	
	protected void setContentidToSession(PortletSession portletSession, String newcontentid)
	{
		String contentid = getContentidFromSession(portletSession);
	    //GET CONTENTID FROM EVENT
	    if(!contentid.equals(newcontentid))
	    {
	    	contentid=newcontentid;
	    	// store the new value back
		    portletSession.setAttribute(CCJBossContentPortlet.CONTENTID_NAME, contentid, PortletSession.PORTLET_SCOPE);
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
        Object contentid = portletSession.getAttribute(CCJBossContentPortlet.CONTENTID_NAME,PortletSession.PORTLET_SCOPE);
        if (contentid!=null && contentid instanceof String) {
            contentid_string = (String) contentid;
        }
        else
        {
        	contentid_string = this.startpage;
        }

        return contentid_string;
    }

	public void serveResource(ResourceRequest request, ResourceResponse response) 
    throws PortletException, IOException{
    
		// starttime
		long s = new Date().getTime();
		try {
			CRRequest req = new CRRequest();
			String contentid = request.getParameter("contentid");
			req.setContentid(contentid);
			req.setAttributeArray(new String[]{"content","mimetype"});
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


}
