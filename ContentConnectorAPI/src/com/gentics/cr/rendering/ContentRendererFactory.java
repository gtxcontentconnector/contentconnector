package com.gentics.cr.rendering;

import com.gentics.cr.CRConfig;


/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
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
