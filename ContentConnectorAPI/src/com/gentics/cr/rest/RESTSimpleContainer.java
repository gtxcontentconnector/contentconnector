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
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.util.CRRequestBuilder;
import com.gentics.cr.util.response.IResponseTypeSetter;
/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class RESTSimpleContainer{

	public RequestProcessor rp;
	public String response_encoding;
	private String contenttype="";
	private static Logger log = Logger.getLogger(RESTSimpleContainer.class);
	
	public String getContentType()
	{
		return(this.contenttype+"; charset="+this.response_encoding);
	}
	
	public RESTSimpleContainer(CRConfigUtil crConf)
	{
		this.response_encoding = crConf.getEncoding();
		
		try {
			
			this.rp = crConf.getNewRequestProcessorInstance(1);
		} catch (CRException e) {
			
			log.error("FAILED TO INITIALIZE REQUEST PROCESSOR... "+e.getStringStackTrace());
		}
	}
	
	public void processService(CRRequestBuilder reqBuilder, Map<String,Resolvable> wrappedObjectsToDeploy, OutputStream stream, IResponseTypeSetter responsetypesetter)
	{
		Collection<CRResolvableBean> coll;
		CRRequestBuilder myReqBuilder = reqBuilder;
		ContentRepository cr = null;
		try {
			cr = myReqBuilder.getContentRepository(this.response_encoding);
			this.contenttype = cr.getContentType();
			if (responsetypesetter != null) {
				responsetypesetter.setContentType(this.getContentType());
			}
			CRRequest req = myReqBuilder.getCRRequest();
			//DEPLOY OBJECTS TO REQUEST
			for (Iterator<Map.Entry<String, Resolvable>> i = wrappedObjectsToDeploy.entrySet().iterator() ; i.hasNext() ; ) {
				Map.Entry<String,Resolvable> entry = (Entry<String,Resolvable>) i.next();
				req.addObjectForFilterDeployment((String)entry.getKey(), entry.getValue());
			}
			// Query the Objects from RequestProcessor
			coll = rp.getObjects(req);
			
			// add the objects to repository as serializeable beans
			if (coll != null) {
				for (Iterator<CRResolvableBean> it = coll.iterator(); it.hasNext();) {
					cr.addObject(it.next());
				}
			}
			cr.toStream(stream);
			stream.flush();
			stream.close();
		} catch (CRException e1) {
			//CR Error Handling
			//CRException is passed down from methods that want to post 
			//the occured error to the client
			cr.respondWithError((OutputStream) stream,e1,myReqBuilder.isDebug());
			log.debug(e1.getMessage()+" : "+e1.getStringStackTrace());
			
		}
		catch(Exception ex)
		{
			CRException crex = new CRException(ex);
			cr.respondWithError((OutputStream) stream,crex,myReqBuilder.isDebug());
			log.debug(ex.getMessage()+" : "+crex.getStringStackTrace());
			
		}
	}
}
