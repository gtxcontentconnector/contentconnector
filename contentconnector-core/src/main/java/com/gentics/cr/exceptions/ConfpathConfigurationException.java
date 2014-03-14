package com.gentics.cr.exceptions;
/**
 * This exception is thrown when the system property
 * com.gentics.portalnode.confpath is not configured correctly
 * 
 * @author Sebastian Vogel <s.vogel@gentics.com>
 */
public class ConfpathConfigurationException extends RuntimeException {
	/**
	 * auto generated serialVersionUID
	 */
	private static final long serialVersionUID = 1944153187045237753L;
	
	public ConfpathConfigurationException(String message) {
		super(message);	
	}
	
	public ConfpathConfigurationException(Throwable cause) {
		super(cause);
	}
	
	public ConfpathConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}
}
