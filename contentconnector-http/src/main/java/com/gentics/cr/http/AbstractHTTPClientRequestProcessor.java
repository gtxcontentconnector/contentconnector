package com.gentics.cr.http;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.ProtocolException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRError;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.exceptions.CRException;

/**
 * based on com.gentics.cr.http.HTTPClientRequestProcessor.
 */
public abstract class AbstractHTTPClientRequestProcessor extends RequestProcessor {

	private static Logger log = Logger.getLogger(AbstractHTTPClientRequestProcessor.class);
	protected String name = null;

	private static final String URL_KEY = "URL";
	/**
	 * Key to configure the used http version. Defaults to HTTP/1.0
	 *
	 * Can be configured in the following manner: HTTP/<major>.<minor>
	 */
	private static final String HTTP_VERSION_KEY = "HTTPVERSION";
	private String path = "";
	private HttpVersion httpVersion = HttpVersion.HTTP_1_0;
	protected HttpClient client;

	/**
	 * Create new instance of HTTPClientRequestProcessor.
	 * @param config
	 * @throws CRException
	 */
	public AbstractHTTPClientRequestProcessor(CRConfig config) throws CRException {
		super(config);
		this.name = config.getName();
		//LOAD ADDITIONAL CONFIG
		client = new HttpClient(new MultiThreadedHttpConnectionManager());
		this.path = (String) config.get(URL_KEY);
		if (this.path == null) {
			log.error("COULD NOT GET URL FROM CONFIG (add RP.<rpnumber>.url=<url> to config). OVERTHINK YOUR CONFIG!");
		}
		String httpVersionString = config.getString(HTTP_VERSION_KEY);
		if (httpVersionString != null) {
			try {
				this.httpVersion = HttpVersion.parse(httpVersionString);
			} catch (ProtocolException e) {
				throw new CRException(e);
			}
		}
	}

	/**
	 * Requests Objects from a remote ContentConnector Servlet using type JavaXML.
	 * @param request
	 * @param doNavigation
	 * @return Collection of CRResolvableBean
	 * @throws CRException
	 */
	public Collection<CRResolvableBean> getObjects(CRRequest request, boolean doNavigation) throws CRException {
		ArrayList<CRResolvableBean> resultlist = new ArrayList<CRResolvableBean>();

		String reqUrl = buildGetUrlString(request);

		GetMethod method = new GetMethod(reqUrl);

		method.getParams().setVersion(httpVersion);

		//Set request charset
		method.setRequestHeader("Content-type", "text/xml; charset=UTF-8");
		// Provide custom retry handler is necessary
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

		try {
			// Execute the method.
			int statusCode = client.executeMethod(method);
			log.info("Request: " + reqUrl + " Status: " + statusCode);
			if (statusCode != HttpStatus.SC_OK) {
				log.error("Request failed: " + method.getStatusLine());
			}

			Collection<CRResolvableBean> result = new Vector<CRResolvableBean>();
			ObjectInputStream objstream = new ObjectInputStream(method.getResponseBodyAsStream());
			Object responseObject;
			try {
				responseObject = objstream.readObject();

				objstream.close();

				if (responseObject instanceof Collection<?>) {
					result = this.toCRResolvableBeanCollection(responseObject);
				} else if (responseObject instanceof CRError) {
					CRError ex = (CRError) responseObject;
					throw new CRException(ex);
				} else {
					log.error("COULD NOT CAST RESULT. Perhaps remote agent does not work properly");
				}

			} catch (ClassNotFoundException e) {
				log.error("Coult not load object from http response", e);
				throw new CRException(e);
			}

			if (result != null) {
				for (CRResolvableBean crBean : result) {
					resultlist.add(crBean);
				}
			}

		} catch (HttpException e) {
			log.error("Fatal protocol violation", e);
			throw new CRException(e);
		} catch (IOException e) {
			log.error("Fatal transport error", e);
			throw new CRException(e);
		} finally {
			// Release the connection.
			method.releaseConnection();
		}

		return resultlist;
	}

	protected String buildGetUrlString(CRRequest request) {
		GetUrlBuilder urlBuilder = new GetUrlBuilder(this.path);

		urlBuilder.append("filter", request.getRequestFilter());
		urlBuilder.appendArray("attributes", request.getAttributeArray());
		urlBuilder.append(request, "count");
		urlBuilder.append(request, "start");
		urlBuilder.appendArray("sorting", request.getSortArray());
		urlBuilder.appendSkipFalse(request, RequestProcessor.META_RESOLVABLE_KEY);
		urlBuilder.appendSkipNull(request, RequestProcessor.HIGHLIGHT_QUERY_KEY);
		urlBuilder.append("type", "JavaBIN");

		appendCustomGetParam(urlBuilder, request);

		return urlBuilder.toString();
	}

	protected abstract void appendCustomGetParam(GetUrlBuilder urlBuilder, CRRequest request);

	@Override
	public void finalize() {

	}
}
