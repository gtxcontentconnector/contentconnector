package com.gentics.cr.http;

import java.beans.XMLDecoder;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import com.gentics.cr.lucene.search.LuceneRequestProcessor;
import com.gentics.cr.util.CRUtil;
/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class HTTPClientRequestProcessor extends RequestProcessor {

	private Logger log = Logger.getLogger(LuceneRequestProcessor.class);
	protected String name=null;
	
	protected String path=null;
	protected String idAttribute = null;
	protected String [] searchedAttributes = null;
	protected int count = 30;
	
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
		loadConfig();
		log=Logger.getLogger(this.getClass());
		client = new HttpClient();


	}

	/**
	 * Load additional config from file
	 */
	protected void loadConfig()
	{
		//TODO manage this using the config (additional parameters)
		Properties props = new Properties();
		try {
			String confpath = CRUtil.resolveSystemProperties("${com.gentics.portalnode.confpath}/rest/"+this.name+".properties");
			
			props.load(new FileInputStream(confpath));
			
			for (Iterator<Entry<Object,Object>> i = props.entrySet().iterator() ; i.hasNext() ; ) {
				Map.Entry<Object,Object> entry = (Entry<Object,Object>) i.next();
				Object value = entry.getValue();
				Object key = entry.getKey();
				this.setProperty((String)key, (String)value);
			}
			
		} catch (FileNotFoundException e1) {
			this.log.error("Could not load configuration file at: "+CRUtil.resolveSystemProperties("${com.gentics.portalnode.confpath}/rest/"+this.name+".properties")+"!");
		} catch (IOException e1) {
			this.log.error("Could not load configuration file at: "+CRUtil.resolveSystemProperties("${com.gentics.portalnode.confpath}/rest/"+this.name+".properties")+"!");
		}catch(NullPointerException e){
			this.log.error("Could not load configuration file at: "+CRUtil.resolveSystemProperties("${com.gentics.portalnode.confpath}/rest/"+this.name+".properties")+"!");
			e.printStackTrace();
		}
	}
	
	/**
	 * Set property that is loaded from file
	 * @param key
	 * @param value
	 */
	protected void setProperty(String key, String value)
	{
		//TODO Manage over config
		if(key instanceof String)
		{
			if("INDEXLOCATION".equalsIgnoreCase(key))
			{
				this.path = value;
			}
			else if("IDATTRIBUTE".equalsIgnoreCase(key))
			{
				this.idAttribute = value;
			}
			else if("SEARCHEDATTRIBUTES".equalsIgnoreCase(key))
			{
				this.searchedAttributes = value.split(",");
			}
			else if("SEARCHCOUNT".equalsIgnoreCase(key))
			{
				this.count = Integer.parseInt(value);
			}
			
		}
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
				countstring="count="+request.getCountString();
			}
			
			String sortstring="";
			if(request.getSortArray()!=null && request.getSortArray().length>0)
			{
				for(String sort:request.getSortArray()){
					sortstring+="&sorting="+sort;
				}
			}
			
			//TODO REQUEST ERWEITERN
			
			GetMethod method = new GetMethod(this.path+"?"+filter+attributesstring+countstring+sortstring+"&type=JavaXML");
		    
		    // Provide custom retry handler is necessary
		 	
		    method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
		    		new DefaultHttpMethodRetryHandler(3, false));

		    try {
			      // Execute the method.
			    int statusCode = client.executeMethod(method);
	
			    if (statusCode != HttpStatus.SC_OK) {
			    	this.log.error("Request failed: " + method.getStatusLine());
			    }
	
			    Collection<CRResolvableBean> result = new Vector<CRResolvableBean>();
			    XMLDecoder d = new XMLDecoder(method.getResponseBodyAsStream());
			      
			    Object responseObject = d.readObject();
			    d.close();
			     
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
			    	this.log.error("COULD NOT CAST RESULT. Perhaps remote agent does not work properly");
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
