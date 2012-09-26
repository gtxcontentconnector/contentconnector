package com.gentics.cr.rendering;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.api.portalnode.connector.PLinkReplacer;
import com.gentics.cr.CRConfig;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.plink.PLinkOutputStream;
import com.gentics.cr.template.ITemplateManager;
import com.gentics.cr.util.WriterStream;

/**
 * Renders content.
 * Replaces PLinks and evaluates velocity.
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 545 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class FastContentRenderer implements IContentRenderer {
	/**
	 * configuration.
	 */
	private CRConfig config = null;

	
	/**
	 * Create an instance of the renderer with given configuration.
	 * @param configuration Configuration.
	 */
	public FastContentRenderer(final CRConfig configuration) {
		this.config = configuration;
	}

	/**
	 * Render textual content from a bean into a writer.
	 * 
	 * @param writer writer that will get the rendered content
	 * @param bean bean holding the content
	 * @param contentAttribute name of the attribute holding the content
	 * @param doReplacePLinks true when plinks shall be replaced in the content
	 * @param plinkReplacer plink replacer to be used
	 * 	(when doReplacePLinks is true)
	 * @param doRenderVelocity true when velocity 
	 * 	shall be rendered in the content. Disabling velocity
	 *  for the content will greatly enhance the performance.
	 * @param resolvables Map of resolvables for the 
	 * 	context of the velocity renderer
	 * @throws CRException in case of CRError
	 * @throws IOException in case of IO Exception
	 */
	public final void renderContent(final Writer writer,
				final CRResolvableBean bean,
				final String contentAttribute,
				final boolean doReplacePLinks,
				final PLinkReplacer plinkReplacer,
				final boolean doRenderVelocity, 
				final HashMap<String, Resolvable> resolvables)
			throws CRException, IOException {
		
		WriterStream ws = new WriterStream(writer);
		
		this.renderContent(ws, bean, contentAttribute, doReplacePLinks,
				plinkReplacer, doRenderVelocity, resolvables);

	}
	
	/**
	 * @param stream stream that will get the rendered content
	 * @param bean bean holding the content
	 * @param contentAttribute name of the attribute holding the content
	 * @param doReplacePLinks true when plinks shall be replaced in the content
	 * @param plinkReplacer plink replacer to be used
	 * 	(when doReplacePLinks is true)
	 * @param doRenderVelocity true when velocity 
	 * 	shall be rendered in the content. Disabling velocity
	 *  for the content will greatly enhance the performance.
	 * @param resolvables Map of resolvables for the 
	 * 	context of the velocity renderer
	 * @throws CRException in case of CRError
	 * @throws IOException in case of IO Exception
	 */
	public final void renderContent(final OutputStream stream,
			final CRResolvableBean bean,
			final String contentAttribute,
			final boolean doReplacePLinks,
			final PLinkReplacer plinkReplacer,
			final boolean doRenderVelocity,
			final HashMap<String, Resolvable> resolvables) throws CRException,
			IOException {
		
			if (doRenderVelocity && !config.getPortalNodeCompMode()) {
				String rendered = this.renderContent(
					bean,
					contentAttribute,
					doReplacePLinks,
					plinkReplacer,
					doRenderVelocity,
					resolvables);
				if (rendered != null) {
					OutputStreamWriter osw = new OutputStreamWriter(stream);
					osw.write(rendered);
				}
			} else {
				Object contentValue = bean.getProperty(contentAttribute);
				if (contentValue instanceof String) {
					String content = (String) contentValue;
					PLinkOutputStream plOs = new PLinkOutputStream(stream,
							plinkReplacer);
					OutputStreamWriter w = new OutputStreamWriter(plOs);
					w.write(content);
					w.close();
				}
			}
	}

	/**
	 * Render contentattribute attribute from a bean into a string.
	 * @param bean bean holding the content
	 * @param contentAttribute name of the attribute holding the content
	 * @param doReplacePLinks true when plinks shall be replaced in the content
	 * @param plinkReplacer plink replacer to be used
	 * 	(when doReplacePLinks is true)
	 * @param doRenderVelocity true when velocity 
	 * 	shall be rendered in the content. Disabling velocity
	 *  for the content will greatly enhance the performance.
	 * @param resolvables Map of resolvables for the 
	 * 	context of the velocity renderer
	 * @return rendered string.
	 * @throws CRException in case of CRError
	 * @throws IOException in case of IO Exception
	 */
	public final String renderContent(final CRResolvableBean bean,
			final String contentAttribute,
			final boolean doReplacePLinks,
			final PLinkReplacer plinkReplacer,
			final boolean doRenderVelocity,
			final HashMap<String, Resolvable> resolvables) throws CRException,
			IOException {
		// get the content (raw)
		Object contentValue = bean.getProperty(contentAttribute);
		if (contentValue instanceof String) {
			String content = (String) contentValue;

			// replace plinks (if configured to do so)
			if (doReplacePLinks && plinkReplacer != null) {
				content = replaceStringPlinks(content, plinkReplacer);
			}

			if (doRenderVelocity && !config.getPortalNodeCompMode()) {

				// Initialize Velocity Context
				ITemplateManager myTemplateManager 
					= config.getTemplateManager();

				// enrich template context
				if (resolvables != null) {
					for (Iterator<Map.Entry<String, Resolvable>> 
						it = resolvables.entrySet().iterator(); it.hasNext();) {
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
	
	
	/**
	 * Replace String Plinks.
	 * @param content content to replace plinks in.
	 * @param replacer the plink replacer.
	 * @return content with replaced plinks
	 * @throws IOException in case of error.
	 */
	private String replaceStringPlinks(final String content,
			final PLinkReplacer replacer) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		PLinkOutputStream plOs = new PLinkOutputStream(os, replacer);

		OutputStreamWriter w = new OutputStreamWriter(plOs);
		w.write(content);
		w.close();

		plOs.close();

		return os.toString();
	}
	
}
