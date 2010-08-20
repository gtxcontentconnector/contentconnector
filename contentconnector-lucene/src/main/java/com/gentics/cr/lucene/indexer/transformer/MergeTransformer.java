package com.gentics.cr.lucene.indexer.transformer;

import org.apache.log4j.Logger;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;
/**
 * Sets a configured value
 * @author Paul Cervenka
 *
 */
public class MergeTransformer extends ContentTransformer {

	private static final String TRANSFORMER_SOUCREATTRIBUTE_KEY="sourceattributes";
	private static final String TRANSFORMER_TARGETATTRIBUTE_KEY="targetattribute";
	private String[] source_attributes;
	private String target_attribute;
	
	private static Logger logger = Logger.getLogger(MergeTransformer.class);
	
	/**
	 * Creates instance of SetValueTransformer
	 * @param config
	 */
	public MergeTransformer(GenericConfiguration config){
		super(config);
		
		String source = (String)config.get(TRANSFORMER_SOUCREATTRIBUTE_KEY);
		target_attribute = (String)config.get(TRANSFORMER_TARGETATTRIBUTE_KEY);
		source_attributes = source.split(",");
		
		if (source_attributes == null) { logger.error("Please configure "+TRANSFORMER_SOUCREATTRIBUTE_KEY + " for my config."); }
		if (target_attribute == null) { logger.error("Please configure "+TRANSFORMER_TARGETATTRIBUTE_KEY + " for my config."); }
	}
	
	@Override
	public void processBean(CRResolvableBean bean) {
		StringBuffer target;
				
		if (source_attributes != null && target_attribute != null) {
			target = new StringBuffer();
			for(String attribute:source_attributes) {
				if (bean.get(attribute) != null)
					target.append(bean.get(attribute)).append(" ");
			}
			bean.set(target_attribute, target.toString());
		}
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
