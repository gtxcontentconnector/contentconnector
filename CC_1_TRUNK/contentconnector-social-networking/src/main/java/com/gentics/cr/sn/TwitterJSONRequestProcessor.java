package com.gentics.cr.sn;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.exceptions.CRException;
/**
 * This Class can be used to request CRResolvableBeans from the Twitter Search API
 * 
 * 
 * A simple configurationfile would look like this:
 * #RequestProcessor that fetches search results from twitter
 * rp.1.rpClass=com.gentics.cr.sn.TwitterJSONRequestProcessor
 * #you can configure the twitter search api here
 * rp.1.searchurl=http://search.twitter.com/search.json
 *
 * 
 * the following commands are supported:
 * 
 * filter=<your search>
 * count=<number of items per page: max 100>
 * start=<number of page to display>
 * 
 * 
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 545 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class TwitterJSONRequestProcessor extends RequestProcessor {
	private static Logger log = Logger.getLogger(TwitterJSONRequestProcessor.class);
	private static final String TWITTER_SEARCH_URL_KEY="searchurl";
	private static final String TWITTER_DEFAULT_SEARCH_URL="http://search.twitter.com/search.json";
	
	protected HttpClient client;
	private String searchurl=TWITTER_DEFAULT_SEARCH_URL;
	
	public TwitterJSONRequestProcessor(CRConfig config) throws CRException {
		super(config);
		client = new HttpClient();
		String t_surl = config.getString(TWITTER_SEARCH_URL_KEY);
		if(t_surl!=null)this.searchurl=t_surl;
	}
	
	private String constructSearchURL(CRRequest req)
	{
		String url =this.searchurl;
		url+="?q="+encode(req.getRequestFilter());
		
		int count = req.getCount();
		if(count!=-1)
		{
			url+="&rpp="+count;
		}
		int start = req.getStart();
		if(start!=0)
		{
			url+="&page="+start;
		}
		
		return url;
	}

	
	public Collection<CRResolvableBean> getObjects(CRRequest req, boolean arg1)
			throws CRException {
		ArrayList<CRResolvableBean> resultlist = new ArrayList<CRResolvableBean>();
		
		GetMethod method = new GetMethod(constructSearchURL(req));
	    
	    // Provide custom retry handler is necessary
	 	method.getParams().setVersion(HttpVersion.HTTP_1_0);
	 	
	 	//Set request charset
	 	method.setRequestHeader("Content-type","text/xml; charset=UTF-8");
	 	
	    method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
	    		new DefaultHttpMethodRetryHandler(3, false));

	    try {
		      // Execute the method.
		    int statusCode = client.executeMethod(method);

		    if (statusCode != HttpStatus.SC_OK) {
		    	log.error("Request failed: " + method.getStatusLine());
		    }

		    		    
		   
		    JSONObject json = (JSONObject) JSONSerializer.toJSON( method.getResponseBodyAsString());
		    JSONArray arr = json.getJSONArray("results");
		    for (Object o : arr) {
		        JSONObject item = (JSONObject) o;
		        CRResolvableBean bean = createBean(item);
		        resultlist.add(bean);
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

	
	@SuppressWarnings("unchecked")
	private CRResolvableBean createBean(JSONObject item) {
		Set<Map.Entry> entrySet = item.entrySet();
		CRResolvableBean bean = new CRResolvableBean();
		for(Map.Entry entry:entrySet)
		{
			bean.set((String)entry.getKey(), entry.getValue());
		}
		return bean;
	}

	
	private String encode(String str)
	{
		if(str!=null)
		{
			try {
				return URLEncoder.encode(str, "UTF-8");
			} catch (UnsupportedEncodingException e2) {
				log.error(e2.getMessage());
				e2.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public void finalize() {
		// TODO Auto-generated method stub
		
	}
}
