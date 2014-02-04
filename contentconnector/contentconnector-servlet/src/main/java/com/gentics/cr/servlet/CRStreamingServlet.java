package com.gentics.cr.servlet;

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
import com.gentics.cr.rest.RESTBinaryStreamingContainer;
import com.gentics.cr.util.BeanWrapper;
import com.gentics.cr.util.CRBinaryRequestBuilder;
import com.gentics.cr.util.HttpSessionWrapper;
import com.gentics.cr.util.response.ServletResponseTypeSetter;

/**
 * @author haymo
 * 
 *         Use to render content from a contentrepository.
 * 
 */
public class CRStreamingServlet extends HttpServlet {

	/**
	 * Regex to validate the input of the content disposition header.
	 */
	private static final String CONTENT_DISPOSITION_HEADER_VALIDATION_REGEX = "[a-zA-Z0-9_\\.-]+";
	/**
	 * 
	 */
	private static final long serialVersionUID = -6943138512221124880L;
	private Logger log;
	private RESTBinaryStreamingContainer container;

	public void init(ServletConfig config) throws ServletException {

		super.init(config);
		this.log = Logger.getLogger("com.gentics.cr");
		CRServletConfig crConf = new CRServletConfig(config);
		container = new RESTBinaryStreamingContainer(crConf);

	}

	@Override
	public void destroy() {
		if (this.container != null)
			this.container.finalize();
	}

	/**
	 * Wrapper Method for the doGet and doPost Methods.
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doService(HttpServletRequest request, final HttpServletResponse response) throws ServletException,
			IOException {

		// URI and Query String
		String requestID = request.getRequestURI();
		if (request.getQueryString() != null) {
			requestID += "?" + request.getQueryString();
		}

		this.log.debug("Starting request: " + requestID);

		// starttime
		long s = new Date().getTime();

		HashMap<String, Resolvable> objects = new HashMap<String, Resolvable>();
		objects.put("request", new BeanWrapper(request));
		objects.put("session", new HttpSessionWrapper(request.getSession()));
		container.processService(
			new CRBinaryRequestBuilder(request),
			objects,
			response.getOutputStream(),
			new ServletResponseTypeSetter(response));

		handleContentDispositionParameter(request, response);

		response.getOutputStream().flush();
		response.getOutputStream().close();

		// endtime
		long e = new Date().getTime();
		this.log.debug("Executiontime for " + requestID + ":" + (e - s));

	}

	private void handleContentDispositionParameter(HttpServletRequest request,
			final HttpServletResponse response) {
		String contentDisposition = request.getParameter("contentdisposition");
		if (contentDisposition != null && contentDisposition != "" && contentDisposition.matches(CONTENT_DISPOSITION_HEADER_VALIDATION_REGEX)) {
			response.addHeader("Content-Disposition", "attachment; filename=\"" + contentDisposition + "\"");
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doService(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doService(request, response);
	}

}
