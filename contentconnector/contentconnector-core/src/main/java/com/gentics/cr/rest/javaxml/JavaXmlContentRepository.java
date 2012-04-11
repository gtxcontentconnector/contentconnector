package com.gentics.cr.rest.javaxml;

import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.gentics.cr.CRError;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.rest.ContentRepository;

/**
 *
 * Implementaion of XML representation for a REST contentrepositroy.
 *
 * 
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 545 $
 * @author $Author: supnig@constantinopel.at $
 *  
 */
public class JavaXmlContentRepository extends ContentRepository {

	private static final long serialVersionUID = 003433324L;

	/**
	 * Create instance
	 * sets response encoding to UTF-8
	 * @param attr
	 */
	public JavaXmlContentRepository(String[] attr) {

		super(attr);

		this.setResponseEncoding("UTF-8");

	}

	/**
	 * Create instance
	 * @param attr
	 * @param encoding
	 */
	public JavaXmlContentRepository(String[] attr, String encoding) {

		super(attr);

		this.setResponseEncoding(encoding);

	}

	/**
	 * Create instance
	 * @param attr
	 * @param encoding
	 * @param options
	 */
	public JavaXmlContentRepository(String[] attr, String encoding, String[] options) {

		super(attr, encoding, options);

		//this.setResponseEncoding(encoding);

	}

	/**
	 * Returns "text/xml"
	 * @return 
	 */
	public String getContentType() {
		return "text/xml";
	}

	/**
	 * Responds with Error
	 * 		Serialized CRError Class
	 * @param stream 
	 * @param ex 
	 * @param isDebug 
	 * 
	 */
	public void respondWithError(OutputStream stream, CRException ex, boolean isDebug) {

		CRError e = new CRError(ex);
		if (!isDebug) {
			e.setStringStackTrace(null);
		}

		XMLEncoder enc = new XMLEncoder(new BufferedOutputStream(stream));

		enc.writeObject(e);

		enc.close();

	}

	private void preprocessingNoByteArray(Collection<CRResolvableBean> coll) {
		Iterator<CRResolvableBean> it = coll.iterator();
		while (it.hasNext()) {
			CRResolvableBean bean = it.next();
			HashMap<String, Object> attributes = (HashMap<String, Object>) bean.getAttrMap();
			if (attributes.containsKey("binarycontent")) {
				String ccrBinUrl = "ccr_bin?contentid=" + bean.getContentid();
				attributes.remove("binarycontent");
				attributes.put("binarycontenturl", ccrBinUrl);
				bean.setAttrMap(attributes);
			}
			if (!bean.getChildRepository().isEmpty()) {
				preprocessingNoByteArray(bean.getChildRepository());
			}
		}
	}

	/**
	 * Writes Data to the specified stream
	 * @param stream 
	 * @throws CRException 
	 * 
	 */
	public void toStream(OutputStream stream) throws CRException {

		if (this.resolvableColl.isEmpty()) {
			//No Data Found
			throw new CRException("NoDataFound", "Data could not be found.", CRException.ERRORTYPE.NO_DATA_FOUND);
		} else {
			//Elements found/status ok
			XMLEncoder e = new XMLEncoder(new BufferedOutputStream(stream));
			String[] options = this.getOptionsArray();
			if (options != null) {
				ArrayList<String> optArr = new ArrayList<String>(Arrays.asList(options));

				if (optArr.contains("nobytearray")) {
					this.preprocessingNoByteArray(this.resolvableColl);
				}
			}

			e.writeObject(this.resolvableColl);
			e.close();
		}

	}

}
