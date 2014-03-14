package com.gentics.cr.lucene.indexer.transformer.html.tag;

/**
 * Replaces an existing attribute in the HTML code.
 * @author bigbear3001
 *
 */
public class ReplaceAttributeCallback implements AttributeCallback {

	/**
	 * {@inheritDoc}
	 */
	public final void invokeCallback(final StringBuffer html, final String attributeName, final String attributeValue) {
		html.replace(
			0,
			html.length(),
			html.toString().replaceAll(
				"(?i)" + attributeName + "=\"?[^\"]*\"?",
				attributeName + "=\"" + attributeValue + "\""));
	}

}
