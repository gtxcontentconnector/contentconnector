package com.gentics.cr.rest.json;

import java.util.Collection;
import java.util.Vector;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.exceptions.CRException;

/**
 * Request Processor to create simple structures via json.<br />
 * Needs a root json object that has an array of other objects in his object attribute:<br />
 * {objects:[<br />
 *   { name: "object1" },<br />
 *   { name: "object2" }<br />
 * ]}<br />
 * @author bigbear3001
 */
public class JSONRequestProcessor extends RequestProcessor {

	/**
	 * Configuration of the request processor.
	 */
	CRConfig conf;

	/**
	 * Objects initialized from the json string.
	 */
	Collection<CRResolvableBean> objects;

	/**
	 * initialize a new {@link JSONRequestProcessor}
	 * @param config - configuration of the request processor.
	 * @throws CRException - if the config wasn't valid.
	 */
	public JSONRequestProcessor(CRConfig config) throws CRException {
		super(config);
		conf = config;
	}

	@Override
	public void finalize() {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<CRResolvableBean> getObjects(CRRequest request,
			boolean doNavigation) throws CRException {
		initObjects();
		return objects;
	}

	/**
	 * init the json string and convert it into {@link CRResolvableBean}s.
	 */
	private synchronized void initObjects() {
		if (objects == null) {
			objects = new Vector<CRResolvableBean>();
			String objectString = config.getString("objects", "[]");
			JSONObject json = JSONObject.fromObject(objectString);
			JSONArray jsonObjects = json.getJSONArray("objects");
			for(Object object : jsonObjects) {
				if(object instanceof JSONObject) {
					objects.add(createCRResolvableBean((JSONObject) object));
				}
			}
		}
	}

	/**
	 * convert a JSONObject into a {@link CRResolvableBean}
	 * @param object - json object to convert
	 * @return the resolvable representing the json object.
	 */
	private CRResolvableBean createCRResolvableBean(JSONObject object) {
		if(object != null) {
			CRResolvableBean bean = new CRResolvableBean();
			for(Object key : object.keySet()) {
				Object value = object.get(key);
				if(value instanceof JSONObject) {
					bean.set(key.toString(), createCRResolvableBean((JSONObject) value));
				} else if(value instanceof JSONArray) {
					Collection<Object> array = new Vector<Object>(((JSONArray) value).size());
					for(Object arrayObject : (JSONArray) value) {
						if(arrayObject instanceof JSONObject) {
							array.add(createCRResolvableBean((JSONObject) arrayObject));
						} else {
							array.add(arrayObject);
						}
					}
					bean.set(key.toString(), array);
				} else {
					bean.set(key.toString(), value);
				}
			}
			return bean;
		}
		return null;
	}

}
