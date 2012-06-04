package com.gentics.cr.rest.json;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.rest.ContentRepository;

/**
 * Implementaion of Json rappresentation for a REST contentrepositroy.
 * 
 * 
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * 
 * @version $Revision: 545 $
 * @author $Author: supnig@constantinopel.at $
 * 
 */
public class JSONContentRepository extends ContentRepository {

	private static final long serialVersionUID = 0003L;

	private JSONObject rootObject;

	/**
	 * Create instance Set response encoding to UTF-8.
	 * 
	 * @param attr
	 */
	public JSONContentRepository(String[] attr) {

		super(attr);
		this.setResponseEncoding("UTF-8");
		rootObject = new JSONObject();
	}

	/**
	 * Create instance.
	 * 
	 * @param attr
	 * @param encoding
	 */
	public JSONContentRepository(String[] attr, String encoding) {

		super(attr);
		this.setResponseEncoding(encoding);
		rootObject = new JSONObject();
	}

	/**
	 * Create instance.
	 * 
	 * @param attr
	 * @param encoding
	 * @param options
	 */
	public JSONContentRepository(String[] attr, String encoding, String[] options) {

		super(attr, encoding, options);
		// this.setResponseEncoding(encoding);
		rootObject = new JSONObject();
	}

	/**
	 * @return "application/jsonrequest"
	 */
	public String getContentType() {
		// return "text/javascript";
		return ("application/jsonrequest");
	}

	/**
	 * Responds with a JSON ERROR Object.
	 * 
	 * @param stream
	 * @param ex
	 * @param isDebug
	 * 
	 */
	public void respondWithError(OutputStream stream, CRException ex, boolean isDebug) {
		// Clear the root element and set the status
		this.rootObject.clear();
		this.rootObject.element("status", "error");
		JSONObject errElement = new JSONObject();

		errElement.element("type", ex.getType());
		errElement.element("message", ex.getMessage());
		if (isDebug) {
			// if in debug mode, add stack trace
			errElement.element("stacktrace", ex.getStringStackTrace());
		}
		this.rootObject.element("Error", errElement);

		// Write stream
		try {
			stream.write(this.rootObject.toString().getBytes());
		} catch (IOException e) {
			;
		}

	}

	/**
	 * Writes the objects as JSON Stream.
	 * 
	 * @param stream
	 * @throws CRException
	 * 
	 */
	public void toStream(OutputStream stream) throws CRException {
		if (this.resolvableColl.isEmpty()) {
			// No Data Found
			throw new CRException("NoDataFound", "Data could not be found.");
		} else {
			this.rootObject.element("status", "ok");

			JSONArray jsArray = new JSONArray();

			for (Iterator<CRResolvableBean> it = this.resolvableColl.iterator(); it.hasNext();) {

				CRResolvableBean crBean = it.next();

				JSONObject objElement = processElement(crBean);

				jsArray.add(objElement);

			}
			this.rootObject.element("Objects", jsArray);
		}

		try {
			// use JSONObject.write instead toString is not a good solution
			OutputStreamWriter wr = new OutputStreamWriter(stream, this.getResponseEncoding());

			this.rootObject.write(wr);
			wr.flush();
			wr.close();
		} catch (IOException ioex) {
			;
		}

	}

	@SuppressWarnings("unchecked")
	private JSONObject processElement(CRResolvableBean crBean) {
		JSONObject objElement = new JSONObject();

		objElement.element("contentid", "" + crBean.getContentid());
		objElement.element("obj_id", "" + crBean.getObj_id());
		objElement.element("obj_type", "" + crBean.getObj_type());
		objElement.element("mother_id", ((crBean.getMother_id() == null) ? "" : "" + crBean.getMother_id()));
		objElement.element("mother_type", ((crBean.getMother_type() == null) ? "" : "" + crBean.getMother_type()));

		if (crBean.getAttrMap() != null && (!crBean.getAttrMap().isEmpty())) {
			JSONObject attrContainer = new JSONObject();

			Iterator<String> bit = crBean.getAttrMap().keySet().iterator();
			while (bit.hasNext()) {

				String entry = bit.next();
				// Element attrElement = doc.createElement(entry);
				Object bValue = crBean.getAttrMap().get(entry);

				if (bValue != null) {
					// deal with multivalue attributes
					if ((bValue.getClass().isArray() && !(bValue instanceof byte[])) || bValue instanceof List) {
						JSONArray value = new JSONArray();
						List<Object> arr;
						if (bValue instanceof List) {
							arr = new ArrayList<Object>();
							List<Object> ob_arr = (List<Object>) bValue;
							for (Object obj : ob_arr) {
								String v = "";
								v = convertToString(obj);
								arr.add(v);
							}
						} else {
							arr = new ArrayList<Object>();
							Object[] ob_arr = (Object[]) bValue;
							for (Object obj : ob_arr) {
								String v = "";
								v = convertToString(obj);
								arr.add(v);
							}
						}
						value.addAll(arr);
						attrContainer.element(entry, value);
					} else if (bValue instanceof Map<?, ?>) {
						attrContainer.element(entry, bValue);
					}
					// dealt with single value attributes (String, Number,
					// ByteArray)
					else {
						String value = "";
						value = convertToString(bValue);
						attrContainer.element(entry, value);
					}
				}
			}
			objElement.element("attributes", attrContainer);
		}
		if (crBean.getChildRepository() != null && crBean.getChildRepository().size() > 0) {
			// JSONObject childContainer = new JSONObject();
			JSONArray childContainer = new JSONArray();
			for (Iterator it = crBean.getChildRepository().iterator(); it.hasNext();) {

				CRResolvableBean chBean = (CRResolvableBean) it.next();

				JSONObject chElement = processElement(chBean);
				childContainer.add(chElement);
				// childContainer.element(chBean.getContentid(),chElement);
			}
			objElement.element("children", childContainer);
		}
		return objElement;
	}

	/**
	 * 
	 * @param bValue
	 * @return
	 */
	private String convertToString(Object bValue) {
		String value;
		if (bValue instanceof String) {
			value = (String) bValue;
		} else if (bValue instanceof Number) {
			value = bValue.toString();
		} else if (bValue instanceof Date) {
			value = Long.toString(((Date) bValue).getTime());
		} else {
			byte[] bs = (byte[]) bValue;
			value = new String(bs);

		}
		return value;
	}

}
