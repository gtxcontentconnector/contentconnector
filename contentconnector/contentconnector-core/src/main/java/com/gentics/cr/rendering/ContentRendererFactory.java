package com.gentics.cr.rendering;

import com.gentics.cr.CRConfig;

/**
 * 
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 545 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class ContentRendererFactory {

	/**
	 * Get a new instance of an IContentRenderer implementation configured in the contentrendererclass attribute in the given config
	 * @param config
	 * @return instance of IContentRenderer
	 */
	public final static IContentRenderer getRendererInstance(CRConfig config) {

		//FIXME implement to get the renderer class from the config

		return new ContentRenderer(config);
	}
}
