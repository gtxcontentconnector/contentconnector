package com.gentics.cr.plink;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.apache.log4j.Logger;

import com.gentics.api.lib.datasource.DatasourceNotAvailableException;
import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.api.portalnode.connector.PLinkInformation;
import com.gentics.api.portalnode.connector.PortalConnectorFactory;
import com.gentics.cr.CRConfig;
import com.gentics.cr.CRException;
import com.gentics.cr.CRRequest;
import com.gentics.cr.template.ITemplateManager;

/**
 * CRPlinkProcessor should be initialized once and passed to CRPlinkReplacer on
 * initalization. The Processor expects a plinkTemplate in the CRConfig.
 * 
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class PlinkProcessor {

	private CRConfig config;
	
	Map<String,Resolvable> contextObjects;

	private static Logger log = Logger.getLogger(PlinkProcessor.class);

	private static JCS plinkCache;
	
	public PlinkProcessor(CRConfig config) {

		this.config = config;
		contextObjects = new HashMap<String,Resolvable>();
		if(config.getPortalNodeCompMode())
		{
			//Servlet will run in portal.node compatibility mode => no velocity available
			log.warn("CRPlinkProcessor is running in Portal.Node 3 compatibility mode \n Therefore Velocity scripts will not work in the content.");
		}
		

		try {
			
			plinkCache = JCS.getInstance("gentics-cr-" + this.config.getName()
					+ "-plinks");
			log.debug("Initialized cache zone for \""
					+ this.config.getName() + "-plinks\".");

		} catch (CacheException e) {

			log.warn("Could not initialize Cache for PlinkProcessor.");

		}
	}
	
	public void deployObjects(Map<String,Resolvable> map)
	{
		Iterator<String> it = map.keySet().iterator();
		while(it.hasNext())
		{
			String key = it.next();
			this.contextObjects.put(key, map.get(key));
		}
	}

	public String getLink(PLinkInformation plink, CRRequest request) {

		// starttime
		long start = new Date().getTime();

		String link = "";
		String contentid = plink.getContentId();

		// load link from cache
		if (plinkCache != null) {
			link = (String) plinkCache.get(contentid);
		}

		// no cache object so try to prepare a link
		if (("".equals(link) || link == null )&& !config.getPortalNodeCompMode()) {
			

				// Render Content with contentid as template name
				Resolvable plinkObject;

				try {
					plinkObject = PortalConnectorFactory.getContentObject(contentid, this.config.getDatasource());
				
				
					ITemplateManager myTemplateEngine = this.config.getTemplateManager();
									// Put objects in the plink template
					myTemplateEngine.put("plink", plinkObject);
					//Deploy predefined Objects to the context
					Iterator<String> it = this.contextObjects.keySet().iterator();
					while(it.hasNext())
					{
						String key = it.next();
						myTemplateEngine.put(key, this.contextObjects.get(key));
					}
					// as url is a special object put it also in the templates
					if (this.config.getPathResolver() != null) {
						String url = this.config.getPathResolver().getPath(
								plinkObject);
						if (url != null) {
							myTemplateEngine.put("url", url);
						}
					}
					
					link = myTemplateEngine.render("link", this.config.getPlinkTemplate());

				} catch (DatasourceNotAvailableException e) {
					CRException ex = new CRException(e);
					log.error(ex.getMessage() + ex.getStringStackTrace());
				} catch (CRException ex) {
					log.error(ex.getMessage() + ex.getStringStackTrace());
				}
			// endtime
			long end = new Date().getTime();

			log.debug("plink generationtime for link " + contentid + ": "
					+ (end - start));

		}

		// If velocity template parsing and caching does not work for any
		// reason use a dynamic link
		if ("".equals(link) || link == null) {
			link = this.config.getPathResolver().getDynamicUrl(contentid);
		}

		// add link to cache
		try {

			if (plinkCache != null) {
				plinkCache.put(contentid, link);
			}

		} catch (CacheException e) {

			log.warn("Could not add link to object " + contentid
					+ " to cache");
		}

		return link;
	}
}
