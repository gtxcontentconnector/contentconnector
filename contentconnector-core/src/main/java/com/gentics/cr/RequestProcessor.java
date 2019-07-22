package com.gentics.cr;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gentics.api.lib.cache.PortalCache;
import com.gentics.api.lib.cache.PortalCacheException;
import org.apache.log4j.Logger;

import com.gentics.api.lib.etc.ObjectTransformer;
import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.api.portalnode.connector.PortalConnectorHelper;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.plink.PathResolver;
import com.gentics.cr.plink.PlinkProcessor;
import com.gentics.cr.plink.PlinkReplacer;
import com.gentics.cr.template.ITemplateManager;

/**
 * 
 * Last changed: $Date: 2010-04-01 15:24:02 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 541 $
 * @author $Author: supnig@constantinopel.at $
 */
public abstract class RequestProcessor {

	/**
	 * configuration key for enabling (default) or disabling the JCS content cache in a RequestProcessor.
	 */
	public static final String CONTENTCACHE_KEY = "crcontentcache";

	protected HashMap<String, Resolvable> resolvables = null;

	protected CRConfig config = null;

	protected PlinkProcessor plinkProc = null;

	private static Logger log = Logger.getLogger(RequestProcessor.class);

	private PortalCache cache;

	private String cacheRegionKey = null;

	/**
	 * If the contentcache is activated in the config, the cache region key will be returned.
	 * When contentcache is not active this method returns null.
	 * @return the cache region key or null when contentcache is not active
	 */
	public String getCacheRegionKey() {
		return cacheRegionKey;
	}

	/**
	 * KEY to store switch for metaresolvable in request.
	 * if request.get(LuceneRequestProcessor.META_RESOLVABLE_KEY) returns "true", a metaresolvable will be generated
	 */
	public static final String META_RESOLVABLE_KEY = "metaresolvable";

	/**
	 * Key where to find the query used for highlighting the content. Usually this is the 
	 * searchqery without the permissions and meta search informations.
	 * If this is not set, the requestFilter (default query) will be used
	 */
	public static final String HIGHLIGHT_QUERY_KEY = "highlightquery";

	/**
	 * finds all links in href="" and src="" attributes.
	 * group 1: contains text before the link
	 * group 2: contains the link
	 * group 3: contains text after the link
	 */
	protected static final Pattern LINKS_PATTERN = Pattern
			.compile("(?s:(<(?:a|area|img)[^>]+?(?:href|src)=[\'\"])(/.*?)([\'\"]))");

	/**
	 * Create new instance of RequestProcessor.
	 * @param config Configuration of the RequestProcessor
	 * @throws CRException if no config is given, or the PlinkProcessor could not
	 * be initialized.
	 */
	public RequestProcessor(final CRConfig config) throws CRException {
		this.config = config;
		this.plinkProc = new PlinkProcessor(config);

		if (config == null) {
			throw new CRException("CONFIG", "Config is NULL => overthink your config!",
					CRException.ERRORTYPE.FATAL_ERROR);
		}
		if (config.getPortalNodeCompMode()) {
			//Servlet will run in portal.node compatibility mode => no velocity available
			log.warn("CRRequestProcessor is running in Portal.Node 3 compatibility mode \n" + ""
					+ "Therefore Velocity scripts will not work in the content.");
		}

		if (config.getBoolean(CONTENTCACHE_KEY, true)) {
			this.cacheRegionKey = "gentics-cr-" + config.getName() + "-crcontent";
			try {
				cache = PortalCache.getCache(this.cacheRegionKey);
				log.debug("Initialized cache zone for \"" + config.getName() + "-crcontent\".");
			} catch (PortalCacheException e) {
				log.warn("Could not initialize Cache for PlinkProcessor.");
				throw new CRException(e);
			}
		}

	}
	
	/**
	 * Get the configuration for this RP.
	 * @return
	 */
	public CRConfig getConfig() {
		return this.config;
	}
	
	/**
	 * Get the cache region of the request processor.
	 * @return
	 */
	public PortalCache getCache() {
		return cache;
	}

	/**
	 * Replace the plinks in an CRResolvableBean using the plinkAttributes in the CRRequest.
	 * @param crBean
	 * @param request
	 * @return CRResolvableBean with replaced Plinks
	 */
	public CRResolvableBean replacePlinks(final CRResolvableBean crBean, final CRRequest request) {
		String[] plinkAttrArray = request.getPlinkAttributeArray();
		if (plinkAttrArray != null) {

			for (int i = 0; i < plinkAttrArray.length; i++) {
				Object attr = crBean.get(plinkAttrArray[i]);
				if (attr instanceof String) {
					crBean.set(
						plinkAttrArray[i],
						PortalConnectorHelper.replacePLinks((String) attr, new PlinkReplacer(this.plinkProc, request)));
				}
			}
		}
		return crBean;
	}

	/**
	 * Fetch the first matching object using the given CRRequest.
	 * @param request
	 * @return first matching object
	 * @throws CRException
	 */
	public CRResolvableBean getFirstMatchingResolvable(final CRRequest request) throws CRException {
		request.setCountString("1");
		Collection<CRResolvableBean> coll = getObjects(request);
		if (coll != null && !coll.isEmpty()) {
			Iterator<CRResolvableBean> bIt = coll.iterator();
			if (bIt.hasNext()) {
				return bIt.next();
			}
		}

		return null;
	}

	/**
	 * Get the matching objects using the given CRRequest.
	 * @param request
	 * @return Collection of CRResolvableBeans
	 * @throws CRException
	 */
	public Collection<CRResolvableBean> getObjects(final CRRequest request) throws CRException {
		return (this.getObjects(request, false));
	}

	/**
	 * Get the matching objects using the given CRRequest.
	 * @param request
	 * @param doNavigation defines if to fetch child elements using child rule
	 * @return Collection of CRResolvableBeans
	 * @throws CRException TODO javadoc
	 */
	public abstract Collection<CRResolvableBean> getObjects(CRRequest request, boolean doNavigation) throws CRException;

	/**
	 * Get the matching objects and sub objects using the given CRRequest.
	 * @param request TODO javadoc
	 * @return Collection of CRResolvableBeans
	 * @throws CRException TODO javadoc
	 */
	public Collection<CRResolvableBean> getNavigation(final CRRequest request) throws CRException {
		return this.getObjects(request, true);
	}

	/**
	 * Fetch the content bean using an url.
	 * @param request needed for retreiving the url.
	 * @return Returns a object bean for binary output from a url.
	 * @throws CRException 
	 */
	public CRResolvableBean getContentByUrl(final CRRequest request) throws CRException {
		Resolvable reso = getBeanByURL(request);
		if (reso == null) {
			log.error("Cannot get Object for path '" + request.getUrl() + "'");
			return null;
		} else {
			return getContent(new CRResolvableBean(reso), request);
		}
	}

	/**
	 * Returns an object bean by url.
	 * @param request request.
	 * @return resolvable bean or null if not found.
	 */
	public final CRResolvableBean getBeanByURL(final CRRequest request) {
		CRResolvableBean reso = null;
		PathResolver pr = this.config.getPathResolver();

		if (pr != null) {
			reso = pr.getObject(request);
		} else {
			log.warn("Could not get Pathresolver to resolve path '" + request.getUrl() + "'.");
		}
		return reso;
	}

	/**
	 * Returns a object bean for binary output from a contentid.
	 * @param request 
	 * @return first object found matching the request
	 * @throws CRException 
	 */
	public CRResolvableBean getContent(CRRequest request) throws CRException {
		CRResolvableBean reso = null;

		reso = this.getFirstMatchingResolvable(request);
		return getContent(reso, request);
	}

	/**
	 * Returns a object bean for binary output from a resolvable.
	 * 
	 * @param reso
	 * @param attribute
	 * @param mimetypeAttribute
	 * @param doReplacePlinks
	 * @param doVelocity
	 * @return
	 */
	private CRResolvableBean getContent(Resolvable reso, CRRequest request) throws CRException {

		//TODO Use content renderer provided by NOPs
		boolean doReplacePlinks = request.getDoReplacePlinks();
		String prefixHostRelativeLinks = ObjectTransformer.getString(
			config.get(CRRequest.PREFIX_HOST_RELATIVE_LINKS),
			null);
		boolean doVelocity = request.getDoVelocity();

		CRResolvableBean crBean = null;

		String attribute = "content";
		String mimetypeAttribute = "mimetype";

		if (reso != null) {
			String contentid = (String) reso.get("contentid");

			// load element from cache
			if (cache != null) {
				Object obj = null;
				try {
					obj = cache.get(contentid);
				} catch (PortalCacheException e) {
					;
				}
				if (obj != null) {
					if (obj instanceof CRResolvableBean) {
						crBean = (CRResolvableBean) obj;
						log.debug("Loaded from cache: " + crBean.getContentid());
					} else {
						log.debug("Loaded from cache: " + obj.toString());
					}
				}

			}
			if (crBean == null) {
				String binType = this.config.getBinaryType();
				if (binType != null) {
					if (binType.equals("" + reso.get("obj_type"))) {
						attribute = "binarycontent";
					} else if (binType.equals("all")) {
						attribute = "binarycontent";
					}
				} else {
					//If binary type is not set in the config => fall back to 10008
					if ("10008".equals("" + reso.get("obj_type"))) {
						attribute = "binarycontent";
					}
				}

				// When attribute is specified return as binary.
				crBean = new CRResolvableBean(reso, new String[] { attribute, mimetypeAttribute });
				log.debug("Can't load from cache => direct access");
			}

			Object o = reso.get(attribute);

			// This is quite ugly!
			String s = "";
			byte[] b = null;

			if (o != null) {
				if (o instanceof String) {
					s = (String) o;
					if (prefixHostRelativeLinks != null) {
						s = prefixHostRelativeLinks(s, prefixHostRelativeLinks);
					}
					if (doReplacePlinks) {
						// starttime
						long start = new Date().getTime();

						// replace plinks
						s = PortalConnectorHelper.replacePLinks(s, new PlinkReplacer(this.plinkProc, request));

						// endtime
						long end = new Date().getTime();
						log.debug("plink parsing time for attribute " + attribute + " of " + contentid + ": "
								+ (end - start));
					}

					if (doVelocity && !config.getPortalNodeCompMode()) {
						// Initialize Velocity Context
						ITemplateManager myTemplateManager = config.getTemplateManager();
						myTemplateManager.put("connector", "Gentics REST API");

						// enrich template context
						if (this.resolvables != null) {
							for (Iterator<Map.Entry<String, Resolvable>> it = this.resolvables.entrySet().iterator(); it
									.hasNext();) {
								Map.Entry<String, Resolvable> entry = it.next();
								myTemplateManager.put(entry.getKey(), entry.getValue());
							}
						}
						try {
							s = myTemplateManager.render("attribute", s);
						} catch (Exception e) {
							throw new CRException(e);
						}
					}
				} else if (o.getClass() == Integer.class) {
					s = ((Integer) o).toString();
				} else if (o.getClass() == java.sql.Timestamp.class) {
					s = ((Timestamp) o).toString();
				} else {
					b = (byte[]) o;
				}

				// add result to cache
				try {
					if (cache != null) {
						cache.put(contentid, crBean);
					}
				} catch (PortalCacheException e) {
					log.warn("Could not add crBean object " + crBean.getContentid() + " to cache", e);
					throw new CRException(e);
				}
			}

			// the returned attribute is always a byte[] Array
			if (b != null) {
				crBean.set(attribute, b);
			} else {
				crBean.set(attribute, s);
			}
		}

		return crBean;
	}

	/**
	 * prefixes all host-relative links in source with the given prefix.
	 */
	public static String prefixHostRelativeLinks(String source, String prefix) {
		Matcher matcher = LINKS_PATTERN.matcher(source);
		StringBuffer sb = new StringBuffer(source.length());

		while (matcher.find()) {
			String link = matcher.group(2);
			//if (link.charAt(0) == '/') {
			matcher.appendReplacement(sb, "$1" + prefix + link + "$3");
			//} else {
			//	matcher.appendReplacement(sb, "$1$2$3");
			//}
		}
		matcher.appendTail(sb);

		return sb.toString();
	}

	/**
	 * Converts a Collection of Resolvables to a parameterized collection of Resolvables.
	 * @param col
	 * @return Collection parameterized to Resolvables
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Collection<Resolvable> toResolvableCollection(final Collection col) {
		Collection<Resolvable> returnCollection = (Collection<Resolvable>) col;
		return returnCollection;
	}

	/**
	 * Converts a Object being an instance of a Collection of Resolvables to a parameterized collection of Resolvables.
	 * @param col
	 * @return  Collection parameterized to Resolvables
	 */
	@SuppressWarnings("unchecked")
	protected Collection<Resolvable> toResolvableCollection(final Object col) {
		if (col instanceof Collection) {
			return (Collection<Resolvable>) col;
		}
		throw new IllegalArgumentException("You have to pass an instance of Collection containing Resolvables");
	}

	/**
	 * Converts a Object being an instance of a Collection of CRResolvableBeans 
	 * to a parameterized collection of CRResolvableBeans.
	 * @param col
	 * @return either this methods throws an IllegalArgumentException or returns a collection of Collection&lt;CRResolvableBean&gt;
	 */
	@SuppressWarnings("unchecked")
	protected Collection<CRResolvableBean> toCRResolvableBeanCollection(Object col) {
		if (col instanceof Collection) {
			return (Collection<CRResolvableBean>) col;
		}

		throw new IllegalArgumentException("You have to pass an instance of Collection containing CRResolvableBeans");
	}

	/**
	 * Converts a Collection of CRResolvableBeans to a parameterized collection of CRResolvableBeans.
	 * @param col
	 * @return Collection parameterized to CRResolvableBean
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Collection<CRResolvableBean> toCRResolvableBeanCollection(Collection col) {
		return (Collection<CRResolvableBean>) col;
	}

	
	/**
	 * Fills the attributes defined in the requests attribute array to each element of the collection col.
	 * @param col
	 * @param request
	 * @param idAttribute 
	 * @throws CRException 
	 */
	public void fillAttributes(Collection<CRResolvableBean> col, CRRequest request, String idAttribute)
			throws CRException {
		RequestProcessorMerger.fillAttributes(this, col, request, idAttribute);
	}

	/**
	 * Fills the attributes defined in the requests attribute array to each element of the collection col.
	 * @param col
	 * @param request
	 * @throws CRException 
	 */
	public void fillAttributes(Collection<CRResolvableBean> col, CRRequest request) throws CRException {
		fillAttributes(col, request, "contentid");
	}

	public abstract void finalize();

}
