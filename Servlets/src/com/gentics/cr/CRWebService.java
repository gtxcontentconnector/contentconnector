package com.gentics.cr;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

import com.gentics.cr.rest.ContentRepository;
import com.gentics.cr.rest.json.JSONContentRepository;



public class CRWebService {

	
	public void init()
	{
		WSRPContainer.getRP();
	}
	
	public String getObjects(String rule, String[] attributes)
	{
		RequestProcessor rp = WSRPContainer.getRP();
		CRRequest req = new CRRequest();
		req.setRequestFilter(rule);
		req.setAttributeArray(attributes);
		try {
			Collection<CRResolvableBean> col = rp.getObjects(req);
			ContentRepository cr = new JSONContentRepository(attributes);
			cr.addObjects(col);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			cr.toStream(baos);
			return baos.toString(req.getEncoding());
		} catch (CRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return("ERROR");
	}
}
