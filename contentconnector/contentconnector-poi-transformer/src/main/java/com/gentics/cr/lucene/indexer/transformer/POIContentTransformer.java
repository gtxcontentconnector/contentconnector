package com.gentics.cr.lucene.indexer.transformer;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.poi.POITextExtractor;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.xmlbeans.XmlException;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.exceptions.CRException;

/**
 * Transformer for all types of POI documents (Microsoft OLE2 documents) which automatically 
 * figures out how to parse the document.
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class POIContentTransformer extends ContentTransformer {

	/**
	 * Config key to set the attribute to use for parsing.
	 */
	private static final String TRANSFORMER_ATTRIBUTE_KEY = "attribute";

	/**
	 * Field to store the config value representing the attribute to use for parsing.
	 */
	private String attribute = "";

	/**
	 * Get new instance of POIContentTransformer.
	 * @param config Configuration for the transformer.
	 */
	public POIContentTransformer(final GenericConfiguration config) {
		super(config);
		attribute = (String) config.get(TRANSFORMER_ATTRIBUTE_KEY);
	}

	/**
	 * Converts a byte array that contains a word file into a string with its contents.
	 * @param obj Object to retrieve the content from.
	 * @return parsed text from the object
	 * @throws CRException 
	 */
	private String getStringContents(final Object obj) throws CRException {
		ByteArrayInputStream is;
		if (obj instanceof byte[]) {
			is = new ByteArrayInputStream((byte[]) obj);
		} else if (obj instanceof String) {
			String str = (String) obj;
			is = new ByteArrayInputStream(str.getBytes());
		} else {
			throw new IllegalArgumentException("Parameter must be instance of byte[]");
		}
		String ret = null;
		POITextExtractor extractor;

		try {
			extractor = ExtractorFactory.createExtractor(is);

			ret = extractor.getText();

		} catch (IOException e) {
			throw new CRException(e);
		} catch (InvalidFormatException e) {
			throw new CRException(e);
		} catch (OpenXML4JException e) {
			throw new CRException(e);
		} catch (XmlException e) {
			throw new CRException(e);
		}
		return (ret);
	}

	@Override
	public void processBean(final CRResolvableBean bean) throws CRException {
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
		// TODO Auto-generated method stub

	}
}
