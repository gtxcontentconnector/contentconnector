package com.gentics.cr.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

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
	private ITemplateManager vtl;
	/**
	 * velocity Template to render.
	 */
	private ITemplate tpl;

	/**
	 * Configuration key to specify a template in the configuration.
	 */
	private static final String VELOCITY_TEMPLATE_KEY = "velocitytemplate";

	/**
	 * Mark if we should render the velocity template.
	 */
	private boolean renderVelocity = true;

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
					log.error("Could not load template from " + templatepath, e);
				} catch (CRException e) {
					log.error("Could not load template from " + templatepath, e);
				}
			}
		}
		if (this.tpl == null) {
			String templateName = this.getClass().getSimpleName() + ".vm";
			try {
				InputStream stream = this.getClass().getResourceAsStream(templateName);
				this.tpl = new FileTemplate(stream);
			} catch (Exception e) {
				log.error("failed to load velocity template from " + templateName, e);
			}
		}
	}

	/**
	 * Get the content connetcor configuration of the servlet.
	 */
	protected final CRConfigUtil getCRConfig() {
		return crConf;
	}

	/**
	 * Wrapper Method for the doGet and doPost Methods. Prepares the data for
	 * the render method. Don't forget to put your variables into the velocity
	 * template before rendering.
	 * @param request {@link HttpServletRequest} to process the servlet for.
	 * @param response {@link HttpServletResponse} to write the output into.
	 * @throws IOException in case something went wrong processing the service.
	 */
	public abstract void doService(final HttpServletRequest request, final HttpServletResponse response)
			throws IOException;

	/**
	 * Renders the velocity template.
	 * Don't forget to put your variables into the velocity template before
	 * rendering.
	 * @param response {@link HttpServletResponse} to render the template into
	 * @throws IOException if we cannot write the output to the output stream.
	 */
	public final void render(final HttpServletResponse response) throws IOException {
		try {
			String timestamp = new Long(System.currentTimeMillis()).toString();
			vtl.put("timestamp", timestamp);
			String output = vtl.render(tpl.getKey(), tpl.getSource());
			response.getWriter().write(output);
		} catch (Exception ex) {
			log.error("Error rendering template for " + this.getClass().getSimpleName() + ".", ex);
		}
		response.getWriter().flush();
		response.getWriter().close();
	}

	/**
	 * Set a variable in the velocity context.
	 * @param name - name of the variable to set
	 * @param value - value to set the variable to
	 */
	protected final void setTemplateVariable(final String name, final Object value) {
		if (vtl != null) {
			vtl.put(name, value);
		}
	}

	/**
	 * skip velocity rendering for this request. this can be used if you want to
	 * put binaries direct to the render response.
	 */
	protected final void skipRenderingVelocity() {
		renderVelocity = false;
	}

	@Override
	protected final void doGet(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		renderVelocity = true;
		doService(request, response);
		if (renderVelocity) {
			render(response);
		}
	}

	@Override
	protected final void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
