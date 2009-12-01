package com.gentics.cr.lucene.indexer.transformer;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;


/**
 * Regex Replacing within the content can be quite costful in terms of performance.
 * 
 * Last changed: $Date: 2009-06-24 17:10:19 +0200 (Mi, 24 Jun 2009) $
 * @version $Revision: 99 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class DebugTransformer extends ContentTransformer{
	private static final String TRANSFORMER_ATTRIBUTE_KEY="attribute";
	private String attribute="";
	/**
	 * Create Instance of CommentSectionStripper
	 * @param config
	 */
	public DebugTransformer(GenericConfiguration config) {
		super(config);
		attribute = (String)config.get(TRANSFORMER_ATTRIBUTE_KEY);
	}

	@Override
	public void processBean(CRResolvableBean bean) {
		if(this.attribute!=null)
		{
			Object obj = bean.get(this.attribute);
			log.error("Attribute "+this.attribute+": "+obj);
		}
		else
		{
			log.error("Configured attribute is null. Bean will not be processed");
		}
	
	}

}
