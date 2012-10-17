package com.gentics.cr.lucene.indexer.transformer.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.gentics.api.portalnode.connector.PortalConnectorHelper;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;
import com.gentics.cr.plink.PLinkStripper;

/**
 * HTMLContentTransformer transformers a part/or a full html structure into plain text.
 * Plinks get stripped (replaced with empty strings).
 * For html -> plain text conversion it uses Jsoup.
 * 
 * Last changed: $Date: 2009-06-24 17:10:19 +0200 (Mi, 24 Jun 2009) $
 * @version $Revision: 99 $
 * @author $Author: supnig@constantinopel.at $
 */
public class HTMLContentTransformer extends ContentTransformer {

	/**
	 * Replace all plinks with empty strings (strip them out) as they are not useful in plain text and we don't want to index plinks.
	 */
	private static final PLinkStripper stripper = new PLinkStripper();;

	/**
	 * Attribute specifying through the config which field to process. In most cases content.   
	 */
	public static final String TRANSFORMER_ATTRIBUTE_KEY = "attribute";

	/**
	 * Field to process set through the config. 
	 */
	private String attribute = "";

	/**
	 * Get new instance of HTMLContentTransformer.
	 * @param config needed to specifiy which field to use.
	 */
	public HTMLContentTransformer(final GenericConfiguration config) {
		super(config);
		attribute = (String) config.get(TRANSFORMER_ATTRIBUTE_KEY);
	}

	/**
	 * Converts a string containing html to a String that does not contain html tags can be indexed by lucene.
	 * @param contentObject
	 * @return
	 */
	private String getStringContents(final Object contentObject) throws CRException {
		StringBuilder plainTextString = new StringBuilder();
		String htmlcontent = getContents(contentObject);
		try {
			if (htmlcontent != null) {
				Document d = Jsoup.parseBodyFragment(htmlcontent);

				for (Element n : d.body().children()) {
					plainTextString.append(n.text());
					plainTextString.append(" ");
				}
			}
		} catch (Exception ex) {
			throw new CRException(ex);
		}
		return plainTextString.toString();
	}

	/**
	 * Converts a object containing html to a String that does not contain html tags can be indexed by lucene.
	 * @param contentObject
	 * @return HTMLStripReader of contents
	 */
	private String getContents(final Object contentObject) {
		String contents = null;
		if (contentObject instanceof String) {
			contents = PortalConnectorHelper.replacePLinks((String) contentObject, stripper);
		} else {
			throw new IllegalArgumentException();
		}
		return contents;
	}

	@Override
	public void processBean(final CRResolvableBean bean) throws CRException {
		if (this.attribute != null) {
			Object obj = bean.get(this.attribute);
			if (obj != null) {
				String newString = getStringContents(obj);
				if (newString != null) {
					bean.set(this.attribute, newString);
				}
			}
		} else {
			LOGGER.error("Configured attribute is null. Bean will not be processed");
		}
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}
}
