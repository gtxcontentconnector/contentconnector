package com.gentics.cr.lucene.search.highlight;

import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.search.Query;

import com.gentics.api.lib.exception.ParserException;
import com.gentics.api.lib.expressionparser.Expression;
import com.gentics.api.lib.expressionparser.ExpressionEvaluator;
import com.gentics.api.lib.expressionparser.ExpressionParser;
import com.gentics.api.lib.expressionparser.ExpressionParserException;
import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.cr.configuration.GenericConfiguration;

/**
 * Content highlighter.
 * Last changed: $Date: 2009-06-26 15:48:16 +0200 (Fr, 26 Jun 2009) $
 * @version $Revision: 105 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public abstract class ContentHighlighter {

	/**
	 * logger.
	 */
	private static Logger log = Logger.getLogger(ContentHighlighter.class);
	/**
	 * evaluator.
	 */
	private static ExpressionEvaluator evaluator = new ExpressionEvaluator();
	/**
	 * expression.
	 */
	private Expression expr;
	
	/**
	 * highlight attribute.
	 */
	private String highlightAttribute = null;
	
	/**
	 * highlighter attribute key.
	 */
	private static final String HIGHLIGHTER_ATTRIBUTE_KEY = "attribute";
	/**
	 * highlighter rule key.
	 */
	private static final String HIGHLIGHTER_RULE_KEY = "rule";
	
	 /**
	   * Unicode Punctuation Characters.<br />
	   * \\u2013 == &ndash;
	   */
	  protected static final String UNICODE_PUNCT_CHARS = "\\u2013";
	  
	  /**
	   * regex to remove text from fragments (e.g. leading commas and spaces). 
	   * Remove all punctuations and whitespaces at the beginning 
	   * except opening brackets
	   */
	  protected static final String REMOVE_TEXT_FROM_FRAGMENT_REGEX 
	  	= "^[\\p{Punct}\\p{Space}" + UNICODE_PUNCT_CHARS + "&&[^(<]]*";

	
	/**
	 * Returns the hightlight attribute that is highlighted by this
	 * contentHighlighter.
	 * @return highlighted attribute.
	 */
	public final String getHighlightAttribute() {
		return (highlightAttribute);
	}
	/**
	 * Constructor.
	 * @param config configuration.
	 */
	protected ContentHighlighter(final GenericConfiguration config) {
		String rule = (String) config.get(HIGHLIGHTER_RULE_KEY);
		highlightAttribute = (String) config.get(HIGHLIGHTER_ATTRIBUTE_KEY);
		try {
			expr = ExpressionParser.getInstance().parse(rule);
		} catch (ParserException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Highlights the configured attribute of a CRResolvableBean.
	 * @param attribute attribute
	 * @param parsedQuery query
	 * @return  highlighted text.
	 */
	public abstract String highlight(String attribute, Query parsedQuery);
	
	/**
	 * Match an Resolvable to a Rule.
	 * @param object resolvable
	 * @return true if rule matches
	 */
	public final boolean match(final Resolvable object) {
		if (object != null) {
			try {
				return (evaluator.match(expr, object));
			} catch (ExpressionParserException e) {
				e.printStackTrace();
			}
		}
		return (false);
	}
	
	/**
	 * highlighter class key.
	 */
	private static final String HIGHLIGHTER_CLASS_KEY = "class";
	/**
	 * highlighter key.
	 */
	private static final String HIGHLIGHTER_KEY = "highlighter";
	
	/**
	 * Create table of ContentTransformers configured in config.
	 * @param config configuration.
	 * @return transformer table
	 */
	public static Hashtable<String, ContentHighlighter>
			getTransformerTable(final GenericConfiguration config) {
		GenericConfiguration tconf = (GenericConfiguration)
				config.get(HIGHLIGHTER_KEY);
		if (tconf != null) {
			Hashtable<String, GenericConfiguration> confs 
					= tconf.getSubConfigs();
			if (confs != null && confs.size() > 0) {
				Hashtable<String, ContentHighlighter> ret =
					new Hashtable<String, ContentHighlighter>(confs.size());
				for (Map.Entry<String, GenericConfiguration>
						e : confs.entrySet()) {
					try {
						GenericConfiguration c = e.getValue();
						String attribute = (String) 
									c.get(HIGHLIGHTER_ATTRIBUTE_KEY);
						String highlighterClass = (String) 
									c.get(HIGHLIGHTER_CLASS_KEY);
						ContentHighlighter t = null;
						t = (ContentHighlighter) Class.forName(highlighterClass)
								.getConstructor(new Class[] 
								{GenericConfiguration.class}).newInstance(c);
						if (t != null && attribute != null) {
							ret.put(attribute, t);
						}
					} catch (Exception ex) {
						log.error("Invalid configuration found. "
								+ "Could not construct ContentHighlighter Map");
						ex.printStackTrace();
					}
					
				}
				return (ret);
			}
		}
		
		return null;
	}
}
