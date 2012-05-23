package com.gentics.cr.lucene.indexer.transformer;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.plink.LucenePathResolver;

/**
 * Converts links within the provided content to plinks.
 * Existing plinks are ignored by the class.
 * 
 * Last changed: $Date: 2009-06-24 17:10:19 +0200 (Mi, 24 Jun 2009) $
 * @version $Revision: 99 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class LinkToPlinkTransformer extends ContentTransformer {

	/**
	 * Log4j logger.
	 */
	private static Logger log = Logger.getLogger(LinkToPlinkTransformer.class);
	
	/**
	 * Config identifier to set the attribute for content parsing.
	 */
	private static final String ATTRIBUTE_KEY = "attribute";

	/**
	 * Default value for attribute to use for content parsing.
	 */
	private String attribute = "content";
	
	/**
	 * Config identifier to set the static prefix in the config.
	 */
	private static final String STATIC_PREFIX_KEY = "staticprefix";
	
	/**
	 * Config identifier to set the hostprefix in the config.
	 */
	private static final String HOST_PREFIX_KEY = "hostprefix";

	
	/**
	 * Find all links (href, src) within the content.
	 */
	private Pattern plinkResolverPattern = Pattern.compile("(href|src)=\"([^\"]+)\"");
	
	/**
	 * Filter all parameter from the url (?contentid).
	 */
	private Pattern parameterPattern = Pattern.compile("(#|\\?)(.*)");
	
	private Pattern excludeHostPattern = Pattern.compile("^([a-zA-Z][a-zA-Z0-9^:]*:|#|<plink)");

	/**
	 * configuration for the LinkToPlinkTransformer.
	 * provides staticprefix, hostprefix, rule, searchconfig, indexconfig, attribute to use for content
	 */
	private CRConfigUtil config;
	
	private LucenePathResolver pr;
	
	/**
	 * e.g.: /Portal.Node/public/content/ .
	 */
	private String staticprefix = null;
	
	/**
	 * e.g.: https://www.test.at .
	 */
	private String hostprefix = null;

	/**
	 * Create new Instance.
	 * @param config
	 */
	public LinkToPlinkTransformer(final GenericConfiguration config) {
		super(config);
		this.config = new CRConfigUtil(config, "link_to_plinktransformer");
		String attString = config.getString(ATTRIBUTE_KEY);
		if (attString != null) {
			this.attribute = attString;
		}
		staticprefix = this.config.getString(STATIC_PREFIX_KEY);
		hostprefix = this.config.getString(HOST_PREFIX_KEY);

		pr = new LucenePathResolver(this.config);

	}

	@Override
	public void processBean(final CRResolvableBean bean) throws CRException {
		String content = (String) bean.get(this.attribute);
		if (content != null) {
			// starttime
			long s = new Date().getTime();
			String replacement = resolveLinks(content);
			bean.set(this.attribute, replacement);
			long e = new Date().getTime();
			log.debug("Resolving static URLs took " + (e - s) + "ms");
		}
	}

	/**
	 * Resolve absolute urls in content to plinks. Plinks are ignored as the regex doesn't capture those.
	 * @param content for parsing.
	 * @return complete url
	 */
	private String resolveLinks(final String content) {
		Matcher matcher = plinkResolverPattern.matcher(content);
		StringBuffer buf = new StringBuffer();
		while (matcher.find()) {
			String linkform = matcher.group(1);
			String link = matcher.group(2);
			String staticlink = link;
			if (this.staticprefix != null) {
				link = link.replaceFirst(this.staticprefix, "");
			}

			//EXTRACT PARAMETERS
			Matcher parammatcher = parameterPattern.matcher(link);
			String parameters = "";
			if (parammatcher.find()) {
				StringBuffer lpBuff = new StringBuffer();
				parameters = parammatcher.group(1) + parammatcher.group(2);
				parammatcher.appendReplacement(lpBuff, "");
				parammatcher.appendTail(lpBuff);
				link = lpBuff.toString();
			}

			//RESOLVE CONTENTID
			CRResolvableBean bean = null;
			if (link != null && !"".equals(link) && !link.startsWith("http://")) {
				CRRequest r = new CRRequest();
				r.setUrl(link);
				bean = this.pr.getObject(r);
				if (bean != null) {
					String contentid = bean.getContentid();
					link = "<plink id=\"" + contentid + "\">";
				} else {
					// this warning may be triggered pretty often in case of plink usage in the content
					log.warn("Could not resolve internal link: " + link);
				}
				log.warn("link is not null and either it is an absolute url or a plink has been created for it");
			}

			//ADD PARAMETERS
			link += parameters;
			//PREFIX HOST
			Matcher hostMatcher = excludeHostPattern.matcher(link);
			if (!hostMatcher.find()) {
				link = this.hostprefix + staticlink;
			}
			//REAPPEND FINISHED LINK
			//we need to escape $ as this kills the expression
			matcher.appendReplacement(buf, linkform + "=\"" + link.replace("$", "\\$") + "\"");
		}
		matcher.appendTail(buf);
		return buf.toString();
	}

	@Override
	public void destroy() {
		this.pr.destroy();

	}

}
