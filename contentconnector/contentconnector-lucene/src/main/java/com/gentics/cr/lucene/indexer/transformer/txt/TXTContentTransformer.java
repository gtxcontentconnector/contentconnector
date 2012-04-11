package com.gentics.cr.lucene.indexer.transformer.txt;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;
import com.gentics.cr.util.CRUtil;

/**
 * 
 * Last changed: $Date: 2009-06-24 17:10:19 +0200 (Mi, 24 Jun 2009) $
 * @version $Revision: 99 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class TXTContentTransformer extends ContentTransformer {
	private static final String TRANSFORMER_ATTRIBUTE_KEY = "attribute";
	private String attribute = "";

	/**
	 * Get new instance of TXTContentTransformer
	 * @param config
	 */
	public TXTContentTransformer(GenericConfiguration config) {
		super(config);
		attribute = (String) config.get(TRANSFORMER_ATTRIBUTE_KEY);
	}

	/**
	 *
	 */
	private Reader getContents(Object obj) {
		ByteArrayInputStream is;
		if (obj instanceof byte[]) {
			is = new ByteArrayInputStream((byte[]) obj);
		} else {
			throw new IllegalArgumentException("Parameter must be instance of byte[]");
		}
		return new InputStreamReader(is);
	}

	/**
	 * 
	 */
	private String getStringContents(Object obj) {
		String ret = null;
		Reader r = getContents(obj);
		try {
			if (r != null) {
				ret = CRUtil.readerToString(r);
			}
			r.close();
		} catch (Exception ex) {
			//Catch all exceptions here to not disturb the indexer
			ex.printStackTrace();
		}
		return (ret);
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
