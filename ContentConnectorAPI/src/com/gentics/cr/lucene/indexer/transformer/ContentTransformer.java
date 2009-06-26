package com.gentics.cr.lucene.indexer.transformer;

import java.io.Reader;
import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;

import com.gentics.api.lib.exception.ParserException;
import com.gentics.api.lib.expressionparser.Expression;
import com.gentics.api.lib.expressionparser.ExpressionEvaluator;
import com.gentics.api.lib.expressionparser.ExpressionParser;
import com.gentics.api.lib.expressionparser.ExpressionParserException;
import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.cr.configuration.GenericConfiguration;


/**
 * 
 * Last changed: $Date: 2009-06-24 17:10:19 +0200 (Mi, 24 Jun 2009) $
 * @version $Revision: 99 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public abstract class ContentTransformer {
	protected static Logger log = Logger.getLogger(ContentTransformer.class);
	private static ExpressionEvaluator evaluator = new ExpressionEvaluator();
	private Expression expr;
	
	private static final String TRANSFORMER_RULE_KEY="rule";
	
	protected ContentTransformer(GenericConfiguration config)
	{
		String rule = (String)config.get(TRANSFORMER_RULE_KEY);
		try {
			expr = ExpressionParser.getInstance().parse(rule);
		} catch (ParserException e) {
			e.printStackTrace();
		}
	}
		
	/**
	 * Get transformed contents as String
	 * @param obj
	 * @return
	 */
	public abstract String getStringContents(Object obj);
	
	/**
	 * Get transformed contents as Reader
	 * @param obj
	 * @return
	 */
	public abstract Reader getContents(Object obj);
	
	
	/**
	 * Match an Resolvable to a Rule
	 * @param object
	 * @param rule
	 * @return true if rule matches
	 */
	public boolean match(Resolvable object)
	{
		if(object!=null)
		{
			try {
				return(evaluator.match(expr, object));
			}catch (ExpressionParserException e) {
				e.printStackTrace();
			}
		}
		return(false);
	}
	
	private static final String TRANSFORMER_ATTRIBUTE_KEY="attribute";
	private static final String TRANSFORMER_CLASS_KEY="transformerclass";
	private static final String TRANSFORMER_KEY="transformer";
	
	/**
	 * Create table of ContentTransformers configured in config
	 * @param config
	 * @return
	 */
	public static Hashtable<String,ContentTransformer> getTransformerTable(GenericConfiguration config)
	{
		GenericConfiguration tconf = (GenericConfiguration)config.get(TRANSFORMER_KEY);
		if(tconf!=null)
		{
			Hashtable<String,GenericConfiguration> confs = tconf.getSubConfigs();
			if(confs!=null && confs.size()>0)
			{
				Hashtable<String,ContentTransformer> ret = new Hashtable<String,ContentTransformer>(confs.size());
				for(Map.Entry<String,GenericConfiguration> e:confs.entrySet())
				{
					try
					{
						GenericConfiguration c = e.getValue();
						String attribute = (String)c.get(TRANSFORMER_ATTRIBUTE_KEY);
						String transformerClass = (String)c.get(TRANSFORMER_CLASS_KEY);
						ContentTransformer t = null;
						t = (ContentTransformer) Class.forName(transformerClass).getConstructor(new Class[] {GenericConfiguration.class}).newInstance(c);
						if(t!=null && attribute!=null)
						{
							ret.put(attribute, t);
						}
					}
					catch(Exception ex)
					{
						log.error("Invalid configuration found. Could not construct ContentTransformer Map");
						ex.printStackTrace();
					}
					
				}
				return(ret);
			}
		}
		
		return null;
	}
}
