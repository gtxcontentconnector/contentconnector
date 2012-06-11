package com.gentics.cr.lucene.indexer.transformer.tika;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;

/**
 * Transformer to automatically parse various filetypes using autodetect of the mimetype.
 * Uses the Tika framework internally for detection and parsing.
 * Tika uses Apache POI and pdfbox.
 * 
 * @author Friedreich Bernhard
 */
public class TikaParserTransformer extends ContentTransformer {

	/**
	 * Config key to set the attribute to use for parsing.
	 */
	private static final String TRANSFORMER_ATTRIBUTE_KEY = "attribute";

	/**
	 * Field to store the config value representing the attribute to use for parsing.
	 */
	private String attribute = "";

	/**
	 * Parser provided by Tika to automatically detect the mimetype and afterwards do the parsing.
	 */
	private final AutoDetectParser parser = new AutoDetectParser();

	/**
	 * Get new instance of TikaParserTransformer.
	 * @param config Configuration for the transformer.
	 */
	public TikaParserTransformer(final GenericConfiguration config) {
		super(config);
		attribute = (String) config.get(TRANSFORMER_ATTRIBUTE_KEY);
	}

	@Override
	public void processBean(final CRResolvableBean bean) throws CRException {
		if (this.attribute != null) {
			Object obj = bean.get(this.attribute);
			if (obj != null) {
				ByteArrayInputStream is;
				if (obj instanceof byte[]) {
					is = new ByteArrayInputStream((byte[]) obj);
				} else {
					throw new IllegalArgumentException("Parameter must be instance of byte[]");
				}

				ContentHandler textHandler = new BodyContentHandler();
				Metadata metadata = new Metadata();

				try {
					parser.parse(is, textHandler, metadata);
				} catch (IOException e) {
					log.error("ioexception", e);
				} catch (SAXException e) {
					log.error("saxexception", e);
				} catch (TikaException e) {
					log.error("tikaexception", e);
				}

				bean.set("title", metadata.get(Metadata.TITLE));
				bean.set("author", metadata.get(Metadata.AUTHOR));
				bean.set(this.attribute, textHandler.toString());
			}
		} else {
			log.error("Configured attribute is null. Bean will not be processed");
		}
	}

	@Override
	public void destroy() {

	}
}
