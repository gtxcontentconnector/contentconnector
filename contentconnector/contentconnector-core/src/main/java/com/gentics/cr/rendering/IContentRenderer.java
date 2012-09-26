package com.gentics.cr.rendering;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashMap;

import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.api.portalnode.connector.PLinkReplacer;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.exceptions.CRException;

/**
 * Renders content.
 * Replaces PLinks and evaluates velocity.
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 545 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public interface IContentRenderer {

	/**
	 * Render Contentattribute attribute from bean into a writer.
	 * @param writer
	 * @param bean
	 * @param contentAttribute
	 * @param doReplacePLinks
	 * @param plinkReplacer
	 * @param doRenderVelocity
	 * @param resolvables
	 * @throws CRException
	 * @throws IOException
	 */
	public void renderContent(OutputStream stream, CRResolvableBean bean, String contentAttribute, boolean doReplacePLinks,
			PLinkReplacer plinkReplacer, boolean doRenderVelocity, HashMap<String, Resolvable> resolvables)
			throws CRException, IOException;
	
	/**
	 * Render Contentattribute attribute from bean into a writer.
	 * @param writer
	 * @param bean
	 * @param contentAttribute
	 * @param doReplacePLinks
	 * @param plinkReplacer
	 * @param doRenderVelocity
	 * @param resolvables
	 * @throws CRException
	 * @throws IOException
	 */
	public void renderContent(Writer writer, CRResolvableBean bean, String contentAttribute, boolean doReplacePLinks,
			PLinkReplacer plinkReplacer, boolean doRenderVelocity, HashMap<String, Resolvable> resolvables)
			throws CRException, IOException;

	/**
	 * Render contentattribute attribute from a bean into a string.
	 * @param bean
	 * @param contentAttribute
	 * @param doReplacePLinks
	 * @param plinkReplacer
	 * @param doRenderVelocity
	 * @param resolvables
	 * @throws CRException
	 * @throws IOException
	 */
	public String renderContent(CRResolvableBean bean, String contentAttribute, boolean doReplacePLinks,
			PLinkReplacer plinkReplacer, boolean doRenderVelocity, HashMap<String, Resolvable> resolvables)
			throws CRException, IOException;
}
