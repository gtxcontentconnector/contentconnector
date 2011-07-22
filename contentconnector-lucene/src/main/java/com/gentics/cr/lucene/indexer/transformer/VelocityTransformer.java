package com.gentics.cr.lucene.indexer.transformer;


import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.template.ITemplate;
import com.gentics.cr.template.ITemplateManager;
import com.gentics.cr.template.StringTemplate;

/**
 * can be used in one of two ways:<br>
 * <br>
 * 1. render a configured template into a configured targetattribute
 * (attributes: 'template', 'targetattribute') <br>
 * <br>
 * 2. render a 'sourceattribute' into the (optional) 'targetattribute'
 * 
 * 
 * you can also add a config attribute named 'contextvars' which contains a java
 * properties format which will be put into the velocity context. (it also
 * supports dots - e.g. portal.properties.test=123)
 * 
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
	 * optionally we can read the template form an attribute instead of a hardcoded template.
	 */
	private static final String TRANSFORMER_SOURCEATTRIBUTE_KEY = "sourceattribute";
	
	/**
	 * Configuration key for target attribute.
	 */
	private static final String TRANSFORMER_TARGETATTRIBUTE_KEY =
		"targetattribute";
	
	/**
	 * user can define additional contextvars in java properties format.
	 */
	private static final String TRANSFORMER_ADDITIONAL_CONTEXTVARS = "contextvars";
	
	/**
	 * attribute name to store the rendered velocity template in.
	 */
	private String targetAttribute;
	
	/**
	 * additional attributes.
	 */
	private Map<String, Object> additionalAttributes = new HashMap<String, Object>();
	
	/**
	 * Velocity template to render.
	 */
	private ITemplate tpl;
	
	/**
	 * Name of the configuration for error messages.
	 */
	private String configName;
	
	/**
	 * {@link VelocityTools} to deploy into the template context.
	 */
	private VelocityTools tools = new VelocityTools();
	
	/**
	 * Log4j logger for debug and error messages.
	 */
	private static Logger logger = Logger.getLogger(VelocityTransformer.class);
	
	/**
	 * Configuration for the VelocityTransformer.
	 */
	private CRConfigUtil crConfigUtil;


	/**
	 * attribute name containing the source template.
	 */
	private String sourceAttribute;
	
	/**
	 * Creates instance of MergeTransformer.
	 * @param config configuration for the MergeTransformer
	 */
	public VelocityTransformer(final GenericConfiguration config) {
		super(config);
		if (config instanceof CRConfigUtil) {
			crConfigUtil = (CRConfigUtil) config;
		} else {
			crConfigUtil = new CRConfigUtil(config,
					"VelocityTransformerConfig");
		}
		configName = crConfigUtil.getName();
		String template = (String) config.get(TRANSFORMER_TEMPLATE_KEY);
		targetAttribute = (String) config.get(TRANSFORMER_TARGETATTRIBUTE_KEY);
		sourceAttribute = (String) config.get(TRANSFORMER_SOURCEATTRIBUTE_KEY);
		
		if (sourceAttribute != null) {
			// we use the source attribute as template ...
		} else if (template == null) {
			logger.error("Please configure " + TRANSFORMER_TEMPLATE_KEY
					+ " for my config.");
		} else {
				try {
					tpl = new StringTemplate(template);
				} catch (CRException e) {
					e.printStackTrace();
				}
		}
		if (targetAttribute == null) {
			if (sourceAttribute != null) {
				targetAttribute = sourceAttribute;
			} else {
				logger.error("Please configure " + TRANSFORMER_TARGETATTRIBUTE_KEY
						+ " for my config.");
			}
		}
		readAdditionalContextVars(config);
	}

	/**
	 * read the additiona context vars from the configuration property.
	 */
	private void readAdditionalContextVars(final GenericConfiguration config) {
		String additionalContextVars = (String) config
				.get(TRANSFORMER_ADDITIONAL_CONTEXTVARS);
		if (additionalContextVars != null) {
			Properties props = new Properties();
			try {
				props.load(new StringReader(additionalContextVars));
				Iterator<Entry<Object, Object>> i = props.entrySet().iterator();
				additionalAttributes = new HashMap<String, Object>();
				while (i.hasNext()) {
					Entry<Object, Object> entry = i.next();
					if (entry.getKey() == null || entry.getValue() == null) {
						continue;
					}
					String key = entry.getKey().toString();
					String[] keyparts = key.split("\\.");
					Map<String, Object> lastprop = additionalAttributes;
					for (int j = 0; j < keyparts.length; j++) {
						if (j == keyparts.length - 1) {
							lastprop.put(keyparts[j], entry.getValue());
						} else {
							Map<String, Object> newprop = new HashMap<String, Object>();
							lastprop.put(keyparts[j], newprop);
							lastprop = newprop;
						}
					}
				}
			} catch (IOException e) {
				logger.error("Error while parsing additional context vars.", e);
			}
		}
	}
	
	@Override
	public final void processBean(final CRResolvableBean bean) {
		ITemplateManager vtm = crConfigUtil.getTemplateManager();
		vtm.put("page", bean);
		vtm.put("tools", tools);
		for (Iterator<Entry<String, Object>> i = additionalAttributes
				.entrySet().iterator(); i.hasNext();) {
			Entry<String, Object> entry = i.next();
			vtm.put(entry.getKey(), entry.getValue());
		}
		ITemplate tmpl = tpl;
		try {
			if (sourceAttribute != null) {
				tmpl = new StringTemplate(bean.getString(sourceAttribute));
			}
			String output = vtm.render(tmpl.getKey(), tmpl.getSource());
			if (output != null && targetAttribute != null) {
				bean.set(targetAttribute, output);
			}
		} catch (CRException e) {
			logger.error("Error while rendering template " + configName
					+ TRANSFORMER_TEMPLATE_KEY + " for bean "
					+ bean.getContentid(), e);
		}
		
	}

	@Override
	public void destroy() {
		
	}

}
