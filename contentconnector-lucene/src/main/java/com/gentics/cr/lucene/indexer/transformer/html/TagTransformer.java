package com.gentics.cr.lucene.indexer.transformer.html;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.gentics.api.lib.exception.ParserException;
import com.gentics.api.lib.expressionparser.EvaluableExpression;
import com.gentics.api.lib.expressionparser.ExpressionEvaluator;
import com.gentics.api.lib.expressionparser.ExpressionParserException;
import com.gentics.api.lib.expressionparser.ExpressionQueryRequest;
import com.gentics.api.lib.resolving.PropertyResolver;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;
import com.gentics.cr.portalnode.expressions.ExpressionParserHelper;

/**
 * Add new attributes to certain html tags.
 * @author bigbear3001
 *
 */
public class TagTransformer extends ContentTransformer {

	/**
	 * Configuration key for the tags to replace.
	 */
	private static final String TAGS_KEY = "tags";
	
	/**
	 * Configuration key for the attributes to add if they are empty.
	 */
	private static final String ADDATTRIBUTESIFEMPTY_KEY =
		"ADDATTRIBUTESIFEMPTY";
	
	/**
	 * Configuration key for target attribute.
	 */
	private static final String TARGETATTRIBUTE_KEY =
		"targetattribute";
	
	/**
	 * attribute name to store the rendered velocity template in.
	 */
	private String targetAttribute;
	
	/**
	 * Regex for finding tags in the attribute.
	 */
	private Pattern tagRegex = Pattern.compile("(?i)<([^\\s>]+)(.*?)>");
	
	/**
	 * Regex for finding attributes of the tag.
	 */
	private Pattern attributeRegex =
		Pattern.compile("\\s([a-z0-9_]+)=\"?([^\">]*)\"?");
	
	/**
	 * Name of the configuration for error messages.
	 */
	private String configName;
	
	/**
	 * Collection of Tagnames that should be processed.
	 */
	private Collection<String> tagNames;
	
	/**
	 * Configuration for attributes to be added to each matching tag.
	 */
	private HashMap<String, EvaluableExpression> addAttributesIfEmpty =
		new HashMap<String, EvaluableExpression>();
	
	/**
	 * Log4j logger for debug and error messages.
	 */
	private static Logger logger = Logger.getLogger(TagTransformer.class);
	
	/**
	 * Creates instance of TagTransformer.
	 * @param config configuration for the TagTransformer
	 */
	public TagTransformer(final GenericConfiguration config) {
		super(config);
		CRConfigUtil crConfigUtil;
		if (config instanceof CRConfigUtil) {
			crConfigUtil = (CRConfigUtil) config;
		} else {
			crConfigUtil = new CRConfigUtil(config,
					"DynamicTagTransformerConfig");
		}
		configName = crConfigUtil.getName();
		targetAttribute = config.getString(TARGETATTRIBUTE_KEY);
		tagNames =
			config.getMultipleString(TAGS_KEY, ",", new Vector<String>());
		GenericConfiguration addAttributesIfEmptyConfig =
			config.getSubConfigs().get(ADDATTRIBUTESIFEMPTY_KEY);
		if (addAttributesIfEmptyConfig != null) {
			for (Object attributeNameObject
					: addAttributesIfEmptyConfig.getProperties().keySet()) {
				String attributeName = (String) attributeNameObject;
				String attributeValue =
					(String) addAttributesIfEmptyConfig.get(attributeName);
				try {
					EvaluableExpression attributeExpression =
						(EvaluableExpression)
							ExpressionParserHelper.parse(attributeValue);
					addAttributesIfEmpty.put(attributeName,
							attributeExpression);
				} catch (ParserException e) {
					logger.error("Cannot parse the attribute " + configName
							+ "." + ADDATTRIBUTESIFEMPTY_KEY + attributeName
							+ " into an expression. (" + attributeValue
							+ ")", e);
				}
			}
		}
	}
	
	@Override
	public final void processBean(final CRResolvableBean bean) {
		String value = bean.getString(targetAttribute);
		StringBuffer newValue = new StringBuffer();
		if (value != null) {
			Matcher tagMatcher = tagRegex.matcher(value);
			while (tagMatcher.find()) {
				String tagName = tagMatcher.group(1);
				if (tagNames.contains(tagName)) {
					String newCode = handleTag(bean, tagMatcher.group(1),
							tagMatcher.group(2), tagMatcher.group(0));
					if (newCode != null) {
						tagMatcher.appendReplacement(newValue, newCode);
					}
				}
			}
			if (newValue.length() != 0) {
				tagMatcher.appendTail(newValue);
				bean.set(targetAttribute, newValue.toString());
			}
		}
	}
	/**
	 * handle a single tag and return replacement.
	 * @param bean - bean representing the page.
	 * @param tagName - name of the tag
	 * @param attributeString - string containing the attributes
	 * @param html - complete html source of the tag
	 * @return replacement String for the tag
	 */
	private String handleTag(final CRResolvableBean bean, final String tagName,
			final String attributeString, final String html) {
		StringBuffer addAttributesToTag = new StringBuffer();
		logger.debug("Handling: " + html);
		Matcher attributeMatcher =
			attributeRegex.matcher(attributeString);
		Map<String, Object> attributes = new HashMap<String, Object>();
		while (attributeMatcher.find()) {
			logger.debug("Handling attribute:"
					+ attributeMatcher.group(0));
			attributes.put(attributeMatcher.group(1),
					attributeMatcher.group(2));
		}
		CRResolvableBean resolvable = new CRResolvableBean();
		resolvable.setAttrMap(attributes);
		resolvable.set("object", bean);
		resolvable.set("page", bean);
		PropertyResolver resolver = new PropertyResolver(resolvable);
		ExpressionQueryRequest expressionQueryRequest = 
			new ExpressionQueryRequest(resolver, attributes);

		for (String addAttributeIfEmpty
				: addAttributesIfEmpty.keySet()) {
			EvaluableExpression expression =
				addAttributesIfEmpty.get(addAttributeIfEmpty);
			try {
				String result = (String) expression.evaluate(
						expressionQueryRequest,
						ExpressionEvaluator.OBJECTTYPE_STRING);
				if (result != null && !result.equals("")) {
					addAttributesToTag.append(" "
							+ addAttributeIfEmpty.toLowerCase() + "=\"" + result
							+ "\"");
				}
				
			} catch (ExpressionParserException e) {
				logger.error("Cannot evaluate expression ("
						+ expression.getExpressionString() + ")as String.", e);
			}
		}
		if (addAttributesToTag.length() != 0) {
			return html.replace(">", addAttributesToTag.toString() + ">");
		} else {
			return null;
		}
	}

	@Override
	public void destroy() {
		
	}

}
