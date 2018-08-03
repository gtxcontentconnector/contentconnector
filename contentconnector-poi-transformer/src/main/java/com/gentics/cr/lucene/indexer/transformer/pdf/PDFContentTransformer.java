package com.gentics.cr.lucene.indexer.transformer.pdf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;

/**
 *
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class PDFContentTransformer extends ContentTransformer {

	private PDFTextStripper stripper = null;
	private static final String TRANSFORMER_ATTRIBUTE_KEY = "attribute";
	private String attribute = "";

	/**
	 * Get new instance of PDFContentTransformer
	 * @param config
	 */
	public PDFContentTransformer(GenericConfiguration config) {
		super(config);
		attribute = (String) config.get(TRANSFORMER_ATTRIBUTE_KEY);
	}

	/**
	 * Converts a byte array containing a pdf file to a String that can be indexed by lucene
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
		PDDocument pdfDocument = null;
		String contents = null;

		try {
			pdfDocument = PDDocument.load(is, "");
			pdfDocument.setAllSecurityToBeRemoved(true);

			//create a writer where to append the text content.
			StringWriter writer = new StringWriter();
			if (stripper == null) {
				stripper = new PDFTextStripper();
			}
			stripper.writeText(pdfDocument, writer);

			// Note: the buffer to string operation is costless;
			// the char array value of the writer buffer and the content string
			// is shared as long as the buffer content is not modified, which will
			// not occur here.
			contents = writer.getBuffer().toString();

		} catch (IOException e) {
			throw new CRException(e);
		} catch (Exception e) {
			//Catch all Exceptions happening here to not disturb the indexer
			e.printStackTrace();
		} finally {
			if (pdfDocument != null) {
				try {
					pdfDocument.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return (contents);
	}

	@Override
	public void processBean(CRResolvableBean bean) throws CRException {
		if (this.attribute != null) {
			Object obj = bean.get(this.attribute);
			if (obj != null) {
				long s = new Date().getTime();
				String newString = getStringContents(obj);
				if (newString != null) {
					bean.set(this.attribute, newString);
				}
				long e = new Date().getTime();
				LOGGER.debug("transforming object " + bean.getContentid() + " (" + bean.getMimetype() + ") took "
						+ (e - s) + "ms");
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
