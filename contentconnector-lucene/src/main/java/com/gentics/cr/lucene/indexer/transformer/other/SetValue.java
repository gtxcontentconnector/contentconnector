package com.gentics.cr.lucene.indexer.transformer.other;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;
/**
 * Sets a configured value
 * @author Andreas Perhab
 *
 */
public class SetValue extends ContentTransformer {

	private static final String TRANSFORMER_ATTRIBUTE_KEY="attribute";
	private static final String TRANSFORMER_VALUE_KEY="value";
	private String attribute;
	private String value;
	
	/**
	 * Creates instance of SetValueTransformer
	 * @param config
	 */
	public SetValue(GenericConfiguration config){
		super(config);
		attribute = (String)config.get(TRANSFORMER_ATTRIBUTE_KEY);
		value = (String)config.get(TRANSFORMER_VALUE_KEY);
	}
	
	@Override
	public void processBean(CRResolvableBean bean) {
		bean.set(attribute, value);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
