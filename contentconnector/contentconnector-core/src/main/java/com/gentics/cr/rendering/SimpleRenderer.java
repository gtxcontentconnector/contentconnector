package com.gentics.cr.rendering;

import com.gentics.api.portalnode.connector.PLinkReplacer;
import com.gentics.api.portalnode.connector.PortalConnectorHelper;
import com.gentics.cr.CRConfig;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.template.ITemplateManager;

/**
 * This simple renderer is intended to be put into a velocity context to render special attributes of a bean
 * 
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 545 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class SimpleRenderer {

	private boolean doPlinks = true;
	private boolean doVelocity = true;

	private CRConfig conf;
	private PLinkReplacer pr;

	/**
	 * Set the SimpleRenderer to enable or disable Velocity.
	 * Default: Velocity enabled
	 * @param dovtl
	 */
	public void setDoVelocity(final boolean dovtl) {
		this.doVelocity = dovtl;
	}

	/**
	 * Set the SimpleRenderer to enable or disable Plinks.
	 * @param doplinks
	 */
	public void setDoPlinks(final boolean doplinks) {
		this.doPlinks = doplinks;
	}

	/**
	 * Create a new instance of SimpleRenderer.
	 * @param conf
	 * @param pr
	 */
	public SimpleRenderer(final CRConfig conf, final PLinkReplacer pr) {
		this.conf = conf;
		this.pr = pr;
	}

	/**
	 * Performs velocity and plink rendering on a given string.
	 * @param source
	 */
	public String render(String source) throws CRException {
		// replace plinks (if configured to do so)
		if (this.doPlinks && this.pr != null) {
			source = PortalConnectorHelper.replacePLinks(source, this.pr);
		}

		if (this.doVelocity) {

			// Initialize Velocity Context
			ITemplateManager myTemplateManager = conf.getTemplateManager();

			source = myTemplateManager.render("attribute", source);
		}
		return source;
	}
}
