package com.gentics.cr.rest.php;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.exceptions.CRException.ERRORTYPE;
import com.gentics.cr.rest.ContentRepository;

/** 
 * Implementaion of Json rappresentation for a REST contentrepositroy.
 *
 * 
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 545 $
 * @author $Author: supnig@constantinopel.at $
 *
 */

public class PHPContentRepository extends ContentRepository {

	/**
	 * Serial ID.
	 */
	private static final long serialVersionUID = -4131893655763487202L;

	private PHPSerializer PHPSerializer;

	private Map<String, Object> cr;

	/**
	 * Create new Instance.
	 * 	Set response encoding to UTF-8
	 * @param attr
	 */
	public PHPContentRepository(final String[] attr) {
		super(attr);
		this.setResponseEncoding("UTF-8");
		PHPSerializer = new PHPSerializer(this.getResponseEncoding());
		cr = new LinkedHashMap<String, Object>();
	}

	/**
	 * Create new Instance.
	 * @param attr
	 * @param encoding
	 */
	public PHPContentRepository(String[] attr, String encoding) {

		super(attr);
		this.setResponseEncoding(encoding);
		PHPSerializer = new PHPSerializer(this.getResponseEncoding());
		cr = new LinkedHashMap<String, Object>();
	}

	/**
	 * Create new instace.
	 * @param attr
	 * @param encoding
	 * @param options
	 */
	public PHPContentRepository(String[] attr, String encoding, String[] options) {
		super(attr, encoding, options);
		PHPSerializer = new PHPSerializer(this.getResponseEncoding());
		cr = new LinkedHashMap<String, Object>();
	}

	/**
	 * returns "application/serialized_PHP_variable".
	 */
	public String getContentType() {
		return "application/serialized_PHP_variable";
	}

	/**
	 * Responts with an PHP Serialized ERROR Object.
	 * @param stream 
	 * @param ex 
	 * @param isDebug 
	 */
	public void respondWithError(final OutputStream stream, final CRException ex, final boolean isDebug) {
		this.cr.clear();
		this.cr.put("status", "error");

		Map<String, Object> error = new LinkedHashMap<String, Object>();
		error.put("type", ex.getType());
		error.put("message", ex.getMessage());

		if (isDebug) {
			error.put("stacktrace", ex.getStringStackTrace());
		}

		this.cr.put("Error", error);

		try {
			stream.write(this.PHPSerializer.serialize(this.cr).getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Writes the CRResolvableBeans to a Stream.
	 * @param stream 
	 * @throws CRException 
	 * 
	 */
	public void toStream(final OutputStream stream) throws CRException {
		if (this.resolvableColl.isEmpty()) {
			//No Data Found

			throw new CRException("NoDataFound", "Data could not be found.", ERRORTYPE.NO_DATA_FOUND);
		} else {
			this.cr.put("status", "ok");
			for (Iterator<CRResolvableBean> it = this.resolvableColl.iterator(); it.hasNext();) {
				CRResolvableBean crBean = it.next();
				Map<String, Object> objElement = processElement(crBean);
				this.cr.put(crBean.getContentid(), objElement);
			}
		}

		try {
			OutputStreamWriter wr = new OutputStreamWriter(stream, this.getResponseEncoding());
			wr.write(this.PHPSerializer.serialize(this.cr));
			wr.flush();
			wr.close();

		} catch (IOException e) {
			throw new CRException(e);
		}

	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> processElement(final CRResolvableBean crBean) {
		Map<String, Object> objElement = new LinkedHashMap<String, Object>();

		objElement.put("contentid", "" + crBean.getContentid());
		objElement.put("obj_id", "" + crBean.getObj_id());
		objElement.put("obj_type", "" + crBean.getObj_type());
		objElement.put("mother_id", crBean.getMother_id() == null ? "" : "" + crBean.getMother_id());
		objElement.put("mother_type", crBean.getMother_type() == null ? "" : "" + crBean.getMother_type());

		if (crBean.getAttrMap() != null && !crBean.getAttrMap().isEmpty()) {
			ConcurrentHashMap<String, Object> attribContainer = new ConcurrentHashMap<String, Object>();
			Iterator<String> bit = crBean.getAttrMap().keySet().iterator();
			while (bit.hasNext()) {
				String entry = bit.next();
				Object bValue = crBean.getAttrMap().get(entry);

				if (bValue != null) {
					if (!entry.equals("binarycontent") && (bValue.getClass().isArray() || bValue.getClass() == ArrayList.class)) {
						Object[] value;
						if (bValue.getClass() == ArrayList.class) {
							value = ((ArrayList<Object>) bValue).toArray();
						} else {
							value = (Object[]) bValue;
						}

						attribContainer.put(entry, value);
					} else {
						Object value = "";
						if (entry.equals("binarycontent")) {
							try {
								value = new String((byte[]) bValue);
							} catch (ClassCastException x) {
								try {
									value = new String(getBytes(bValue));
								} catch (IOException e) {
									value = bValue.toString();
									e.printStackTrace();
								}
							}
						} else {
							if (bValue instanceof String) {
								value = (String) bValue;
							} else if (bValue instanceof Map<?, ?>) {
								value = bValue;
							} else {
								value = bValue.toString();
							}
						}
						attribContainer.put(entry, value);
					}
				}
			}
			objElement.put("attributes", attribContainer);
		}
		if (crBean.hasChildren()) {
			Map<String, Map<String, Object>> childContainer = new LinkedHashMap<String, Map<String, Object>>();

			for (Iterator<CRResolvableBean> it = crBean.getChildRepository().iterator(); it.hasNext();) {
				CRResolvableBean chBean = it.next();
				Map<String, Object> chElement = processElement(chBean);
				childContainer.put(chBean.getContentid(), chElement);
			}

			objElement.put("children", childContainer);
		}
		return objElement;
	}
}
