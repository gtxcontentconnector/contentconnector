package com.gentics.cr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.apache.log4j.Logger;

import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.api.portalnode.action.PluggableActionResponse;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.util.PluggableActionCaller;

/**
 * 
 * Last changed: $Date: 2010-04-01 15:24:02 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 541 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class XMLRequestProcessor extends RequestProcessor {

	private static Logger log = Logger.getLogger(XMLRequestProcessor.class);

	private static JCS cache;

	/**
	 * Create new instance of XMLRequestProcessor
	 * @param config CRConfig with the following attributes:
	 * 		- xmlUrl: URL where i can load the XML file, u can use a place holder #query# for search queries. The place holder is replaced in getObjects method.
	 *		- xsltUrl: URL where i can load the XSLT file from. The xslt file is used to parse the xmlsource in a compatible format for CreateResolveablesAction. See http://www.gentics.com/help/topic/com.gentics.portalnode.sdk.doc/misc/doc/reference/xhtml/ref-implementation-plugins.html#reference.implementation.plugins.viewplugin.pluggableactions.createresolvablesaction for details.
	 * 		- contentidRegex: Regular expression to filter out contentids from resolvables contentid attribute which is an url in most cases. The attribute contentid ist replaced by the first selector in the regex. If you want the whole attribute just use '(.*)'.
	 * @throws CRException
	 */
	public XMLRequestProcessor(CRConfig config) throws CRException {
		super(config);
		if (config.getBoolean(RequestProcessor.CONTENTCACHE_KEY, true)) {
			try {
				cache = JCS.getInstance("gentics-cr-" + config.getName() + "-content");
			} catch (CacheException e) {
				log.error("Cannot instanciate cache region.", e);
			}
		}
	}

	/**
	 * get resolvables from given XmlUrl
	 * @param request CRRequest with the following attributes set
	 * 		-  requestFilter: the variable #query# is replaced in the XmlUrl by requestFilter
	 * @param doNavigation only for compatibility has no functional impact. The XMLRequestProcessor is used to parse search results. They cannot be used as Navigation only as List
	 * @throws CRException in case no filter is given a CRException is thrown.
	 * @return Collection of CRResolvableBeans parsed from the xmlsource
	 */
	public Collection<CRResolvableBean> getObjects(CRRequest request, boolean doNavigation) throws CRException {
		Collection<Resolvable> collection;
		Collection<CRResolvableBean> crCollection;
		String query = request.getRequestFilter();
		if (query == null) {
			log.error("Request without request filter. Please give me a parameter filter.");
			throw new CRException();
		}
		//Create search url, get XML Code from search parse it with XSLT and create Resolvables out of the code
		// TODO: make single steps optional (users shouldn't have to define a xslt or contentidfilter)
		String url = this.config.getXmlUrl().replaceAll("#query#", query);
		String code = getCleanUnicodeXML(getCode(url));
		String xslt = getCode(this.config.getXsltUrl());
		String xml = parseXslt(code, xslt);
		String contentid_regex = this.config.getContentidRegex();
		collection = getResolveables(xml);
		crCollection = new ArrayList<CRResolvableBean>(collection.size());
		Iterator<Resolvable> colIterator = collection.iterator();
		while (colIterator.hasNext()) {
			Resolvable item = colIterator.next();
			CRResolvableBean crItem = new CRResolvableBean(item, request.getAttributeArray());
			if (contentid_regex != null)
				crItem.set("contentid", ((String) crItem.get("contentid")).replaceAll(contentid_regex, "$1"));
			crCollection.add(crItem);
		}
		return crCollection;
	}

	/**
	 * get resovleables from given xml code
	 * @param xmlCode must be compatible with: http://www.gentics.com/help/topic/com.gentics.portalnode.sdk.doc/misc/doc/reference/xhtml/ref-implementation-plugins.html#reference.implementation.plugins.viewplugin.pluggableactions.createresolvablesaction
	 * @return collection of resolvables
	 */
	private Collection<Resolvable> getResolveables(String xmlCode) {
		HashMap<String, Object> parameters = new HashMap<String, Object>(1);
		parameters.put("xml", xmlCode);
		PluggableActionResponse response = PluggableActionCaller.call("CreateResolvablesAction", parameters);
		return this.toResolvableCollection(response.getParameter("result"));
	}

	/**
	 * transform xml code with given xslt code
	 * @param xmlCode xml code to transform
	 * @param xsltCode xslt code to use 
	 * @return transformed xml code
	 */
	private String parseXslt(String xmlCode, String xsltCode) {
		HashMap<String, Object> parameters = new HashMap<String, Object>(2);
		parameters.put("xmlsource", xmlCode);
		parameters.put("xslt", xsltCode);
		PluggableActionResponse response = PluggableActionCaller.call("XSLTRenderAction", parameters);
		return (String) response.getParameter("content");
	}

	/**
	 * Remove not unicode compatible characters from XML to prevent XML parsers from errors. This method is useful if you use webservices that give you xml and don't understand xml. This webservices often use templates to generate xml and not a xml generator.
	 * @param xmlCode XML code should be cleaned up
	 * @return clean XML code  
	 */
	private String getCleanUnicodeXML(String xmlCode) {
		//TODO: at the moment incompatible characters are only removed with a regular expression. We should check the syntax to in the future. E.g. to add CDATAs if we need them.
		return xmlCode.replaceAll("[^\\p{L}\\p{Print}\\r\\n]", ".");
	}

	/**
	 * Fetch code from given URL. Use JCSCache to cache results of URL
	 * @param url URL to load code from.
	 * @return string with code of URL, null in case we got an error (timeout)
	 */
	private String getCode(String url) {
		log.debug("Get code of: " + url);
		String content;
		content = (String) cache.get(url);
		if (content == null) {
			HashMap<String, Object> parameters = new HashMap<String, Object>(2);
			parameters.put("url", url);
			//TODO: allow to set User the timeouts as described in: http://www.gentics.com/help/topic/com.gentics.portalnode.sdk.doc/misc/doc/reference/xhtml/ref-implementation-plugins.html#reference.implementation.plugins.viewplugin.pluggableactions.urlloaderaction
			parameters.put("readTimeout", 300);
			PluggableActionResponse response = PluggableActionCaller.call("URLLoaderAction", parameters);
			content = (String) response.getParameter("content");
			try {
				cache.put(url, content);
			} catch (CacheException e) {
				log.error("Error putting code of " + url + " in cache");
				e.printStackTrace();
			}
			log.debug("Got code of " + url);
		} else
			log.debug("Got code of " + url + " from cache");
		return content;
	}

	@Override
	public void finalize() {
		// TODO Auto-generated method stub

	}

}
