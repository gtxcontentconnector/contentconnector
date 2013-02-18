package com.gentics.cr.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpConstants;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;

import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.exceptions.CRException.ERRORTYPE;
import com.gentics.cr.util.CRBinaryRequestBuilder;
import com.gentics.cr.util.response.IResponseTypeSetter;
import com.gentics.lib.http.HTTPRequest;
import com.sun.jersey.api.core.HttpResponseContext;

/**
 * Container for Binary responses.
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * 
 * @version $Revision: 545 $
 * @author $Author: supnig@constantinopel.at $
 * 
 */
public class RESTBinaryContainer {

	/**
	 * Request processor.
	 */
	private RequestProcessor rp;

	/**
	 * Response encoding.
	 */
	private String responseEncoding;

	/**
	 * Logger.
	 */
	private static Logger log = Logger.getLogger(RESTBinaryContainer.class);

	/**
	 * Config.
	 */
	private CRConfigUtil crConf;

	/**
	 * Key for br replacements.
	 */
	private static final String LIVEEDITORXHTML_KEY 
		= "container.liveeditorXHTML";

	
	/**
	 * Finalize the Container.
	 */
	public final void finalize() {
		if (this.rp != null) {
			this.rp.finalize();
		}
	}

	/**
	 * Create new instance.
	 * @param config config
	 */
	public RESTBinaryContainer(final CRConfigUtil config) {
		this.responseEncoding = config.getEncoding();
		this.crConf = config;
		try {
			this.rp = crConf.getNewRequestProcessorInstance(1);
		} catch (CRException e) {
			CRException ex = new CRException(e);
			log.error("FAILED TO INITIALIZE REQUEST PROCESSOR... "
					+ ex.getStringStackTrace());
		}
	}

	/**
	 * Respond with error.
	 * @param stream output stream
	 * @param ex exception
	 * @param debug true if we have to write the stackstrace
	 * @param responseTypeSetter setter for the response type.
	 */
	private void respondWithError(final OutputStream stream,
			final CRException ex, final boolean debug, 
			final IResponseTypeSetter responseTypeSetter) {
		responseTypeSetter.setContentType(
				"text/html; charset=" + this.responseEncoding);
		if (ex.getErrorType() == ERRORTYPE.NO_DATA_FOUND) {
			responseTypeSetter.setResponseCode(HttpStatus.SC_NOT_FOUND);
		} else {
			responseTypeSetter.setResponseCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
		}
		String ret = "" + ex.getMessage();
		if (debug) {
			ret += " - " + ex.getStringStackTrace();
		}
		try {
			OutputStreamWriter wr 
				= new OutputStreamWriter(stream, this.responseEncoding);
			wr.write(ret);
			wr.flush();
			wr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public final void processService(CRRequest request, 
			final Map<String, Resolvable> wrappedObjectsToDeploy,
			final OutputStream stream,
			final IResponseTypeSetter responsetypesetter,
			final Object requestObject,
			boolean debug) {
		CRResolvableBean crBean = null;
		CRRequest req;
		try {
			req = request;
			// DEPLOY OBJECTS TO REQUEST
			for (Iterator<Map.Entry<String, Resolvable>> 
					i = wrappedObjectsToDeploy.entrySet()
						.iterator(); i.hasNext();) {
				Map.Entry<String, Resolvable> entry 
					= (Entry<String, Resolvable>) i.next();
				req.addObjectForFilterDeployment(
					(String) entry.getKey(), entry.getValue());
			}
			if (this.crConf.usesContentidUrl()) {
				if (req.getContentid() == null) {
					Object obj = requestObject;
					if (obj != null && obj instanceof HttpServletRequest) {
						String[] reqURI = ((HttpServletRequest) obj)
								.getRequestURI().split("/");
						ArrayList<String> reqList 
							= new ArrayList<String>(Arrays.asList(reqURI));
						int index = reqList.indexOf(
							((HttpServletRequest) obj).getServletPath()
								.replaceAll("/", ""));
						if (reqList.size() >= index + 1) {
							req.setRequestFilter(
									"object.contentid==" 
									+ reqList.get(index + 1).toString());
						}
					}
				}
			}
			req.setAttributeArray(new String[] { "mimetype" });
			// load by url if no contentid
			if (req.isUrlRequest()) {
				crBean = rp.getContentByUrl(req);
			} else {
				crBean = rp.getContent(req);
			}
			if (crBean != null) {
				String mimetype = crBean.getMimetype();
				if (mimetype == null) {

					CRConfigUtil rpConf = crConf.getRequestProcessorConfig(1);
					if (crBean.getObj_type().equals(rpConf.getPageType())) {
						mimetype = "text/html; charset=" + this.responseEncoding;
						log.info("Responding with mimetype: text/html");
					} else {
						log.info("Mimetype has not been set, using " + "standard instead. (" + crBean.getObj_type()
								+ "!=" + rpConf.getPageType() + ")");
					}
				} else {
					// Charset should only be set if content is not a binary
					mimetype = crBean.getMimetype(); //+ "; charset=" + this.responseEncoding;

					log.info("Responding with mimetype: " + crBean.getMimetype());
				}

				responsetypesetter.setContentType(mimetype);
				responsetypesetter.setResponseCode(HTTPRequest.HTTP_OK);
				// output data.
				if (crBean.isBinary()) {
					log.debug("Size of content: " + crBean.getBinaryContent().length);
					stream.write(crBean.getBinaryContent());

				} else {
					OutputStreamWriter wr = new OutputStreamWriter(stream, this.responseEncoding);
					String content = crBean.getContent(this.responseEncoding);
					if (Boolean.parseBoolean((String) crConf.get(LIVEEDITORXHTML_KEY))) {
						// Gentics Content.Node Liveeditor produces non XHTML
						// brakes.
						// Therefore we must replace them before we return the
						// code
						// TODO This is quite ugly => do this in a stream
						content = content.replace("<BR>", "</ br>");
					}
					wr.write(content);
					wr.flush();
					wr.close();
				}
			} else {
				CRException crex = new CRException("NoDataFound", "Data could not be found.", ERRORTYPE.NO_DATA_FOUND);
				this.respondWithError(stream, crex, debug, responsetypesetter);
			}
			stream.flush();
			stream.close();
		} catch (CRException e1) {
			respondWithError((OutputStream) stream, e1, debug, responsetypesetter);
			e1.printStackTrace();
		} catch (Exception e) {
			log.error("Error while processing service " + "(RESTBinaryContainer)", e);
			CRException crex = new CRException(e);
			respondWithError(stream, crex, debug, responsetypesetter);
		}
	}
	
	/**
	 * Process the whole Service.
	 * 
	 * @param reqBuilder Request Builder
	 * @param wrappedObjectsToDeploy Objects to deploy
	 * @param stream output stream
	 * @param responsetypesetter responsetypesetter
	 */
	public final void processService(final CRBinaryRequestBuilder reqBuilder,
			final Map<String, Resolvable> wrappedObjectsToDeploy,
			final OutputStream stream,
			final IResponseTypeSetter responsetypesetter) {
		
		processService(reqBuilder.getBinaryRequest(), wrappedObjectsToDeploy, stream, responsetypesetter, reqBuilder.getRequest(), reqBuilder.isDebug());
		
	}
}
