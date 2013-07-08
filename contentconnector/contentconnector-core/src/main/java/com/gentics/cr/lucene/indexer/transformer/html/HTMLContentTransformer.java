package com.gentics.cr.lucene.indexer.transformer.html;

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import com.gentics.api.portalnode.connector.PortalConnectorHelper;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;
import com.gentics.cr.plink.PLinkStripper;

/**
 * HTMLContentTransformer transformers a part/or a full html structure into
 * plain text. Plinks get stripped (replaced with empty strings). For html ->
 * plain text conversion it uses Jsoup. It tries as good as it can but it will only strip
 * valid html.
 * 
 * Last changed: $Date: 2009-06-24 17:10:19 +0200 (Mi, 24 Jun 2009) $
 * @version $Revision: 99 $
 * @author $Author: supnig@constantinopel.at $
 */
public class HTMLContentTransformer extends ContentTransformer {

	/**
	 * Replace all plinks with empty strings (strip them out) as they are not
	 * useful in plain text and we don't want to index plinks.
	 */
	private static final PLinkStripper stripper = new PLinkStripper();

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
	 * Converts a string containing html to a String that does not contain html
	 * tags can be indexed by lucene.
	 */
	private String getStringContents(final Object contentObject) throws CRException {
		String htmlString = getContents(contentObject);
		String result = "";
		try {
			if (htmlString != null) {
				Document documentFragment = Jsoup.parseBodyFragment(htmlString);
				result = retrieveContent(documentFragment);

				if (result.equals("")) {
					String fragmentText = documentFragment.text();
					if (fragmentText != null) {
						result = fragmentText;
					}
				}
			}
		} catch (Exception ex) {
			throw new CRException(ex);
		}
		return result;
	}

	private String retrieveContent(final Document documentFragment) {
		StringBuilder strippedString = new StringBuilder();
		List<Node> children = documentFragment.body().childNodes();
		for (int pos = 0; pos < children.size(); pos++) {
			Node node = children.get(pos);
			String text = getTextFromNode(node);
			if (!text.equals("")) {
				String tempString = strippedString.toString();
				if (tempString.equals("") || tempString.endsWith(" ") || tempString.endsWith("/") || tempString.endsWith(".")
						|| tempString.endsWith(":")) {
					// directly add the text if the string is empty or ends with a space, slash, dot, doublepoint
					strippedString.append(text);
					
				} else {
					// check next element if a space shall be added
					if (checkNextElement(children, pos)) {
						strippedString.append(" ");
					}
					strippedString.append(text);
				}
				if(!text.endsWith(" ") && node instanceof Element && ((Element) node).tag().isBlock()) {
					// make sure there is always a space at the end of block elements!
					strippedString.append(" ");
				} 
			}
		}
		return strippedString.toString().trim();
	}

	private boolean checkNextElement(List<Node> children, int pos) {
		if (pos + 1 < children.size()) {
			return true;
		}
		return false;
	}

	private String getTextFromNode(Node node) {
		String text = "";
		if (node instanceof Element) {
			text = ((Element) node).text();
		} else if (node instanceof TextNode) {
			text = ((TextNode) node).text();
		}
		return text;
	}

	/**
	 * Converts a object containing html to a String that does not contain html
	 * tags can be indexed by lucene.
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
				if (newString == null || newString.equals("")) {
					LOGGER.debug("Original String was: \"" + obj.toString() + "\" - Stripped everything and returning an empty string");
				}
				bean.set(this.attribute, newString);
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
