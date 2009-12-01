package com.gentics.cr;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.gentics.cr.plink.PlinkProcessor;
import com.gentics.cr.plink.PlinkReplacer;
import com.gentics.cr.rendering.ContentRenderer;
import com.gentics.cr.util.CRBinaryRequestBuilder;

/**
 * 
 * Last changed: $Date: 2009-06-23 17:40:36 +0200 (Di, 23 Jun 2009) $
 * @version $Revision: 96 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class JSPServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8607594484139295241L;
	private static Logger log = Logger.getLogger(JSPServlet.class);
	private CRServletConfig crConf;
	private ContentRenderer renderer;
	private PlinkProcessor pproc;
	private RequestProcessor rp;
	
	private static final String JSP_FILE_KEY = "jspfile";
	/**
	 * @param config 
	 * @throws ServletException 
	 * 
	 */
	public void init(ServletConfig config) throws ServletException {

		super.init(config);
		this.crConf = new CRServletConfig(config);
		this.renderer = new ContentRenderer(crConf.getRequestProcessorConfig("1").getRequestProcessorConfig(2));
		this.pproc = new PlinkProcessor(crConf.getRequestProcessorConfig("1").getRequestProcessorConfig(2));
		try {
			this.rp = crConf.getNewRequestProcessorInstance(1);
		} catch (CRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Wrapper Method for the doGet and doPost Methods
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doService(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		log.debug("Request:" + request.getQueryString());
		// starttime
		long s = new Date().getTime();
		// get the objects
		CRBinaryRequestBuilder rb = new CRBinaryRequestBuilder(request);
		CRRequest req = rb.getBinaryRequest();
		
		try {
			Collection<CRResolvableBean> coll = this.rp.getObjects(req);
			if(coll!=null)
			{
				request.setAttribute("objects",coll);
			}
		} catch (CRException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		request.setAttribute(com.gentics.cr.taglib.servlet.RenderContentTag.RENDERER_PARAM, renderer);
		request.setAttribute(com.gentics.cr.taglib.servlet.RenderContentTag.PLINK_PARAM, new PlinkReplacer(pproc, req));
		
		String jspFile = (String)this.crConf.get(JSP_FILE_KEY);
		getServletContext().getRequestDispatcher(jspFile).forward(request, response); 
		// endtime
		long e = new Date().getTime();
		log.info("Executiontime for " + request.getQueryString() + ":" + (e - s));

	}

	
	/**
	 * @param request 
	 * @param response 
	 * @throws ServletException 
	 * @throws IOException 
	 * 
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doService(request, response);
	}

	/**
	 * @param request 
	 * @param response 
	 * @throws ServletException 
	 * @throws IOException 
	 * 
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doService(request, response);
	}

}