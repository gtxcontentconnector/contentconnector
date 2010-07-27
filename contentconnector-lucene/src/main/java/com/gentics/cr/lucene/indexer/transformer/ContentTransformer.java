package com.gentics.cr.lucene.indexer.transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.gentics.api.lib.exception.ParserException;
import com.gentics.api.lib.expressionparser.Expression;
import com.gentics.api.lib.expressionparser.ExpressionEvaluator;
import com.gentics.api.lib.expressionparser.ExpressionParser;
import com.gentics.api.lib.expressionparser.ExpressionParserException;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.monitoring.MonitorFactory;
import com.gentics.cr.monitoring.UseCase;


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
	private String rule;
	
	private static final String TRANSFORMER_RULE_KEY="rule";
	private String transformerkey="";
	
	/**
	 * Gets the Transformerkey of current Transformer
	 * @return
	 */
	public String getTransformerKey()
	{
		return transformerkey;
	}
	
	protected void setTransformerkey(String key)
	{
		this.transformerkey = key; 
	}
	
	protected ContentTransformer(GenericConfiguration config)
	{
		rule = (String)config.get(TRANSFORMER_RULE_KEY);
		try {
			expr = ExpressionParser.getInstance().parse(rule);
		} catch (ParserException e) {
			log.error("Could not generate valid Expression from configured Rule: "+rule, e);
		}
	}
	
	/**
	 * Destroys the transformer.
	 */
	public abstract void destroy();
	
	/**
	 * Process the specified bean with monitoring
	 * @param bean
	 * @throws CRException
	 */
	public void processBeanWithMonitoring(CRResolvableBean bean) throws CRException
	{
		UseCase pcase = MonitorFactory.startUseCase("Transformer:"+this.getClass());
		try
		{
			processBean(bean);
		}
		finally{
			pcase.stop();
		}
	}
	
	/**
	 * Processes the specified bean
	 * @param bean
	 * @throws CRException throws exception if bean could not be processed
	 */
	public abstract void processBean(CRResolvableBean bean)throws CRException;
	
	/**
	 * Tests if the specified CRResolvableBean should be processed by the transformer
	 * @param object
	 * @return true if rule matches
	 */
	public boolean match(CRResolvableBean object)
	{
		if(object!=null)
		{
			try {
				return(evaluator.match(expr, object));
			}catch (ExpressionParserException e) {
				log.error("Could not evaluate Expression with gived object and rule: "+rule, e);
			}
		}
		return(false);
	}
	
	
	private static final String TRANSFORMER_CLASS_KEY="transformerclass";
	private static final String TRANSFORMER_KEY="transformer";
	
	/**
	 * Create List of ContentTransformers configured in config
	 * @param config
	 * @return
	 */
	public static List<ContentTransformer> getTransformerList(GenericConfiguration config)
	{
		GenericConfiguration tconf = (GenericConfiguration)config.get(TRANSFORMER_KEY);
		if(tconf!=null)
		{
			Map<String,GenericConfiguration> confs = tconf.getSortedSubconfigs();
			if(confs!=null && confs.size()>0)
			{
				ArrayList<ContentTransformer> ret = new ArrayList<ContentTransformer>(confs.size());
				for(Map.Entry<String,GenericConfiguration> e:confs.entrySet())
				{
					GenericConfiguration c = e.getValue();
					String transformerClass = (String)c.get(TRANSFORMER_CLASS_KEY);
					try
					{
						ContentTransformer t = null;
						t = (ContentTransformer) Class.forName(transformerClass).getConstructor(new Class[] {GenericConfiguration.class}).newInstance(c);
						if(t!=null)
						{
							t.setTransformerkey(e.getKey());
							ret.add(t);
						}
					}
					catch(Exception ex)
					{
						log.error("Invalid configuration found. Could not instantiate "+transformerClass);
						ex.printStackTrace();
					}
					
				}
				return(ret);
			}
		}
		
		return null;
	}
}
