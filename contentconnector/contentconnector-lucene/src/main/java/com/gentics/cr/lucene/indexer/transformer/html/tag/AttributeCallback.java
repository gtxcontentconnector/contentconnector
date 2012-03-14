package com.gentics.cr.lucene.indexer.transformer.html.tag;

/**
 * AttributeCallback is used to perform changes on the HTML source of a tag when it the expression configured
 * for the {@link TagTransformer} changes the value of an attribute.
 * @author bigbear3001
 *
 */
public interface AttributeCallback {
	/**
	 * Perform the changes in the HTML code on the attribute identified by name.
	 * @param html - HTML code to change
	 * @param attributeName - name of the attribute to change
	 * @param attributeValue - value of the attribute to change to
	 */
	void invokeCallback(final StringBuffer html, final String attributeName,
			final String attributeValue);
}
