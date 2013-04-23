package com.gentics.cr.template;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.gentics.cr.exceptions.CRException;

/**
 * loads a template from a string.
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 545 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class StringTemplate implements ITemplate {

	/**
	 * Template source.
	 */
	private String source;

	/**
	 * Template key.
	 */
	private String key;

	/**
	 * gets the key of the template. usually a md5 hash
	 * @return key
	 */
	public final String getKey() {
		return this.key;
	}

	/**
	 * gets the source of the template.
	 * @return source
	 */
	public final String getSource() {
		return this.source;
	}

	/**
	 * Creates a new instance of FileTemplate.
	 * @param template - velocity code of the template
	 * @throws CRException if the MD5 algorithm is not found for generating the
	 * key.
	 */
	public StringTemplate(final String template) throws CRException {
		try {
			this.source = template;
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(this.source.getBytes());
			this.key = new String(digest.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new CRException(e);
		}
	}

}
