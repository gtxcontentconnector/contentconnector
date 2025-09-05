package com.gentics.cr.sn;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.exceptions.CRException;
import com.gentics.lib.log.NodeLogger;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

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
	private static NodeLogger log = NodeLogger.getNodeLogger(TwitterJSONRequestProcessor.class);
	private static final String TWITTER_SEARCH_URL_KEY = "searchurl";
	private static final String TWITTER_DEFAULT_SEARCH_URL = "http://search.twitter.com/search.json";

	protected HttpClient client;
	private String searchurl = TWITTER_DEFAULT_SEARCH_URL;

	public TwitterJSONRequestProcessor(CRConfig config) throws CRException {
		super(config);
		client = HttpClientBuilder
					.create()
					.setRetryHandler(new DefaultHttpRequestRetryHandler(3, false))
					.build();
		String searchUrl = config.getString(TWITTER_SEARCH_URL_KEY);
		if (searchUrl != null) {
			this.searchurl = searchUrl;
		}
	}

	private String constructSearchURL(CRRequest req) {
		String url = this.searchurl;
		url += "?q=" + encode(req.getRequestFilter());

		int count = req.getCount();
		if (count != -1) {
			url += "&rpp=" + count;
		}
		int start = req.getStart();
		if (start != 0) {
			url += "&page=" + start;
		}

		return url;
	}

	public Collection<CRResolvableBean> getObjects(CRRequest req, boolean arg1) throws CRException {
		ArrayList<CRResolvableBean> resultlist = new ArrayList<CRResolvableBean>();

		HttpGet method = new HttpGet(constructSearchURL(req));

		// Provide custom retry handler is necessary
		method.setProtocolVersion(HttpVersion.HTTP_1_0);

		//Set request charset
		method.addHeader("Content-type", "text/xml; charset=UTF-8");

		try {
			// Execute the method.
			HttpResponse response = client.execute(method);
			int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode != HttpStatus.SC_OK) {
				log.error("Request failed: " + response.getStatusLine());
			}

			JSONObject json = (JSONObject) JSONSerializer.toJSON(IOUtils.toString(response.getEntity().getContent(), "UTF8"));
			JSONArray arr = json.getJSONArray("results");
			for (Object o : arr) {
				JSONObject item = (JSONObject) o;
				CRResolvableBean bean = createBean(item);
				resultlist.add(bean);
			}

		} catch (ClientProtocolException e) {
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
		for (Map.Entry entry : entrySet) {
			bean.set((String) entry.getKey(), entry.getValue());
		}
		return bean;
	}

	private String encode(String str) {
		if (str != null) {
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
