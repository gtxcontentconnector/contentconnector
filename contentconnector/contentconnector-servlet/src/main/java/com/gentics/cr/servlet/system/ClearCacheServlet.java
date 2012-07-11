package com.gentics.cr.servlet.system;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.gentics.api.lib.datasource.Datasource;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRDatabaseFactory;
import com.gentics.cr.CRServletConfig;

/**
 * This servlet can be used to clear the cache for one, several, or all items of
 * the configured datasource.
 * 
 * @author Christopher
 * 
 */
public class ClearCacheServlet extends HttpServlet {

	/**
	 * ID parameter.
	 */
	private static final String ID_PARAMETER = "contentid";

	/**
	 * All parameter.
	 */
	private static final String ALL_PARAMETER = "all";

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger
			.getLogger(ClearCacheServlet.class);

	/**
	 * Config.
	 */
	private CRConfigUtil conf;
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 7945993479664628287L;

	/**
	 * init.
	 * 
	 * @param config
	 *            configuration.
	 * @throws ServletException
	 *             in case of error.
	 */
	public final void init(final ServletConfig config) throws ServletException {
		super.init(config);
		conf = new CRServletConfig(config);
	}

	/**
	 * doService.
	 * 
	 * @param request
	 *            request.
	 * @param response
	 *            response.
	 * @throws ServletException
	 *             in case of error
	 * @throws IOException
	 *             in case of error.
	 */
	public final void doService(final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {

		String allString = request.getParameter(ALL_PARAMETER);
		if (allString != null && Boolean.parseBoolean(allString)) {
			Datasource ds = conf.getDatasource();
			CRDatabaseFactory.clearCache(ds);
			LOGGER.debug("Cleared the cache of the configured datasource for "
					+ this.getServletName());
		} else {
			String[] contentids = request.getParameterValues(ID_PARAMETER);
			Datasource ds = conf.getDatasource();
			if (contentids != null) {
				for (String id : contentids) {
					CRDatabaseFactory.clearCache(ds, id);
					LOGGER.debug("Cleared " + id + " from the cache for "
							+ this.getServletName());
				}
			}
		}
	}

	/**
	 * doGet.
	 * 
	 * @param request
	 *            request.
	 * @param response
	 *            response.
	 * @throws ServletException
	 *             in case of error
	 * @throws IOException
	 *             in case of error.
	 */
	public final void doGet(final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {
		doService(request, response);
	}

	/**
	 * doPost.
	 * 
	 * @param request
	 *            request.
	 * @param response
	 *            response.
	 * @throws ServletException
	 *             in case of error
	 * @throws IOException
	 *             in case of error.
	 */
	public final void doPost(final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {
		doService(request, response);
	}
}
