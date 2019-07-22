package com.gentics.cr.lucene.indexer.transformer.multivaluestring;

import java.util.Collection;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;

/**
 * 
 * Last changed: $Date: 2009-06-26 15:48:16 +0200 (Fr, 26 Jun 2009) $
 * @version $Revision: 105 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class SimpleMVString extends ContentTransformer {

	private String NULLValue = "NULL";
	private static final String NULL_VALUE_KEY = "nullvalue";
	private static final String DEFAULT_NULL_VALUE = "NULL";
	private static final String TRANSFORMER_ATTRIBUTE_KEY = "attribute";
	private static final String DELIMITER_KEY = "delimiter";
	private static final String DEFAULT_DELIMITER_VALUE = " ";
	private String attribute = "";
	private String delimiter = " ";

	/**
	 * Create new Instance of SimpleMVString
	 * @param config
	 */
	public SimpleMVString(GenericConfiguration config) {
		super(config);
		//Define Value for NULLS
		NULLValue = (String) config.get(NULL_VALUE_KEY);
		if (NULLValue == null) {
			NULLValue = DEFAULT_NULL_VALUE;
		}
		delimiter = (String) config.get(DELIMITER_KEY);
		if (delimiter == null) {
			delimiter = DEFAULT_DELIMITER_VALUE;
		}
		attribute = (String) config.get(TRANSFORMER_ATTRIBUTE_KEY);
	}

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	private String getStringContents(Object obj) {
		StringBuilder ret = new StringBuilder();
		if (obj != null && obj instanceof Collection) {
			Collection<Object> coll = (Collection<Object>) obj;
			if (coll == null || coll.size() <= 0) {
				return NULLValue;
			}
			for (Object object : coll) {
				if (object != null) {
					ret.append(object.toString() + delimiter);
				}
			}
		}
		if ("".equals(ret.toString())) {
			return NULLValue;
		} else {
			return ret.toString();
		}
	}

	@Override
	public void processBean(CRResolvableBean bean) {
		if (bean != null && this.attribute != null) {
			Object obj = bean.get(this.attribute);
			String newString = getStringContents(obj);
			if (newString != null) {
				bean.set(this.attribute, newString);
			}			
		} else {
			LOGGER.error("Configured attribute or bean is null. Bean will not be processed");
		}

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
