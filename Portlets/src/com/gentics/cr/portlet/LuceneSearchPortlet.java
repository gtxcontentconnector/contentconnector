package com.gentics.cr.portlet;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.apache.log4j.Logger;

import com.gentics.cr.CRException;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRRequestProcessor;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessorMerger;
import com.gentics.cr.lucene.search.CRSearcher;
import com.gentics.cr.lucene.search.LuceneRequestProcessor;
/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class LuceneSearchPortlet extends GenericPortlet {
	
	private Logger log;
	private CRRequestProcessor crRP;
	private LuceneRequestProcessor luceneRP;
	public final static String EVENT_NAME = "GENTICS_LUCENE_SEARCH";
	public final static String RESULT_IN_SESSION_NAME = "LUCENE_SEARCH_RESULT";
	
	private CRSearcher searcher;
	
	public void init()
	{
		this.log = Logger.getLogger("com.gentics.cr");
		CRPortletConfig crConf = new CRPortletConfig(this.getPortletConfig());
		try {	
			crRP = new CRRequestProcessor(crConf.getRequestProcessorConfig(1));
			luceneRP = new LuceneRequestProcessor(crConf.getRequestProcessorConfig(1));
		} catch (CRException e) {
			e.printStackTrace();
		}
		//this.searcher = new CRSearcher(this.getPortletName());
	}
	
	/**
     * doView will be called in the modules render phase prints "Hello World"
     * text to the renderResponse PrintWriter
     */
    protected void doView(RenderRequest renderRequest, RenderResponse renderResponse)
            throws PortletException, IOException {

    	// starttime
		long s = new Date().getTime();
		
		
		PortletURL actionURL = renderResponse.createActionURL();
			
		renderRequest.setAttribute("aURL", actionURL.toString());
	   	PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/searchPortlet.jsp");
	   	dispatcher.include(renderRequest, renderResponse);
		
		// endtime
		long e = new Date().getTime();
		this.log.debug("Executiontime for RESTContentPortlet:" + (e - s));
    }
    
    /* (non-Javadoc)
     * @see javax.portlet.GenericPortlet#processAction(javax.portlet.ActionRequest, javax.portlet.ActionResponse)
     */
    public void processAction(ActionRequest request, ActionResponse response)
            throws PortletException, IOException {
        // every time, the action is processed, we trigger the event
    	String query=request.getParameter("q");
    	Collection<CRResolvableBean> coll;
    	
    	if(query!=null)
    	{
    		CRRequest crreq = new CRRequest();
    		crreq.setRequestFilter(query);
    		crreq.setAttributeArray(new String[]{"contentid","content"});
    		try {
				coll = RequestProcessorMerger.merge("contentid", this.luceneRP, this.crRP, crreq);
				saveInSession(coll);
			} catch (CRException e) {
				e.printStackTrace();
			}
    	}
    	System.out.println("ACTION WITH "+query);
        response.setEvent(LuceneSearchPortlet.EVENT_NAME, query);
    }
    
    private void saveInSession(Collection<CRResolvableBean> results)
    {
    	
    }
    
    public void serveResource(ResourceRequest request, ResourceResponse response)
    throws PortletException, IOException {
    	
    	// starttime
		long s = new Date().getTime();
				
		// endtime
		long e = new Date().getTime();
		this.log.debug("Executiontime for RESTContentPortlet:" + (e - s));
		
	}
    
}
