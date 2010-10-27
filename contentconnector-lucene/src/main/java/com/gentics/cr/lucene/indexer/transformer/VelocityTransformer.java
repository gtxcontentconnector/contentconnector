package com.gentics.cr.lucene.indexer.transformer;


import org.apache.log4j.Logger;

import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.template.ITemplate;
import com.gentics.cr.template.ITemplateManager;
import com.gentics.cr.template.StringTemplate;
/**
 * Render the velocity template into the target attribute.
 * @author bigbear3001
 *
 */
public class VelocityTransformer extends ContentTransformer {

	/**
	 * Configuration key for source attribute.
	 */
	private static final String TRANSFORMER_TEMPLATE_KEY =
		"template";
	/**
	 * Configuration key for target attribute.
	 */
	private static final String TRANSFORMER_TARGETATTRIBUTE_KEY =
		"targetattribute";
	
	/**
	 * attribute name to store the rendered velocity template in.
	 */
	private String targetAttribute;
	/**
	 * Velocity template manager.
	 */
	private ITemplateManager vtm;
	
	/**
	 * Velocity template to render.
	 */
	private ITemplate tpl;
	
	/**
	 * Log4j logger for debug and error messages.
	 */
	private static Logger logger = Logger.getLogger(VelocityTransformer.class);
	
	/**
	 * Creates instance of MergeTransformer.
	 * @param config configuration for the MergeTransformer
	 */
	public VelocityTransformer(final GenericConfiguration config) {
		super(config);
		
		String template = (String) config.get(TRANSFORMER_TEMPLATE_KEY);
		targetAttribute = (String) config.get(TRANSFORMER_TARGETATTRIBUTE_KEY);
		
		if (template == null) {
			logger.error("Please configure " + TRANSFORMER_TEMPLATE_KEY
					+ " for my config.");
		} else if(config instanceof CRConfigUtil) {
				CRConfigUtil crConfigUtil = (CRConfigUtil) config;
				vtm = crConfigUtil.getTemplateManager();
				try {
					tpl = new StringTemplate(template);
				} catch (CRException e) {
					e.printStackTrace();
				}
		}
		if (targetAttribute == null) {
			logger.error("Please configure " + TRANSFORMER_TARGETATTRIBUTE_KEY
					+ " for my config.");
		}
	}
	
	@Override
	public final void processBean(final CRResolvableBean bean) {
		vtm.put("page", bean);
		try {
			String output = vtm.render(tpl.getKey(), tpl.getSource());
			if (output != null && targetAttribute != null) {
				bean.set(targetAttribute, output);
			}
		} catch (CRException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void destroy() {
		
	}

}
