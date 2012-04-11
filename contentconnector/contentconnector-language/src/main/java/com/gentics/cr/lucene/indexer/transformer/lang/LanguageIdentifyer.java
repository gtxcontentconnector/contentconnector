package com.gentics.cr.lucene.indexer.transformer.lang;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.analytics.language.LanguageGuesser;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;

/**
 * 
 * Last changed: $Date: 2009-09-02 17:57:48 +0200 (Mi, 02 Sep 2009) $
 * @version $Revision: 180 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class LanguageIdentifyer extends ContentTransformer {

	private static final String TRANSFORMER_ATTRIBUTE_KEY = "attribute";
	private static final String LANGUAGE_ATTRIBUTE_KEY = "langattribute";
	private String attribute;
	private String langattribute;

	/**
	 * Create new instance of LanguageIdentifyer
	 * @param config
	 */
	public LanguageIdentifyer(GenericConfiguration config) {
		super(config);
		attribute = (String) config.get(TRANSFORMER_ATTRIBUTE_KEY);
		langattribute = (String) config.get(LANGUAGE_ATTRIBUTE_KEY);
	}

	@Override
	public void processBean(CRResolvableBean bean) {
		Object att = bean.get(attribute);
		if (att != null && langattribute != null) {
			String lang = null;
			if (att instanceof String) {
				lang = getLangFromString((String) att);
			} else if (att instanceof byte[]) {
				lang = getLangFromBinary((byte[]) att);
			}
			if (lang == null)
				lang = "NULL";
			bean.set(langattribute, lang);
		}
	}

	private String getLangFromBinary(byte[] binary) {
		String lang = null;
		ByteArrayInputStream is = new ByteArrayInputStream(binary);
		try {
			lang = LanguageGuesser.detectLanguage(is);
		} catch (IOException iox) {
			iox.printStackTrace();
		}
		return lang;
	}

	private String getLangFromString(String string) {
		return LanguageGuesser.detectLanguage(string);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
