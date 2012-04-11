package com.gentics.cr.lucene.indexer.transformer.ppt;

import java.io.ByteArrayInputStream;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;

/**
 * 
 * Last changed: $Date: 2009-06-24 17:10:19 +0200 (Mi, 24 Jun 2009) $
 * @version $Revision: 99 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class PPTContentTransformer extends ContentTransformer {
	private static final String TRANSFORMER_ATTRIBUTE_KEY = "attribute";
	private String attribute = "";

	/**
	 * Get new instance of PPTContentTransformer
	 * @param config
	 */
	public PPTContentTransformer(GenericConfiguration config) {
		super(config);
		attribute = (String) config.get(TRANSFORMER_ATTRIBUTE_KEY);
	}

	/**
	 * Converts a byte array containing a ppt file to a String that can be indexed by lucene
	 * @param obj
	 * @return
	 */
	private String getStringContents(Object obj) {
		ByteArrayInputStream is;
		if (obj instanceof byte[]) {
			is = new ByteArrayInputStream((byte[]) obj);
		} else {
			throw new IllegalArgumentException("Parameter must be instance of byte[]");
		}
		MSPowerPointParser parser = new MSPowerPointParser(is);
		String contents = parser.getContents();
		return (contents);
	}

	@Override
	public void processBean(CRResolvableBean bean) {
		if (this.attribute != null) {
			Object obj = bean.get(this.attribute);
			if (obj != null) {
				String newString = getStringContents(obj);
				if (newString != null) {
					bean.set(this.attribute, newString);
				}
			}
		} else {
			log.error("Configured attribute is null. Bean will not be processed");
		}
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}
}
