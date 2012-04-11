/**
 * 
 */
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
import com.gentics.cr.util.BeanWrapper;
import com.gentics.cr.util.CRNavigationRequestBuilder;
import com.gentics.cr.util.HttpSessionWrapper;
import com.gentics.cr.util.response.ServletResponseTypeSetter;

/**
 * @author Christopher
 *
 */
public class RESTNavigation extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 557189789791823626L;

	/**
	 * Log4j logger for debug and error messages;
	 */
	private Logger log = Logger.getLogger(RESTNavigation.class);

	/**
	 * Configuration for the Servlet.
	 */
	private CRServletConfig crConf;

	private RESTNavigationContainer container;

	public void init(ServletConfig config) throws ServletException {

		super.init(config);
		crConf = new CRServletConfig(config);
		container = new RESTNavigationContainer(crConf);
	}

	@Override
	public void destroy() {
		if (this.container != null)
			this.container.finalize();
	}

	public void doService(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		UseCase uc = MonitorFactory.startUseCase("RESTServlet(" + request.getServletPath() + ")");
		this.log.debug("Request:" + request.getQueryString());

		// starttime
		long s = new Date().getTime();
		HashMap<String, Resolvable> objects = new HashMap<String, Resolvable>();
		objects.put("request", new BeanWrapper(request));
		objects.put("session", new HttpSessionWrapper(request.getSession()));
		CRNavigationRequestBuilder requestBuilder = new CRNavigationRequestBuilder(request, crConf);
		container.processService(requestBuilder, objects, response.getOutputStream(), new ServletResponseTypeSetter(
				response));
		response.getOutputStream().flush();
		response.getOutputStream().close();
		// endtime
		long e = new Date().getTime();
		this.log.info("Executiontime for " + request.getQueryString() + ":" + (e - s));
		uc.stop();
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doService(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doService(request, response);
	}

}
