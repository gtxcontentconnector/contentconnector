package com.gentics.cr.plink;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.gentics.api.lib.cache.PortalCache;
import com.gentics.api.lib.cache.PortalCacheException;
import org.apache.log4j.Logger;

import com.gentics.api.lib.datasource.Datasource;
import com.gentics.api.lib.datasource.DatasourceNotAvailableException;
import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.api.portalnode.connector.PLinkInformation;
import com.gentics.api.portalnode.connector.PortalConnectorFactory;
import com.gentics.cr.CRConfig;
import com.gentics.cr.CRDatabaseFactory;
import com.gentics.cr.CRRequest;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.template.ITemplateManager;

/**
 * CRPlinkProcessor should be initialized once and passed to CRPlinkReplacer on
 * initalization. The Processor expects a plinkTemplate in the CRConfig.
 * 
 * 
 * Last changed: $Date: 2010-01-25 12:05:57 +0100 (Mo, 25 Jän 2010) $
 * @version $Revision: 406 $
 * @author $Author: supnig@constantinopel.at $
 */
public class PlinkProcessor {

	private CRConfig config;

	Map<String, Resolvable> contextObjects;

	private static Logger log = Logger.getLogger(PlinkProcessor.class);

	private static PortalCache plinkCache;

	/**
	 * configuration key to enable (default) or disable the JCS cache for resolving PLinks.
	 */
	public final static String PLINK_CACHE_ACTIVATION_KEY = "plinkcache";

	private boolean plinkcache = true;

	/**
	 * Create new instance of plink processor.
	 * @param config
	 */
	public PlinkProcessor(final CRConfig config) {

		this.config = config;
		contextObjects = new HashMap<String, Resolvable>();
		if (config != null && config.getPortalNodeCompMode()) {
			//Servlet will run in portal.node compatibility mode => no velocity available
			log.warn("CRPlinkProcessor is running in Portal.Node 3 compatibility mode \n"
					+ "Therefore Velocity scripts will not work in the content.");
		}
		String plinkCacheEnabledString = null;
		if (config != null) {
			plinkCacheEnabledString = config.getString(PLINK_CACHE_ACTIVATION_KEY);
		}
		if (plinkCacheEnabledString != null && !"".equals(plinkCacheEnabledString)) {
			plinkcache = Boolean.parseBoolean(plinkCacheEnabledString);
		}

		if (plinkcache) {
			try {
				String configName = "shared";
				if (config != null && config.getName() != null) {
					configName = config.getName();
				} else {
					log.error("Attention i'm using a shared plinkcache because i'm missing my config or the config name.");
				}
				plinkCache = PortalCache.getCache("gentics-cr-" + configName + "-plinks");
				log.debug("Initialized cache zone for \"" + configName + "-plinks\".");

			} catch (PortalCacheException e) {

				log.warn("Could not initialize Cache for PlinkProcessor.");

			}
		} else {
			plinkCache = null;
		}
	}

	/**
	 * Deploy objects to the velocity context.
	 * @param map
	 */
	public void deployObjects(final Map<String, Resolvable> map) {
		Iterator<String> it = map.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			this.contextObjects.put(key, map.get(key));
		}
	}

	/**
	 * get a generated link according to the information in plinkinformation.
	 * @param plink
	 * @param request
	 */
	public String getLink(final PLinkInformation plink, final CRRequest request) {

		// starttime
		long start = new Date().getTime();

		String link = "";
		String contentid = plink.getContentId();
		String cacheKey = contentid;

		String type = "";
		if (request.getRequest() != null && request.getRequest() instanceof HttpServletRequest) {
			type = ((HttpServletRequest) request.getRequest()).getParameter("format");
			String typeArg = ((HttpServletRequest) request.getRequest()).getParameter("type");
			if (typeArg != null && !typeArg.equals("")) {
				type = typeArg;
			}
			// reset to an empty string if php or null
			// otherwise just use the given type for the cache-key
			if (type == null || type.equals("php")) {
				type = "";
			}
			// if the link refers to a binary-file, always use an empty type (this falls back to "php")
			if (contentid.startsWith(config.getBinaryType() + ".")) {
				type = "";
			}
			cacheKey += "-" + type;
		}

		// load link from cache
		if (plinkCache != null) {
			try {
				link = (String) plinkCache.get(cacheKey);
			} catch (PortalCacheException e) {
				;
			}
		}

		// no cache object so try to prepare a link
		if (("".equals(link) || link == null) && !config.getPortalNodeCompMode()) {

			// Render Content with contentid as template name
			Resolvable plinkObject;
			Datasource ds = null;
			try {
				ds = this.config.getDatasource();
				plinkObject = PortalConnectorFactory.getContentObject(contentid, ds);
				

				ITemplateManager myTemplateEngine = this.config.getTemplateManager();
				// Put objects in the plink template
				myTemplateEngine.put("plink", plinkObject);
				//Deploy predefined Objects to the context
				Iterator<String> it = this.contextObjects.keySet().iterator();
				while (it.hasNext()) {
					String key = it.next();
					myTemplateEngine.put(key, this.contextObjects.get(key));
				}
				// as url is a special object put it also in the templates
				if (this.config.getPathResolver() != null) {
					String url = this.config.getPathResolver().getPath(plinkObject);
					if (url != null) {
						myTemplateEngine.put("url", url);
					}
				}

				try {
					link = myTemplateEngine.render("link", this.config.getPlinkTemplate());
				} catch (Exception e) {
					throw new CRException(e);
				}

			} catch (DatasourceNotAvailableException e) {
				CRException ex = new CRException(e);
				log.error(ex.getMessage() + ex.getStringStackTrace());
			} catch (CRException ex) {
				log.error(ex.getMessage() + ex.getStringStackTrace());
			} finally {
				CRDatabaseFactory.releaseDatasource(ds);
			}
			// endtime
			long end = new Date().getTime();

			log.debug("plink generationtime for link " + contentid + ": " + (end - start));

		}

		// If velocity template parsing and caching does not work for any
		// reason use a dynamic link
		if ("".equals(link) || link == null) {

			if ("true".equals(this.config.get(CRConfig.ADVPLR_KEY))) {
				link = this.config.getPathResolver().getDynamicUrl(contentid, config, request);
			} else {
				link = this.config.getPathResolver().getDynamicUrl(contentid);
			}
		}

		// add link to cache
		try {

			if (plinkCache != null) {
				plinkCache.put(cacheKey, link);
			}

		} catch (PortalCacheException e) {

			log.warn("Could not add link to object " + contentid + " to cache");
		}

		return link;
	}
}
