package com.gentics.cr.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.gentics.cr.CRConfigFileLoader;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.template.FileTemplate;
import com.gentics.cr.template.ITemplate;
import com.gentics.cr.template.ITemplateManager;

/**
 * Generic Servlet to render the output with velocity.
 * @author bigbear3001
 */
public abstract class VelocityServlet extends HttpServlet {

	/**
	 * Log4j logger for error and debug messages.
	 */
	private static Logger log = Logger.getLogger(VelocityServlet.class);

	/**
	 * CRConfiguration of the servlet to detect the velocity template if
	 * configured.
	 */
	private CRConfigUtil crConf;
	/**
	 * Template Manager to get store the environments for the velocity template.
	 */
	protected ITemplateManager vtl;
	/**
	 * velocity Template to render.
	 */
	private ITemplate tpl;
	
	/**
	 * Configuration key to specify a template in the configuration.
	 */
	private static final String VELOCITY_TEMPLATE_KEY = "velocitytemplate";

	@Override
	public void init(final ServletConfig config) throws ServletException {

		super.init(config);
		String servletName = config.getServletName();
		crConf = new CRConfigFileLoader(servletName, null);
		vtl = crConf.getTemplateManager();
		
		String templatepath = crConf.getString(VELOCITY_TEMPLATE_KEY);
		if (templatepath != null) {
			File f = new File(templatepath);
			if (f.exists()) {
				try {
					this.tpl = new FileTemplate(new FileInputStream(f));
				} catch (FileNotFoundException e) {
					log.error("Could not load template from " + templatepath,
							e);
				} catch (CRException e) {
					log.error("Could not load template from " + templatepath,
							e);
				}
			}
		}
		if (this.tpl == null) {
			String templateName = this.getClass().getSimpleName() + ".vm";
			try {
				
				this.tpl = new FileTemplate(VelocityServlet.class
						.getResourceAsStream(templateName));
			} catch (Exception ex) {
				log.error("failed to load velocity template from "
						+ templateName);
			}
		}
	}
	
	/**
	 * Wrapper Method for the doGet and doPost Methods.
	 * call render() at the end of your code to render the velocitytemplate
	 * @param request {@link HttpServletRequest} to process the servlet for.
	 * @param response {@link HttpServletResponse} to write the output into.
	 * @throws IOException in case something went wrong processing the service.
	 */
	public abstract void doService(final HttpServletRequest request,
			final HttpServletResponse response) throws IOException;
	
	/**
	 * Renders the velocity template.
	 * Don't forget to put your variables into the velocity template before
	 * rendering.
	 * @param response {@link HttpServletResponse} to render the template into
	 * @throws IOException if we cannot write the output to the output stream.
	 */
	public final void render(final HttpServletResponse response)
			throws IOException {
		try {
			String output = vtl.render(tpl.getKey(), tpl.getSource());
			response.getWriter().write(output);
		} catch (Exception ex) {
			log.error("Error rendering template for "
					+ this.getClass().getSimpleName() + ".", ex);
		}
		response.getWriter().flush();
		response.getWriter().close();
	}

	@Override
	protected void doGet(final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {
		doService(request, response);
	}

	@Override
	protected void doPost(final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {
		doService(request, response);
	}

}
