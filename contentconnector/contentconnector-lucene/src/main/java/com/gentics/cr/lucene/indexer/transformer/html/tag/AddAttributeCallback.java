package com.gentics.cr.lucene.indexer.transformer.html.tag;

/**
 * Adds an attribute if it doesn't exist yet.
 * @author bigbear3001
 *
 */
public class AddAttributeCallback implements AttributeCallback {
	/**
	 * {@inheritDoc}
	 */
	public final void invokeCallback(final StringBuffer html, final String attributeName, final String attributeValue) {
		html.insert(html.indexOf(">"), " " + attributeName + "=\"" + attributeValue + "\"");
	}

}
