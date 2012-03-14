package com.gentics.cr.lucene.indexer.transformer.other;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;
/**
 * Converts the value of the attriute in .toString()
 * @author Paul Cervenka
 *
 */
public class ToString extends ContentTransformer {

	private static final String TRANSFORMER_ATTRIBUTE_KEY="attribute";
	private String attribute;
	
	/**
	 * Creates instance of SetValueTransformer
	 * @param config
	 */
	public ToString(GenericConfiguration config){
		super(config);
		attribute = (String)config.get(TRANSFORMER_ATTRIBUTE_KEY);
	}
	
	@Override
	public void processBean(CRResolvableBean bean) {
		Object value = bean.get(attribute);
		
		if (value != null)
			bean.set(attribute, value.toString());
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
