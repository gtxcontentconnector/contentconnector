package com.gentics.cr.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.exceptions.CRException.ERRORTYPE;
import com.gentics.cr.util.CRRequestBuilder;
import com.gentics.cr.util.ContentRepositoryConfig;
import com.gentics.cr.util.response.IResponseTypeSetter;
import com.gentics.lib.log.NodeLogger;

/**
 * Processes simple rest requests.
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 545 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class RESTSimpleContainer {

	/**
	 * RequestProcessor.
	 */
	private RequestProcessor rp;
	/**
	 * Encoding.
	 */
	private String responseEncoding;
	/**
	 * Contenttype.
	 */
	private String contenttype = "";
	/**
	 * Logger instance.
	 */
	private static final NodeLogger LOG = NodeLogger.getNodeLogger(RESTSimpleContainer.class);
	/**
	 * Configuration.
	 */
	private CRConfigUtil config;

	/**
	 * Get the content type as String.
	 * @return contettype as String.
	 */
	public final String getContentType() {
		return (this.contenttype + "; charset=" + this.responseEncoding);
	}

	/**
	 * Create new instance.
	 * @param crConf configuration.
	 */
	public RESTSimpleContainer(final CRConfigUtil crConf) {
		this.responseEncoding = crConf.getEncoding();
		this.config = crConf;
		try {
			this.rp = crConf.getNewRequestProcessorInstance(1);
		} catch (CRException e) {
			LOG.error("FAILED TO INITIALIZE REQUEST PROCESSOR... " + e.getStringStackTrace());
		}
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
	 * Process the whole service.
	 * @param reqBuilder reqBuilder
	 * @param wrappedObjectsToDeploy objects
	 * @param stream stream
	 * @param responsetypesetter responsetypesetter.
	 */
	public final void processService(final CRRequestBuilder reqBuilder, final Map<String, Resolvable> wrappedObjectsToDeploy,
			final OutputStream stream, final IResponseTypeSetter responsetypesetter) {
		CRRequestBuilder myReqBuilder = reqBuilder;
		CRRequest req = myReqBuilder.getCRRequest();
		ContentRepositoryConfig contentRepositoryConfig = myReqBuilder.getContentRepositoryConfig();
		processService(req, contentRepositoryConfig, wrappedObjectsToDeploy, stream, responsetypesetter, myReqBuilder.isDebug());
	}

	public final void processService(final CRRequest req, final ContentRepositoryConfig contentRepository,
			final Map<String, Resolvable> wrappedObjectsToDeploy, final OutputStream stream, final IResponseTypeSetter responsetypesetter) {
		processService(req, contentRepository, wrappedObjectsToDeploy, stream, responsetypesetter, false);
	}

	public final void processService(final CRRequest req, final ContentRepositoryConfig cRepository,
			final Map<String, Resolvable> wrappedObjectsToDeploy, final OutputStream stream, final IResponseTypeSetter responsetypesetter,
			final boolean debug) {

		Collection<CRResolvableBean> coll;
		ContentRepository cr = null;
		ContentRepositoryConfig contentRepository = cRepository;
		if (contentRepository == null) {
			contentRepository = new ContentRepositoryConfig(config);
		}
		try {
			cr = contentRepository.getContentRepository(this.responseEncoding, this.config);
			this.contenttype = cr.getContentType();
			if (responsetypesetter != null) {
				responsetypesetter.setContentType(this.getContentType());
			}
			boolean deployMetaresolvable = Boolean.parseBoolean((String) config.get(ContentRepository.DEPLOYMETARESOLVABLE_KEY));
			if (deployMetaresolvable) {
				req.set(RequestProcessor.META_RESOLVABLE_KEY, true);
			}
			//DEPLOY OBJECTS TO REQUEST AND TO RENDERER
			for (Entry<String, Resolvable> entry : wrappedObjectsToDeploy.entrySet()) {
				req.addObjectForFilterDeployment(entry.getKey(), entry.getValue());
				cr.addAdditionalDeployableObject(entry.getKey(), entry.getValue());
			}
			// Query the Objects from RequestProcessor
			coll = rp.getObjects(req);
			// add the objects to repository as serializable beans
			if (coll != null) {
				for (Iterator<CRResolvableBean> it = coll.iterator(); it.hasNext();) {
					cr.addObject(it.next());
				}
			}
			cr.toStream(stream);
		} catch (CRException ex) {
			//CR Error Handling
			//CRException is passed down from methods that want to post
			//the occured error to the client
			cr.respondWithError((OutputStream) stream, ex, debug);
			if (ex.getErrorType() == ERRORTYPE.NO_DATA_FOUND) {
				LOG.info(ex.getMessage(), ex);
			} else {
				LOG.error(ex.getMessage(), ex);
			}
		} catch (Exception ex) {
			CRException crex = new CRException(ex);
			LOG.error("Exception occured", crex);
			cr.respondWithError((OutputStream) stream, crex, debug);
			LOG.error(ex.getMessage(), crex);
		} finally {
			try {
				stream.flush();
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
