package com.gentics.cr.lucene.indexer.transformer.doc;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;

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
public class RTFContentTransformer extends ContentTransformer {
	private static final String TRANSFORMER_ATTRIBUTE_KEY = "attribute";
	private String attribute = "";

	/**
	 * Get new instance of DOCContentTransformer
	 * @param config
	 */
	public RTFContentTransformer(GenericConfiguration config) {
		super(config);
		attribute = (String) config.get(TRANSFORMER_ATTRIBUTE_KEY);
	}

	/**
	 * Converts a byte array that contains a word file into a string with its contents
	 * @param obj
	 * @return
	 */
	private String getStringContents(Object obj) throws CRException {
		ByteArrayInputStream is;
		if (obj instanceof byte[]) {
			is = new ByteArrayInputStream((byte[]) obj);
		} else {
			throw new IllegalArgumentException(
					"Parameter must be instance of byte[]");
		}
		String ret = null;
		//creating a default blank styled document
		DefaultStyledDocument styledDoc = new DefaultStyledDocument();

		//Creating a RTF Editor kit
		RTFEditorKit rtfKit = new RTFEditorKit();

		//Populating the contents in the blank styled document
		try {
			rtfKit.read(is, styledDoc, 0);

			// Getting the root document
			Document doc = styledDoc.getDefaultRootElement().getDocument();

			//Printing out the contents of the RTF document as plain text
			ret = doc.getText(0, doc.getLength());
		} catch (IOException e) {
			throw new CRException(e);
		} catch (BadLocationException e) {
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
			log
					.error("Configured attribute is null. Bean will not be processed");
		}
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}
}
