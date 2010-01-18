package com.gentics.cr.exceptions;

/**
 * ConfigurationException is thrown if there is an error in the Configuration
 * @author perhab
 *
 */
public class ConfigurationException extends CRException {
	
	/**
	 * UniqueIdentifier
	 */
	private static final long serialVersionUID = -6544207321495879531L;

	/**
	 * default constructor for ConfigurationException this is a wrapper for {@link CRException#CRException(String, String, com.gentics.cr.exceptions.CRException.ERRORTYPE)} 
	 * @param newtype
	 * @param newmessage
	 * @param type
	 */
	public ConfigurationException(String newtype, String newmessage, ERRORTYPE type){
		super(newtype,newmessage,type);
	}
}
