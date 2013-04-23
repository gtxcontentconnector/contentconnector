package com.gentics.cr.exceptions;

/**
 * ConfigurationException is thrown if there is an error in the Configuration.
 * @author perhab
 *
 */
public class ConfigurationException extends CRException {

	/**
	 * generated serial version uid.
	 */
	private static final long serialVersionUID = -6544207321495879531L;

	/**
	 * default constructor for ConfigurationException, this is a wrapper for
	 * {@link CRException#CRException(String, String,
	 * com.gentics.cr.exceptions.CRException.ERRORTYPE)}.
	 * @param newtype type of the exception
	 * @param newmessage message of the exception
	 * @param type type of the exception
	 */
	public ConfigurationException(final String newtype, final String newmessage, final ERRORTYPE type) {
		super(newtype, newmessage, type);
	}
}
