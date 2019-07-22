package com.gentics.cr.lucene.indexer.transformer.doc;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.poi.hwpf.OldWordFileFormatException;
import org.apache.poi.hwpf.extractor.Word6Extractor;
import org.apache.poi.hwpf.extractor.WordExtractor;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;

/**
 * Contenttransformer that converts MS Word files into text using
 * the Paragraph API of the Apache POI package.
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class DOCParagraphContentTransformer extends ContentTransformer {
	private static final String TRANSFORMER_ATTRIBUTE_KEY = "attribute";
	private String attribute = "";

	/**
	 * Get new instance of DOCParagraphContentTransformer.
	 * @param config
	 */
	public DOCParagraphContentTransformer(GenericConfiguration config) {
		super(config);
		attribute = (String) config.get(TRANSFORMER_ATTRIBUTE_KEY);
	}

	/**
	 * Converts a byte array that contains a word file into a string with its contents.
	 * @param obj
	 * @return
	 */
	private String getStringContents(Object obj) throws CRException {
		ByteArrayInputStream is;
		if (obj instanceof byte[]) {
			is = new ByteArrayInputStream((byte[]) obj);
		} else {
			throw new IllegalArgumentException("Parameter must be instance of byte[]");
		}
		String ret = null;
		try {
			WordExtractor docextractor = new WordExtractor(is);
			String[] paragraphs = docextractor.getParagraphText();
			if (paragraphs != null) {
				StringBuilder builder = new StringBuilder();
				for (String p : paragraphs) {
					builder.append(WordExtractor.stripFields(p));
				}
				ret = builder.toString();
			}
		} catch (OldWordFileFormatException e) {
			try {
				is.reset();
				Word6Extractor docextractor = new Word6Extractor(is);
				ret = docextractor.getText();
			} catch (IOException e1) {
				throw new CRException(e1);
			}

		} catch (IOException e) {
			throw new CRException(e);
		}
		return (ret);
	}

	@Override
	public void processBean(CRResolvableBean bean) throws CRException {
		if (this.attribute != null) {
			Object obj = bean.get(this.attribute);
			if (obj != null) {
				String newString = getStringContents(obj);
				if (newString != null) {
					bean.set(this.attribute, newString);
				}
			}
		} else {
			LOGGER.error("Configured attribute is null. Bean will not be processed");
		}
	}

	@Override
	public void destroy() {

	}
}
