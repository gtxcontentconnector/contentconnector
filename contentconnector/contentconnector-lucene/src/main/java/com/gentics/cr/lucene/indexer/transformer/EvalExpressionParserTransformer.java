package com.gentics.cr.lucene.indexer.transformer;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.gentics.api.lib.exception.ParserException;
import com.gentics.api.lib.expressionparser.EvaluableExpression;
import com.gentics.api.lib.expressionparser.ExpressionEvaluator;
import com.gentics.api.lib.expressionparser.ExpressionParser;
import com.gentics.api.lib.expressionparser.ExpressionParserException;
import com.gentics.api.lib.expressionparser.ExpressionQueryRequest;
import com.gentics.api.lib.resolving.PropertyResolver;
import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;

/**
 * Last changed: $Date: 2011-05-13 10:40:00 +0200 (Fr, 13 May 2011) $
 * 
 * @version $Revision:  $
 * @author $Author: bernhard.friedreich@extern.brz.gv.at $
 * 
 */
public class EvalExpressionParserTransformer extends ContentTransformer {
	private static final String SRC_ATTRIBUTE_KEY = "srcattribute";
	private static final String TARGET_ATTRIBUTE_KEY = "targetattribute";
	private String providedExpression = "";
	private String evaluatedTarget = "expressionresult";

	private static Logger LOGGER = Logger.getLogger(EvalExpressionParserTransformer.class);
	
	/**
	 * Create Instance of EvalExpressionParserTransformer.
	 * 
	 * @param config
	 */
	public EvalExpressionParserTransformer(final GenericConfiguration config) {
		super(config);
		providedExpression = (String) config.get(SRC_ATTRIBUTE_KEY);
		evaluatedTarget = (String) config.get(TARGET_ATTRIBUTE_KEY);
	}

	@Override
	public void processBean(final CRResolvableBean bean) {
		if (providedExpression != null) {
			String evaluatedExpression = evaluateString(providedExpression, bean);
			if (evaluatedExpression != null) {
				bean.set(evaluatedTarget, evaluatedExpression);
			}
		}
	}

	/**
	 * Used to evaluate an expression like concat("/Content.Node/", kategorie, filename) using ExpressionParser.
	 * @param expressionString String to evaluate
	 * @param resolvable Resolvable used to resolve properties (e.g. filename)
	 * @return Result as string
	 */
	@SuppressWarnings("unchecked")
	private static String evaluateString(final String expressionString, final Resolvable resolvable){
	    String result = null;
	    ExpressionParser expressionParser = ExpressionParser.getInstance();
	    try {
	            EvaluableExpression expression = (EvaluableExpression) expressionParser.parse(expressionString);
	            ExpressionQueryRequest expressionQueryRequest = 
	            	new ExpressionQueryRequest(new PropertyResolver(resolvable), new HashMap<Object, Object>(0));
	            result = 
	            	(String) expression.evaluate(expressionQueryRequest, ExpressionEvaluator.OBJECTTYPE_STRING);
	            
	    } catch (ExpressionParserException e) {
	            LOGGER.error("Error while evaluating the expression (" 
	            		+ expressionString + ") with the base resolvable (" + resolvable + ")", e);
	    } catch (ParserException e) {
	            LOGGER.error("Error parsing the expression (" + expressionString + ").", e);
	    }
	    return result;
	}
	
	@Override
	public void destroy() {

	}

}
