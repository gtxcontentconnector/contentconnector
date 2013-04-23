package com.gentics.cr.lucene.autocomplete;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import javax.xml.transform.TransformerFactoryConfigurationError;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.rest.ContentRepository;

/**
 * 
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 545 $
 * @author $Author: andrea.schauerhuber@gmail.com $
 *
 */
public class JQueryAutocompleteRepository extends ContentRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2985183238160980550L;

	/**
	 * Create new instance.
	 * @param attr
	 */
	public JQueryAutocompleteRepository(String[] attr) {

		super(attr);

	}

	/**
	 * 
	 * @param attr
	 * @param encoding
	 */
	public JQueryAutocompleteRepository(String[] attr, String encoding) {

		super(attr);

	}

	/**
	 * 
	 * @param attr
	 * @param encoding
	 * @param options
	 */
	public JQueryAutocompleteRepository(String[] attr, String encoding, String[] options) {

		super(attr, encoding, options);

	}

	/**
	 * returns text/xml.
	 */
	public String getContentType() {
		return "text/plain";
	}

	/**
	 * Respond with Error.
	 * @param stream 
	 * @param ex 
	 * @param isDebug 
	 */
	public void respondWithError(OutputStream stream, CRException ex, boolean isDebug) {

		try {

			OutputStreamWriter wr = new OutputStreamWriter(stream, this.getResponseEncoding());
			wr.write("ERROR");
			wr.close();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Write XML Elements to the specified stream.
	 * @param stream 
	 * @throws CRException 
	 */
	public void toStream(OutputStream stream) throws CRException {

		try {
			OutputStreamWriter wr = new OutputStreamWriter(stream, this.getResponseEncoding());
			String nl = System.getProperty("line.separator");
			wr.write("[");
			for (Iterator<CRResolvableBean> it = this.resolvableColl.iterator(); it.hasNext();) {
				CRResolvableBean bean = it.next();
				if (it.hasNext()) {
					wr.write("{\"id\":\"" + bean.get(Autocompleter.SOURCE_WORD_FIELD) + "\", \"label\":\""
							+ bean.get(Autocompleter.SOURCE_WORD_FIELD) + "\", \"value\":\""
							+ bean.get(Autocompleter.SOURCE_WORD_FIELD) + "\"}," + nl);
				} else {
					wr.write("{\"id\":\"" + bean.get(Autocompleter.SOURCE_WORD_FIELD) + "\", \"label\":\""
							+ bean.get(Autocompleter.SOURCE_WORD_FIELD) + "\", \"value\":\""
							+ bean.get(Autocompleter.SOURCE_WORD_FIELD) + "\"}");
				}
			}

			wr.write("]");
			wr.flush();
			wr.close();

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
