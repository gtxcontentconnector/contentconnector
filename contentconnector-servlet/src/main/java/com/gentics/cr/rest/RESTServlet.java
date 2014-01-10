package com.gentics.cr.rest;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.cr.CRServletConfig;
import com.gentics.cr.monitoring.MonitorFactory;
import com.gentics.cr.monitoring.UseCase;
import com.gentics.cr.util.CRRequestBuilder;
import com.gentics.cr.util.HttpSessionWrapper;
import com.gentics.cr.util.RequestBeanWrapper;
import com.gentics.cr.util.response.ServletResponseTypeSetter;

/**
 * @author haymo
 * 
 * Used to render the Rest xml.
 * 
 */
public class RESTServlet extends HttpServlet {

	private static final long serialVersionUID = 0002L;
	/**
	 * Log4j logger for debug and error messages.
	 */
	private static Logger log = Logger.getLogger(RESTServlet.class);
	/**
	 * Configuration for the Servlet.
	 */
	private CRServletConfig crConf;
	private RESTSimpleContainer container;

	public void init(ServletConfig config) throws ServletException {

		super.init(config);
		crConf = new CRServletConfig(config);
		container = new RESTSimpleContainer(crConf);

	}

	@Override
	public void destroy() {
		if (this.container != null) {
			this.container.finalize();
		}
	}

	/**
	 * Wrapper Method for the doGet and doPost Methods. Creates a CRRequest and lets the rest container handle the request.
	 * @param request - servlet request
	 * @param response - servlet responce to write back to
	 * @throws IOException in case the container cannot process the request successfully
	 */
	public void doService(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HashMap<String, Resolvable> objects = new HashMap<String, Resolvable>();
		objects.put("request", new RequestBeanWrapper(request));
		objects.put("session", new HttpSessionWrapper(request.getSession()));
		CRRequestBuilder rB = new CRRequestBuilder(request, crConf);
		//response.setContentType(rB.getContentRepository(this.crConf.getEncoding()).getContentType()+"; charset="+this.crConf.getEncoding());
		container.processService(rB, objects, response.getOutputStream(), new ServletResponseTypeSetter(response));
	}
	
	
	/**
	 * Wrapper Method for the doService that measures the time and closes the output stream.
	 * @param request - servlet request
	 * @param response - servlet responce to write back to
	 * @throws IOException in case the container cannot process the request
	 * @see #doService(HttpServletRequest, HttpServletResponse)
	 */
	public void doServiceSafe(HttpServletRequest request, HttpServletResponse response) throws IOException {
		UseCase uc = MonitorFactory.startUseCase("RESTServlet(" + request.getServletPath() + ")");
		log.debug("Request:" + request.getQueryString());
		long starttime = new Date().getTime();
		try {
			doService(request, response);
		} finally {
			response.getOutputStream().flush();
			response.getOutputStream().close();
			
			long endtime = new Date().getTime();
			if (log.isInfoEnabled()) {
				StringBuilder requestID = new StringBuilder();
				requestID.append(request.getRequestURI());
				if (request.getQueryString() != null) {
					requestID.append('?');
					requestID.append(request.getQueryString());
				}
				log.info("Executiontime for " + requestID + ":" + (endtime - starttime));
			}
			uc.stop();
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doServiceSafe(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doServiceSafe(request, response);
	}

}
