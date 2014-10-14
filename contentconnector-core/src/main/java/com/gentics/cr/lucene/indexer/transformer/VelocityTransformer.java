package com.gentics.cr.lucene.indexer.transformer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.template.FileTemplate;
import com.gentics.cr.template.ITemplate;
import com.gentics.cr.template.ITemplateManager;
import com.gentics.cr.template.StringTemplate;
import com.gentics.cr.util.velocity.VelocityTools;

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
	private static final String TRANSFORMER_TEMPLATE_KEY = "template";

	/**
	 * Path to a velocity template to use for transformation.
	 */
	private static final String TRANSFORMER_TEMPLATE_PATH_KEY = "templatepath";

	/**
	 * optionally we can read the template form an attribute instead of a hardcoded template.
	 */
	private static final String TRANSFORMER_SOURCEATTRIBUTE_KEY = "sourceattribute";

	/**
	 * Configuration key for target attribute.
	 */
	private static final String TRANSFORMER_TARGETATTRIBUTE_KEY = "targetattribute";

	/**
	 * Defines whether the parsed velocity should replace or be appended to the targetattribute.
	 */
	private static final String TRANSFORMER_APPEND_KEY = "append";

	/**
	 * user can define additional contextvars in java properties format.
	 */
	private static final String TRANSFORMER_ADDITIONAL_CONTEXTVARS = "contextvars";

	/**
	 * attribute name to store the rendered velocity template in.
	 */
	private String targetAttribute;

	/**
	 * By default the parsed velocity is set onto the targetAttribute. 
	 * Setting this option to "true" the parsed velocity can be appended to the end of the targetAttribute's value.
	 */
	private boolean appendToTargetAttribute = false;

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
	 * Creates instance of VelocityTransformer.
	 * @param config configuration for the VelocityTransformer
	 */
	public VelocityTransformer(final GenericConfiguration config) {
		super(config);
		if (config instanceof CRConfigUtil) {
			crConfigUtil = (CRConfigUtil) config;
		} else {
			crConfigUtil = new CRConfigUtil(config, "VelocityTransformerConfig");
		}
		configName = crConfigUtil.getName();
		String template = (String) config.get(TRANSFORMER_TEMPLATE_KEY);
		String templatePath = (String) config.get(TRANSFORMER_TEMPLATE_PATH_KEY);
		targetAttribute = (String) config.get(TRANSFORMER_TARGETATTRIBUTE_KEY);
		sourceAttribute = (String) config.get(TRANSFORMER_SOURCEATTRIBUTE_KEY);
		String append = (String) config.get(TRANSFORMER_APPEND_KEY);
		if (append != null && (append.equals("1") || append.toLowerCase().equals("true"))) {
			appendToTargetAttribute = true;
		}

		if (sourceAttribute != null) {
			// we use the source attribute as template ...
		} else {
			if (template != null) {
				logger.debug("Using template configured using var: " + TRANSFORMER_TEMPLATE_KEY + ".");
				try {
					tpl = new StringTemplate(template);
				} catch (CRException e) {
					e.printStackTrace();
				}
			} else if (templatePath != null) {
				logger.debug("Using template: " + templatePath);
				try {
					tpl = getFileTemplate(templatePath);
				} catch (FileNotFoundException e) {
					logger.error("Could not find template (" + templatePath + ")", e);
				} catch (CRException e) {
					logger.error("Could not load template (" + templatePath + ")", e);
				} catch (UnsupportedEncodingException e) {
					logger.error("Could not find encoding", e);
				}
			} else {
				logger.error("Neither " + TRANSFORMER_TEMPLATE_KEY + " nor " + TRANSFORMER_TEMPLATE_PATH_KEY + " nor "
						+ TRANSFORMER_SOURCEATTRIBUTE_KEY + " are configured. "
						+ "This transformer won't work correctly.");
			}

		}
		if (targetAttribute == null) {
			if (sourceAttribute != null) {
				targetAttribute = sourceAttribute;
			} else {
				logger.error("Please configure " + TRANSFORMER_TARGETATTRIBUTE_KEY + " for my config.");
			}
		}
		readAdditionalContextVars(config);
	}

	/**
	 * Load template from file.
	 * @param templatePath Relative path of the template (CRConfigUtil.DEFAULT_TEMPLATE_PATH is prefixed).
	 * @return FileTemplate
	 * @throws FileNotFoundException file not found/accessible
	 * @throws CRException Exception creating the FileTemplate
	 * @throws UnsupportedEncodingException 
	 */
	private FileTemplate getFileTemplate(final String templatePath) throws FileNotFoundException, CRException,
			UnsupportedEncodingException {
		File file = new File(templatePath);
		if (!file.isAbsolute()) {
			file = new File(CRConfigUtil.DEFAULT_TEMPLATE_PATH + File.separator + templatePath);
		}

		FileInputStream inStream = new FileInputStream(file);
		InputStreamReader streamReader = new InputStreamReader(inStream, "UTF-8");
		BufferedReader bufferedReader = new BufferedReader(streamReader);
		return new FileTemplate(bufferedReader);
	}

	/**
	 * read the additiona context vars from the configuration property.
	 * @param config needed for getting the context vars from the config.
	 */
	private void readAdditionalContextVars(final GenericConfiguration config) {
		String additionalContextVars = (String) config.get(TRANSFORMER_ADDITIONAL_CONTEXTVARS);
		if (additionalContextVars != null) {
			Properties props = new Properties();
			try {
				props.load(IOUtils.toInputStream(additionalContextVars));
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
		vtm.put("properties", parameters);
		for (Iterator<Entry<String, Object>> i = additionalAttributes.entrySet().iterator(); i.hasNext();) {
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
				if (appendToTargetAttribute) {
					Object target = bean.get(targetAttribute);
					if (target != null && target instanceof String) {
						String mergedString = target.toString() + output;
						bean.set(targetAttribute, mergedString);
					}
				} else {
					bean.set(targetAttribute, output);
				}
			}
		} catch (Exception e) {
			logger.error("Error while rendering template " + configName + " - " + TRANSFORMER_TEMPLATE_KEY
					+ " for bean " + bean.getContentid(), e);
		}
	}

	@Override
	public void destroy() {

	}

}
