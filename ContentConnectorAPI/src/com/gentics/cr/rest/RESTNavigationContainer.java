package com.gentics.cr.rest;

import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRException;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRRequestProcessor;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.util.CRNavigationRequestBuilder;
import com.gentics.cr.util.response.IResponseTypeSetter;

public class RESTNavigationContainer{

	public CRRequestProcessor rp;
	public String response_encoding;
	private String contenttype="";
	private Logger log;
	
	public RESTNavigationContainer(CRConfigUtil crConf)
	{
		this.log = Logger.getLogger(this.getClass().getName());
		this.response_encoding = crConf.getEncoding();
		
		try {
			this.rp = new CRRequestProcessor(crConf.getRequestProcessorConfig("1"));
		} catch (CRException e) {
			CRException ex = new CRException(e);
			this.log.error("FAILED TO INITIALIZE REQUEST PROCESSOR... "+ex.getStringStackTrace());
		}
	}
	
	public String getContentType()
	{
		return(this.contenttype+"; charset="+this.response_encoding);
	}

	public void processService(CRNavigationRequestBuilder reqBuilder,Map<String, Resolvable> wrappedObjectsToDeploy, OutputStream stream, IResponseTypeSetter responsetypesetter) {
		Collection<CRResolvableBean> coll;
		CRNavigationRequestBuilder myReqBuilder = reqBuilder;
		ContentRepository cr = null;
		try {
			cr = myReqBuilder.getContentRepository(this.response_encoding);
			this.contenttype = cr.getContentType();
			responsetypesetter.setContentType(this.getContentType());
			CRRequest req = myReqBuilder.getNavigationRequest();
			//DEPLOY OBJECTS TO REQUEST
			for (Iterator<Map.Entry<String, Resolvable>> i = wrappedObjectsToDeploy.entrySet().iterator() ; i.hasNext() ; ) {
				Map.Entry<String,Resolvable> entry = (Entry<String,Resolvable>) i.next();
				req.addObjectForFilterDeployment((String)entry.getKey(), entry.getValue());
			}
			// Query the Objects from RequestProcessor
			coll = rp.getNavigation(req);
			
			// add the objects to repository as serializeable beans
			if (coll != null) {
				for (Iterator<CRResolvableBean> it = coll.iterator(); it.hasNext();) {
					cr.addObject(it.next());
				}
			}
			cr.toStream(stream);
		} catch (CRException e1) {
			//CR Error Handling
			//CRException is passed down from methods that want to post 
			//the occured error to the client
			cr.respondWithError((OutputStream) stream,e1,myReqBuilder.isDebug());
			this.log.debug(e1.getMessage()+" : "+e1.getStringStackTrace());
		}
		catch(Exception ex)
		{
			CRException crex = new CRException(ex);
			cr.respondWithError((OutputStream) stream,crex,myReqBuilder.isDebug());
			this.log.debug(ex.getMessage()+" : "+crex.getStringStackTrace());
		}
		
	}
	
	

}
