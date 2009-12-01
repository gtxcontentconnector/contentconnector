package com.gentics.cr.rendering;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.api.portalnode.connector.PLinkReplacer;
import com.gentics.api.portalnode.connector.PortalConnectorHelper;
import com.gentics.cr.CRConfig;
import com.gentics.cr.CRException;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.template.ITemplateManager;

/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class ContentRenderer implements IContentRenderer{
	/**
	 * configuration
	 */
	protected CRConfig config = null;
	
	//private static Logger log = Logger.getLogger(ContentRenderer.class);

	/**
	 * Create an instance of the renderer with given configuration
	 * 
	 * @param config
	 *            configuration
	 */
	public ContentRenderer(CRConfig config) {
		this.config = config;
	}

	/**
	 * Render textual content from a bean into a writer
	 * 
	 * @param writer
	 *            writer that will get the rendered content
	 * @param bean
	 *            bean holding the content
	 * @param contentAttribute
	 *            name of the attribute holding the content
	 * @param doReplacePLinks
	 *            true when plinks shall be replaced in the content
	 * @param plinkReplacer
	 *            plink replacer to be used (when doReplacePLinks is true)
	 * @param doRenderVelocity
	 *            true when velocity shall be rendered in the content
	 * @param resolvables
	 *            Map of resolvables for the context of the velocity renderer
	 * @throws CRException
	 * @throws IOException 
	 */
	public void renderContent(Writer writer, CRResolvableBean bean,
			String contentAttribute, boolean doReplacePLinks,
			PLinkReplacer plinkReplacer, boolean doRenderVelocity,
			HashMap<String, Resolvable> resolvables) throws CRException, IOException {
		// get the content (raw)
		
		String rendered = this.renderContent(bean, contentAttribute, doReplacePLinks, plinkReplacer, doRenderVelocity, resolvables);
		if(rendered!=null)
		{
			writer.write(rendered);
		}
		
	}

	/**
	 * Render contentattribute attribute from a bean into a string
	 * @param bean
	 * @param contentAttribute
	 * @param doReplacePLinks
	 * @param plinkReplacer
	 * @param doRenderVelocity
	 * @param resolvables
	 * @return
	 * @throws CRException
	 * @throws IOException
	 */
	public String renderContent(CRResolvableBean bean, String contentAttribute,
			boolean doReplacePLinks, PLinkReplacer plinkReplacer,
			boolean doRenderVelocity, HashMap<String, Resolvable> resolvables)
			throws CRException, IOException {
		// get the content (raw)
		Object contentValue = bean.getProperty(contentAttribute);
		if (contentValue instanceof String) {
			String content = (String) contentValue;

			// replace plinks (if configured to do so)
			if (doReplacePLinks && plinkReplacer != null) {
				content = PortalConnectorHelper.replacePLinks(content,
						plinkReplacer);
			}

			if (doRenderVelocity && !config.getPortalNodeCompMode()) {
				
				// Initialize Velocity Context
				ITemplateManager myTemplateManager = config
						.getTemplateManager();

				// enrich template context
				if (resolvables != null) {
					for (Iterator<Map.Entry<String, Resolvable>> it = resolvables
							.entrySet().iterator(); it.hasNext();) {
						Map.Entry<String, Resolvable> entry = it.next();
						myTemplateManager.put(entry.getKey(), entry.getValue());
					}
				}
				content = myTemplateManager.render("attribute", content);
			}

			return content;
		}
		return null;
	}
}
