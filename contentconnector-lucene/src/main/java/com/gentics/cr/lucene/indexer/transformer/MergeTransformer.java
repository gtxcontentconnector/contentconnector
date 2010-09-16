package com.gentics.cr.lucene.indexer.transformer;

import org.apache.log4j.Logger;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
/**
 * Merge one or more attributes into another attribute.
 * @author Paul Cervenka
 *
 */
public class MergeTransformer extends ContentTransformer {

	/**
	 * Configuration key for source attribute.
	 */
	private static final String TRANSFORMER_SOUCREATTRIBUTE_KEY =
		"sourceattributes";
	/**
	 * Configuration key for target attribute.
	 */
	private static final String TRANSFORMER_TARGETATTRIBUTE_KEY =
		"targetattribute";
	
	/**
	 * Array of attributes to merge.
	 */
	private String[] sourceAttributes;
	/**
	 * attribute name to store the merged attributes in.
	 */
	private String targetAttribute;
	
	/**
	 * Log4j logger for debug and error messages.
	 */
	private static Logger logger = Logger.getLogger(MergeTransformer.class);
	
	/**
	 * Creates instance of MergeTransformer.
	 * @param config configuration for the MergeTransformer
	 */
	public MergeTransformer(final GenericConfiguration config) {
		super(config);
		
		String source = (String) config.get(TRANSFORMER_SOUCREATTRIBUTE_KEY);
		targetAttribute = (String) config.get(TRANSFORMER_TARGETATTRIBUTE_KEY);
		sourceAttributes = source.split(",");
		
		if (sourceAttributes == null) {
			logger.error("Please configure " + TRANSFORMER_SOUCREATTRIBUTE_KEY
					+ " for my config.");
		}
		if (targetAttribute == null) {
			logger.error("Please configure " + TRANSFORMER_TARGETATTRIBUTE_KEY
					+ " for my config.");
		}
	}
	
	@Override
	public final void processBean(final CRResolvableBean bean) {
		StringBuffer target;
				
		if (sourceAttributes != null && targetAttribute != null) {
			target = new StringBuffer();
			for (String attribute : sourceAttributes) {
				if (bean.get(attribute) != null) {
					target.append(bean.get(attribute)).append(" ");
				}
			}
			bean.set(targetAttribute, target.toString());
		}
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
