package com.gentics.cr.portlet;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
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
import com.gentics.cr.nav.NavObject;
import com.gentics.cr.template.FileTemplate;
import com.gentics.cr.template.ITemplate;

public class CCJBossNavigationPortlet extends GenericPortlet implements
		ResourceServingPortlet {
	
	/**
	 * this is the rule to find out, whether an object is a resource (image,
	 * css, js) or not
	 */
	public final static String PAGE_RULE = "object.obj_type == 10007";
	
	private Logger log;
	private RequestProcessor rp;
	
	private CCPortletConfig crConf;
	private ITemplate template;
	private String tmplPath;
	private String startfolder;
	private Expression pageRule;
	
	public void init()
	{
		PortletConfig config = this.getPortletConfig();
		this.log = Logger.getLogger("com.gentics.cr");
		crConf = new CCPortletConfig(config);
		this.tmplPath = config.getInitParameter("template");
		this.startfolder = config.getInitParameter("startfolder");
		
		try {
		
			this.pageRule = ExpressionParser.getInstance().parse(PAGE_RULE);
		} catch (ParserException e1) {
			e1.printStackTrace();
		}
		
		try {
			this.template = new FileTemplate(this.getPortletContext().getResourceAsStream(this.tmplPath));
			this.rp = new CRRequestProcessor(crConf.getRequestProcessorConfig("1"));
			
		} catch (CRException e) {
			CRException ex = new CRException(e);
			this.log.error("FAILED TO INITIALIZE REQUEST PROCESSOR... "+ex.getStringStackTrace());
		}
	}
	
	public void render(RenderRequest request, RenderResponse response)
    throws PortletException, IOException{
        response.setContentType("text/html");
                	
		
		
		// starttime
		long s = new Date().getTime();
		try {
			CRRequest req = new CRRequest();
			req.setContentid(this.startfolder);
			req.setAttributeArray(new String[]{"name","mimetype"});
			req.setChildFilter("object.obj_type==10002");
			Collection<CRResolvableBean> crBean = rp.getNavigation(req);
			
			CRResolvableBean[] bean = crBean.toArray(new CRResolvableBean[0]);
			long s1 = new Date().getTime();
			HashMap<String,Object> objects = new HashMap<String,Object>();
			objects.put("response", response);
			
			NavObject n = new NavObject(this.crConf,bean[0], 0, null, template, objects);
			response.setContentType("text/html");
			response.getWriter().write(n.render());
			long e1 = new Date().getTime();
			this.log.debug("Rendertime for "+this.crConf.getName()+":" + (e1 - s1));
			
			
			
		} catch (CRException e1) {
			this.log.error(e1.getMessage()+" - "+e1.getStringStackTrace());
			e1.printStackTrace();
		}
		
		/* Display the content through a JSP-File (= defined above "jspTemplate")
		 * The above code handles the attributes and which jsp-template should be used
		 */
		
		// endtime
		long e = new Date().getTime();
		this.log.debug("Executiontime for "+this.crConf.getName()+":" + (e - s));
            
}
	
	public void processAction(ActionRequest request, ActionResponse response)
    throws PortletException, IOException {
		// every time, the action is processed, we trigger the event
		String contentid="";
		contentid+=request.getParameter("contentid");
		response.setEvent(CCJBossContentPortlet.EVENT_NAME, contentid);
	}

	public void serveResource(ResourceRequest request, ResourceResponse response) 
    throws PortletException, IOException{
    
    response.setContentType("text/html");
    response.getWriter().write("Output From Serve Resource");
    
    
}


}
