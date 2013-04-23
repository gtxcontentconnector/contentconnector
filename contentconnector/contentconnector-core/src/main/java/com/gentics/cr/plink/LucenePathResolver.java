package com.gentics.cr.plink;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.gentics.api.lib.datasource.Datasource;
import com.gentics.api.lib.datasource.DatasourceException;
import com.gentics.api.lib.datasource.DatasourceNotAvailableException;
import com.gentics.api.lib.exception.ParserException;
import com.gentics.api.lib.expressionparser.Expression;
import com.gentics.api.lib.expressionparser.ExpressionParserException;
import com.gentics.api.lib.expressionparser.filtergenerator.DatasourceFilter;
import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.api.portalnode.connector.PortalConnectorFactory;
import com.gentics.cr.CRConfig;
import com.gentics.cr.CRConfigFileLoader;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRDatabaseFactory;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.util.indexing.IndexController;

/**
 * This class is used to resolve URLs to objects an vice versa.
 *
 * Sample Configuration:
 *  main.properties
 *  ================
 * 	before.transformer.2.ds-handle.type=jdbc
 *	before.transformer.2.ds-handle.driverClass=com.mysql.jdbc.Driver
 *	before.transformer.2.ds-handle.url=jdbc:mysql://localhost:3306/content
 *	before.transformer.2.ds.cache=true
 *	before.transformer.2.ds.cache.foreignlinkattributes=true
 *	before.transformer.2.ds.cache.syncchecking=true
 *	before.transformer.2.attribute=content
 *	before.transformer.2.staticprefix=/Content.Node
 *	before.transformer.2.hostprefix=http://www.mydomain.at
 *	before.transformer.2.rule=object.obj_type==10007
 *	before.transformer.2.searchconfig=searcher
 *	before.transformer.2.indexconfig=indexer
 *	before.transformer.2.transformerclass=\
 *com.gentics.cr.lucene.indexer.transformer.LinkToPlinkTransformer
 *	before.transformer.3.attribute=content
 *	before.transformer.3.rule=object.obj_type==10007
 *	before.transformer.3.transformerclass=\
 *com.gentics.cr.lucene.indexer.transformer.LinkTargetTransformer
 *	before.transformer.3.externaltarget=_blank
 *	before.transformer.3.externalalt=&Ouml;ffnet in einem neuen Fenster
 *
 *  indexer.properties
 *  ==================
 *  #file system location of index
 *  index.1.indexLocations.1.path=${com.gentics.portalnode.confpath}/index/index
 *  #index.1.indexLocations.1.path=RAM
 *  #interval in seconds
 *  index.1.interval=5
 *  #job check interval
 *  index.1.checkinterval=5
 *  index.1.periodical=true
 *  #job batch size
 *  index.1.batchsize=10
 *  #ContentRepository specific config
 *  index.1.CR.1.rp.1.rpClass=com.gentics.cr.CRRequestProcessor
 *  index.1.analyzerconfig\
 *  =${com.gentics.portalnode.confpath}/rest/analyzer.properties
 *  #Datasource
 *  index.1.CR.1.rp.1.ds-handle.type=jdbc
 *  index.1.CR.1.rp.1.ds-handle.driverClass=com.mysql.jdbc.Driver
 *  index.1.CR.1.rp.1.ds-handle.url=jdbc:mysql://localhost:3306/content
 *  #DO NOT USE CACHE FOR INDEXING
 *  index.1.CR.1.rp.1.ds.cache=false
 *  index.1.CR.1.rp.1.ds.cache.foreignlinkattributes=false
 *  index.1.CR.1.rp.1.ds.cache.syncchecking=false
 *  index.1.CR.1.rp.1.response-charset=UTF-8
 *  index.1.CR.1.rp.1.binarytype=10008
 *  index.1.CR.1.rp.1.foldertype=10002
 *  index.1.CR.1.rp.1.pagetype=10007
 *  #index.1.CR.2.ds-handle.portalnodedb=ccr
 *  index.1.CR.1.rule=object.node_id==15 AND object.obj_type==10007
 *  #index.1.CR.1.rule=10009==10008
 *  index.1.CR.1.indexedAttributes=pub_dir,filename
 *  index.1.CR.1.containedAttributes=pub_dir,filename
 *  index.1.CR.1.idattribute=contentid
 *  index.1.CR.1.batchsize=5
 *  
 *  indexer.properties
 *  ==================
 * 
 * Last changed: $Date: 2010-01-18 15:57:21 +0100 (Mo, 18 Jän 2010) $
 * @version $Revision: 401 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class LucenePathResolver {

	/**
	 * Configuration.
	 */
	private CRConfig conf = null;
	/**
	 * Logger.
	 */
	private static Logger log = Logger.getLogger(LucenePathResolver.class);
	/**
	 * IndexConfigKey.
	 */
	private static final String INDEXER_CONFIG_FILE_KEY = "indexconfig";
	/**
	 * SearchConfigKey.
	 */
	private static final String SEARCH_CONFIG_FILE_KEY = "searchconfig";

	/**
	 * IndexController.
	 */
	private IndexController idx = null;
	/**
	 * RequestProcessor.
	 */
	private RequestProcessor rp = null;

	/**
	 * Initialize the expression needed to resolve Objects from passed URLs. As
	 * this uses a lot of time initalization in the constructor improves
	 * performance. Initialize PathResolver once on Server startup.
	 * @param config configuration 
	 * 
	 */
	public LucenePathResolver(final CRConfig config) {

		this.conf = config;
		String cxname = this.conf.getString(INDEXER_CONFIG_FILE_KEY);
		if (cxname == null) {
			cxname = "indexer";
		}
		CRConfigUtil idxConfig = new CRConfigFileLoader(cxname, null);

		String csname = this.conf.getString(SEARCH_CONFIG_FILE_KEY);
		if (csname == null) {
			csname = "searcher";
		}
		CRConfigUtil srcConfig = new CRConfigFileLoader(csname, null);

		idx = new IndexController(idxConfig);
		try {
			rp = srcConfig.getNewRequestProcessorInstance(1);
		} catch (CRException e) {
			log.error("Could not initialize searcher request processor." + "Check your searcher config.", e);
		}
	}

	/**
	 * Destroys the PathResolver.
	 */
	public final void destroy() {
		this.idx.stop();
	}

	/**
	 * The Method looks in the repository with the expression initialized in the
	 * constructor and tries to find a corrisponding object. This only works
	 * correctly when only one node is in the repository, otherwise there may
	 * bee more object with the same URL in the repository.
	 * @param request 
	 * @return a Resolvable Object based on the passed URL.
	 */
	public final CRResolvableBean getObject(final CRRequest request) {
		CRResolvableBean contentObject = null;
		String url = request.getUrl();
		if (url != null) {

			CRRequest r = new CRRequest();

			PathBean pb = new PathBean(request.getUrl());
			String path = pb.getPath();

			String filter = "";
			if (path == null || "".equals(path)) {
				filter = "(pub_dir:(/)) AND filename:(" + pb.getFilename() + ")";
			} else {
				filter = "(pub_dir:(" + pb.getPath() + ") OR pub_dir:(" + pb.getPath() + "/)) AND filename:("
						+ pb.getFilename() + ")";
			}
			log.debug("Using filter: " + filter);
			r.setRequestFilter(filter);
			try {
				contentObject = rp.getFirstMatchingResolvable(r);
			} catch (CRException e) {
				log.error("Could not load object from path " + url, e);
			}

		}

		return contentObject;
	}

	/**
	 * initializes a Resolvable Object and calls {@link com.gentics.cr.plink.LucenePathResolver#getPath(Resolvable)}.
	 * 
	 * @param contentid contentid
	 * @return string
	 */
	public final String getPath(final String contentid) {

		Resolvable linkedObject = null;
		Datasource ds = null;
		try {
			ds = this.conf.getDatasource();
			// initialize linked Object
			linkedObject = PortalConnectorFactory.getContentObject(contentid, ds);
			return getPath(linkedObject);

		} catch (DatasourceNotAvailableException e) {
			log.error("Datasource error generating url for " + contentid);
		} finally {
			CRDatabaseFactory.releaseDatasource(ds);
		}

		// if the linked Object cannot be initialized return a dynamic URL
		//		this.log.info("Use dynamic url for " + contentid);
		//		return getDynamicUrl(contentid);
		return (null);
	}

	/**
	 * @param linkedObject
	 *            must be a Resolvable
	 * @return a the URL from Gentics Content.Node. This expects the attributes
	 *         filename and folder_id to be set for the passed object and the
	 *         attribute pub_dir to be set for folders.
	 */
	public final String getPath(final Resolvable linkedObject) {

		String filename = "";
		String pubdir = "";

		if (linkedObject != null) {

			// get filename from linkedObject
			filename = (String) linkedObject.get("filename");

			// Get folder Object from attribute folder_id and attribute pub_dir
			// from it
			Resolvable folder = (Resolvable) linkedObject.get("folder_id");
			if (folder != null) {
				pubdir = (String) folder.get("pub_dir");
			}
		}

		// If filename is empty or not set, no need to return an path
		if (filename != null && !"".equals(filename)) {

			return pubdir + filename;
		} else {

			// return a dynamic URL instead
			log.warn("Object " + linkedObject.get("contentid") + " has no filename.");
			//this.log.info("Use dynamic url for "
			//		+ linkedObject.get("contentid"));

			//return getDynamicUrl((String) linkedObject.get("contentid"));
			return (null);
		}
	}

	/**
	 * @param contentid contentid
	 * @return a dynamic URL to suitable for CRServlet.
	 */
	public final String getDynamicUrl(final String contentid) {

		return "?contentid=" + contentid;

	}

	/**
	 * Get the alternate URL for the request. 
	 * This is used if the object cannot be resolved dynamically.
	 * @param contentid String with the identifier of the object
	 * @return String with the configured servlet / portal url.
	 */
	public final String getAlternateUrl(final String contentid) {
		String url = null;
		String objtype = contentid.split("\\.")[0];
		if (objtype != null) {
			//try to get a specific URL for the objecttype
			url = conf.getString(CRConfig.ADVPLR_HOST + "." + objtype);
		}
		if (url == null) {
			//if we didn't get a specific URL take the generic one
			url = conf.getString(CRConfig.ADVPLR_HOST);
		}
		return url + contentid;
	}

	/**
	 * @param contentid contentid
	 * @param config config
	 * @param request request
	 * @return a dynamic URL to suitable for CRServlet.
	 * Supports beautiful URLs, 
	 * therefore it needs to load DB Objects and Attributes 
	 */
	public final String getDynamicUrl(final String contentid, final CRConfig config, final CRRequest request) {
		String url = null;
		if (request != null) {
			url = (String) request.get("url");
			if (url == null) {
				return getDynamicUrl(contentid);
			} else {
				Datasource ds = null;
				String ret = null;
				try {
					//if there is an attribute URL the servlet was 
					//called with a beautiful URL so give back a beautiful URL
					//check if valid local link
					String applicationrule = (String) config.get("applicationrule");
					ds = config.getDatasource();
					Expression expression = null;
					try {
						expression = PortalConnectorFactory.createExpression("object.contentid == '" + contentid
								+ "' && " + applicationrule);
					} catch (ParserException exception) {
						log.error("Error while building expression object for " + contentid, exception);
						ret = getDynamicUrl(contentid);
					}

					DatasourceFilter filter = null;
					try {
						filter = ds.createDatasourceFilter(expression);
					} catch (ExpressionParserException e) {
						log.error("Error while building filter object for " + contentid, e);
						ret = getDynamicUrl(contentid);
					}
					int count = 0;
					try {
						count = ds.getCount(filter);
					} catch (DatasourceException e) {
						log.error("Error while querying for " + contentid, e);
						ret = getDynamicUrl(contentid);
					}

					if (count == 0 || "true".equals(config.getString(CRConfig.ADVPLR_HOST_FORCE))) {
						//not permitted or forced, build link
						ret = getAlternateUrl(contentid);
					} else {

						Resolvable plinkObject;
						try {
							plinkObject = PortalConnectorFactory.getContentObject(contentid, ds);
							//TODO: make this more beautiful and 
							//compatible with portlets
							String filenameattribute = (String) config.get(CRConfig.ADVPLR_FN_KEY);
							String pubdirattribute = (String) config.get(CRConfig.ADVPLR_PB_KEY);
							String filename = (String) plinkObject.get(filenameattribute);
							String pubdir = (String) plinkObject.get(pubdirattribute);
							HttpServletRequest servletRequest = (HttpServletRequest) request.get("request");
							String contextPath = servletRequest.getContextPath();
							String servletPath = servletRequest.getServletPath();
							ret = contextPath + servletPath + pubdir + filename;
						} catch (DatasourceNotAvailableException e) {
							log.error("Error while getting object for " + contentid, e);
							ret = getDynamicUrl(contentid);
						}
					}
				} catch (Exception e) {
					log.error("Error while processing dynamic url", e);
				} finally {
					CRDatabaseFactory.releaseDatasource(ds);
				}
				return ret;
			}
		} else {
			return getAlternateUrl(contentid);
		}

	}

}
