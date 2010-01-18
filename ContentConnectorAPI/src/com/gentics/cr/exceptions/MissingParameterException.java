package com.gentics.cr.exceptions;

import com.gentics.cr.CRConfig;

/**
 * Exception to use for if a parameter is missing
 * @author perhab
 *
 */
public class MissingParameterException extends ConfigurationException {

	/**
	 * UniqueIdentifier
	 */
	private static final long serialVersionUID = -1431840546323718286L;

	/**
	 * Exception if a parameter is missing
	 * @param missingParameterName name of the missing parameter as String
	 */
	public MissingParameterException(String missingParameterName, CRConfig config){
		super("MISSINGPARAMETER","The parameter with the name " + config.getName() + missingParameterName + " is missing.",CRException.ERRORTYPE.FATAL_ERROR);
	}
	
}
