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

import org.apache.log4j.Logger;

import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.plink.PLinkOutputStream;
import com.gentics.cr.plink.PlinkProcessor;
import com.gentics.cr.plink.PlinkReplacer;
import com.gentics.cr.rendering.ContentRendererFactory;
import com.gentics.cr.rendering.IContentRenderer;
import com.gentics.cr.util.CRBinaryRequestBuilder;
import com.gentics.cr.util.response.IResponseTypeSetter;

/**
 * Container for Binary responses.
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * 
 * @version $Revision: 545 $
 * @author $Author: supnig@constantinopel.at $
 * 
 */
public class RESTBinaryStreamingContainer {

	/**
	 * Request processor.
	 */
	private RequestProcessor rp;
	
	/**
	 * Response encoding.
	 */
	private String responseEncoding;
	
	/**
	 * contenttype.
	 */
	private String contenttype = "";
	
	/**
	 * Logger.
	 */
	private static Logger log = Logger
			.getLogger(RESTBinaryStreamingContainer.class);
	
	/**
	 * Contentrenderer.
	 */
	private IContentRenderer contentRenderer;
	
	/**
	 * PLinkprocessor.
	 */
	private PlinkProcessor plinkProcessor;
	
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
	 * get conten type as string.
	 * 
	 * @return contenttype
	 */
	public final String getContentType() {
		return (this.contenttype);
	}

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
	public RESTBinaryStreamingContainer(final CRConfigUtil config) {
		this.responseEncoding = config.getEncoding();
		this.crConf = config;
		try {
			this.rp = crConf.getNewRequestProcessorInstance(1);
		} catch (CRException e) {
			CRException ex = new CRException(e);
			log.error("FAILED TO INITIALIZE REQUEST PROCESSOR... "
					+ ex.getStringStackTrace());
		}
		contentRenderer = ContentRendererFactory
				.getRendererInstance(crConf.getRequestProcessorConfig(1));
		plinkProcessor = new PlinkProcessor(
				crConf.getRequestProcessorConfig(1));
	}

	/**
	 * Respond with error.
	 * @param stream output stream
	 * @param ex exception
	 * @param debug true if we have to write the stackstrace
	 */
	private void respondWithError(final OutputStream stream,
			final CRException ex,
			final boolean debug) {
		String ret = "" + ex.getMessage();
		if (debug) {
			ret += " - " + ex.getStringStackTrace();
		}
		try {
			OutputStreamWriter wr = new OutputStreamWriter(stream,
					this.responseEncoding);
			wr.write(ret);
			wr.flush();
			wr.close();
		} catch (IOException e) {
			e.printStackTrace();
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
		CRBinaryRequestBuilder myReqBuilder = reqBuilder;
		CRResolvableBean crBean = null;
		CRRequest req;
		try {
			req = myReqBuilder.getBinaryRequest();
			//The StreamingBinaryContainer will 
			//not render velocity in the content
			req.setDoVelocity(false);
			// DEPLOY OBJECTS TO REQUEST
			for (Iterator<Map.Entry<String, Resolvable>> 
					i = wrappedObjectsToDeploy
					.entrySet().iterator(); i.hasNext();) {
				Map.Entry<String, Resolvable> entry = 
						(Entry<String, Resolvable>) i.next();
				req.addObjectForFilterDeployment((String) entry.getKey(),
						entry.getValue());
			}
			if (this.crConf.usesContentidUrl()) {
				if (req.getContentid() == null) {
					Object obj = reqBuilder.getRequest();
					if (obj instanceof HttpServletRequest) {
						String[] reqURI = ((HttpServletRequest) obj)
								.getRequestURI().split("/");
						ArrayList<String> reqList = new ArrayList<String>(
								Arrays.asList(reqURI));
						int index = reqList.indexOf(((HttpServletRequest) obj)
								.getServletPath().replaceAll("/", ""));
						if (reqList.size() >= index + 1) {
							req.setRequestFilter("object.contentid=="
									+ reqList.get(index + 1).toString());
						}
					}
				}
			}
			req.setAttributeArray(new String[] { "mimetype" });
			// load by url if no contentid
			if (req.isUrlRequest()) {
				crBean = rp.getBeanByURL(req);
			} else {
				crBean = rp.getFirstMatchingResolvable(req);
			}
			if (crBean != null) {
				// set mimetype.
				if (crBean.getMimetype() == null) {

					CRConfigUtil rpConf = crConf.getRequestProcessorConfig(1);
					if (crBean.getObj_type().equals(rpConf.getPageType())) {
						this.contenttype = "text/html; charset="
								+ this.responseEncoding;
						log.info("Responding with mimetype: text/html");
					} else {
						log.info("Mimetype has not been set, using "
								+ "standard instead. ("
								+ crBean.getObj_type()
								+ "!="
								+ rpConf.getPageType() + ")");
					}
				} else {

					this.contenttype = crBean.getMimetype() + "; charset="
							+ this.responseEncoding;

					log.info("Responding with mimetype: "
							+ crBean.getMimetype());
				}

				responsetypesetter.setContentType(this.getContentType());
				// output data.
				if (crBean.isBinary()) {
					stream.write(crBean.getBinaryContent());

				} else {
					
					PLinkOutputStream plos = new PLinkOutputStream(stream,
							new PlinkReplacer(plinkProcessor, req));
					
					OutputStreamWriter wr = new OutputStreamWriter(plos,
							this.responseEncoding);
					String content = crBean.getContent(this.responseEncoding);
					wr.write(content);
					wr.flush();
					wr.close();
				}
			} else {
				CRException crex = new CRException("NoDataFound",
						"Data could not be found.");
				this.respondWithError(stream, crex, myReqBuilder.isDebug());
			}
			stream.flush();
			stream.close();
		} catch (CRException e1) {
			this.contenttype = "text/html; charset=" + this.responseEncoding;
			respondWithError((OutputStream) stream, e1, myReqBuilder.isDebug());
			e1.printStackTrace();
		} catch (Exception e) {
			log.error("Error while processing service "
					+ "(RESTBinaryContainer)", e);
			CRException crex = new CRException(e);
			this.respondWithError(stream, crex, myReqBuilder.isDebug());
		}

	}
}
