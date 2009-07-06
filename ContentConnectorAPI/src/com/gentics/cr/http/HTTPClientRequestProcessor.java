package com.gentics.cr.http;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRError;
import com.gentics.cr.CRException;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.util.CRUtil;
/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class HTTPClientRequestProcessor extends RequestProcessor {

	private static Logger log = Logger.getLogger(HTTPClientRequestProcessor.class);
	protected String name=null;
	
	private static final String URL_KEY = "URL";
	
	private String path="";
	
	protected HttpClient client;
	
	/**
	 * Create new instance of HTTPClientRequestProcessor
	 * @param config
	 * @throws CRException
	 */
	public HTTPClientRequestProcessor(CRConfig config) throws CRException {
		super(config);
		this.name=config.getName();
		//LOAD ADDITIONAL CONFIG
		client = new HttpClient();
		this.path = (String)config.get(URL_KEY);
		if(this.path==null)log.error("COULD NOT GET URL FROM CONFIG (add RP.<rpnumber>.url=<url> to config). OVERTHINK YOUR CONFIG!");
	}
	
		
	/**
	 * Requests Objects from a remote ContentConnector Servlet using type JavaXML
	 * @param request
	 * @param doNavigation
	 * @return Collection of CRResolvableBean
	 * @throws CRException
	 */
	public Collection<CRResolvableBean> getObjects(CRRequest request, boolean doNavigation) throws CRException {
			ArrayList<CRResolvableBean> resultlist = new ArrayList<CRResolvableBean>();
		 	
			String filter = "filter="+request.getRequestFilter();
			
			String attributesstring = "";
			for(String att:request.getAttributeArray())
			{
				attributesstring+="&attributes="+att;
			}
			
			String countstring="";
			if(request.getCountString()!=null && !request.getCountString().equals(""))
			{
				countstring="&count="+request.getCountString();
			}
			
			String sortstring="";
			if(request.getSortArray()!=null && request.getSortArray().length>0)
			{
				for(String sort:request.getSortArray()){
					sortstring+="&sorting="+sort;
				}
			}
			
			//TODO REQUEST ERWEITERN
			
			GetMethod method = new GetMethod(this.path+"?"+filter+attributesstring+countstring+sortstring+"&type=JavaBIN");
		    
		    // Provide custom retry handler is necessary
		 	
		    method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
		    		new DefaultHttpMethodRetryHandler(3, false));

		    try {
			      // Execute the method.
			    int statusCode = client.executeMethod(method);
	
			    if (statusCode != HttpStatus.SC_OK) {
			    	HTTPClientRequestProcessor.log.error("Request failed: " + method.getStatusLine());
			    }
	
			    Collection<CRResolvableBean> result = new Vector<CRResolvableBean>();
			    
			    ObjectInputStream objstream = new ObjectInputStream(method.getResponseBodyAsStream());
			    Object responseObject;
				try {
					responseObject = objstream.readObject();
					
				    objstream.close();
	
				     
				    if(responseObject instanceof Collection)
				    {
				    	result = this.toCRResolvableBeanCollection(responseObject);
				    }
				    else if(responseObject instanceof CRError)
				    {
				    	CRError ex = (CRError)responseObject;
				    	throw new CRException(ex);
				    }
				    else
				    {
				    	HTTPClientRequestProcessor.log.error("COULD NOT CAST RESULT. Perhaps remote agent does not work properly");
				    }
			    
				} catch (ClassNotFoundException e) {
					HTTPClientRequestProcessor.log.error("Coult not load object from http response: "+e.getMessage());
					e.printStackTrace();
				}
		      
				if(result!=null)
				{
					for(CRResolvableBean crBean:result)
					{
						resultlist.add(crBean);
					}
				}

		    } catch (HttpException e) {
		      System.err.println("Fatal protocol violation: " + e.getMessage());
		      e.printStackTrace();
		    } catch (IOException e) {
		      System.err.println("Fatal transport error: " + e.getMessage());
		      e.printStackTrace();
		    } finally {
		      // Release the connection.
		      method.releaseConnection();
		    }  

		return resultlist;
	}


}
