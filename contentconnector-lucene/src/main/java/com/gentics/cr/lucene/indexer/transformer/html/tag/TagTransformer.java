package com.gentics.cr.lucene.indexer.transformer.html.tag;


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
	 * Configuration key to attributes to replace.
	 */
	private static final String REPLACEATTRIBUTES_KEY = "REPLACEATTRIBUTES";
	
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
	private HashMap<String, EvaluableExpression> addAttributesIfEmpty;

	/**
	 * Configuration for attributes to be replaced in each matching tag.
	 */
	private HashMap<String, EvaluableExpression> replaceAttributes;

	/**
	 * Configuration of the TagTransformer.
	 */
	private CRConfigUtil crConfig;
	
	/**
	 * AttributeCallback for adding attributes.
	 */
	private static AttributeCallback addAttributeCallback = new AddAttributeCallback();
	
	/**
	 * AttributeCallback for replacing attributes.
	 */
	private static AttributeCallback replaceAttributeCallback = new ReplaceAttributeCallback();
	
	
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
		crConfig = crConfigUtil;
		configName = crConfigUtil.getName();
		targetAttribute = config.getString(TARGETATTRIBUTE_KEY);
		tagNames =
			config.getMultipleString(TAGS_KEY, ",", new Vector<String>());
		addAttributesIfEmpty = initAttributeDefintion(ADDATTRIBUTESIFEMPTY_KEY);
		replaceAttributes = initAttributeDefintion(REPLACEATTRIBUTES_KEY);
		
	}
	
	/**
	 * initialize the attribute replacing/add definitions from the specified cofniguration key.
	 * @param configurationKey - configuration key to use.
	 * @return HashMap with attribute name and their expressions.
	 */
	private HashMap<String, EvaluableExpression> initAttributeDefintion(final String configurationKey) {
		HashMap<String, EvaluableExpression> attributeDefinition = new HashMap<String, EvaluableExpression>();
		GenericConfiguration attributesConfig = crConfig.getSubConfigs().get(configurationKey);
		if (attributesConfig != null) {
			for (Object attributeNameObject : attributesConfig.getProperties().keySet()) {
				String attributeName = (String) attributeNameObject;
				String attributeValue = (String) attributesConfig.get(attributeName);
				try {
					EvaluableExpression attributeExpression = (EvaluableExpression) ExpressionParserHelper
							.parse(attributeValue);
					attributeDefinition.put(attributeName, attributeExpression);
				} catch (ParserException e) {
					logger.error("Cannot parse the attribute " + configName + "." + configurationKey + "."
							+ attributeName + " into an expression. (" + attributeValue + ")", e);
				}
			}
		}
		return attributeDefinition;
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
		logger.debug("Handling: " + html);
		StringBuffer result = new StringBuffer(html);
		int changes = 0;
		Map<String, Object> attributes = parseAttributes(attributeString);
		
		ExpressionQueryRequest expressionQueryRequest = prepareQueryRequest(bean, attributes);
		
		changes += processAttributes(result, expressionQueryRequest, addAttributesIfEmpty, addAttributeCallback);
		changes += processAttributes(result, expressionQueryRequest, replaceAttributes, replaceAttributeCallback);
		
		if (changes > 0) {
			return result.toString();
		} else {
			return null;
		}
	}

	/**
	 * prepare the query request used to resolve attribute definition expressions.
	 * @param bean - resolvable to resolve via "object" and "page"
	 * @param attributes - attributes to resolve via the keys of the map
	 * @return {@link ExpressionQueryRequest} to use for evaluating the expressions
	 */
	private ExpressionQueryRequest prepareQueryRequest(final CRResolvableBean bean,
			final Map<String, Object> attributes) {
		CRResolvableBean resolvable = new CRResolvableBean();
		resolvable.setAttrMap(attributes);
		resolvable.set("object", bean);
		resolvable.set("page", bean);
		PropertyResolver resolver = new PropertyResolver(resolvable);
		return new ExpressionQueryRequest(resolver, new HashMap<String, String>(0));
		
	}

	/**
	 * parse the attributes from the string and return them as map.
	 * @param attributeString - string to get the attributes from.
	 * has to be in the form ' attribute1="value1" ... attributeN="valueN"'
	 * @return map with attribute names as keys and attribute values as values
	 */
	private Map<String, Object> parseAttributes(final String attributeString) {
		Matcher attributeMatcher =
			attributeRegex.matcher(attributeString);
		Map<String, Object> attributes = new HashMap<String, Object>();
		while (attributeMatcher.find()) {
			logger.debug("Handling attribute:"
					+ attributeMatcher.group(0));
			attributes.put(attributeMatcher.group(1),
					attributeMatcher.group(2));
		}
		return attributes;
	}

	/**
	 * process the attribute defintion and call the given callback with each match.
	 * @param html - html code of the tag to perform the changes on.
	 * @param expressionQueryRequest - {@link ExpressionQueryRequest} to use for evaluating the attribute definitions.
	 * @param attributesDefintion - map with attribute name as key and evaluable expression to evaluate for the
	 * attribute value as value.
	 * @param attributeCallback - callback to call when the expression for the attribute doesn't return null or an empty
	 * string.
	 * @return number of changes performed on the html StringBuffer.
	 */
	private int processAttributes(final StringBuffer html, final ExpressionQueryRequest expressionQueryRequest,
			final HashMap<String, EvaluableExpression> attributesDefintion, final AttributeCallback attributeCallback) {
		int changes = 0;
		for (String attributeName : attributesDefintion.keySet()) {
			EvaluableExpression expression = attributesDefintion.get(attributeName);
			try {
				String result =
					(String) expression.evaluate(expressionQueryRequest, ExpressionEvaluator.OBJECTTYPE_STRING);
				if (result != null && !result.equals("")) {
					attributeCallback.invokeCallback(html, attributeName.toLowerCase(), result);
					changes++;
				}
				
			} catch (ExpressionParserException e) {
				logger.error("Cannot evaluate expression (" + expression.getExpressionString() + ")as String.", e);
			}
		}
		return changes;
	}

	@Override
	public void destroy() {
		
	}

}
