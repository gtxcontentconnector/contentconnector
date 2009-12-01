package com.gentics.cr;

import java.io.Serializable;

/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class CRError implements Serializable {
	
	
	private static final long serialVersionUID = -4462449550837801315L;
	private String type;
	private String message;
	private String stringStackTrace;
	private CRException.ERRORTYPE errtype = CRException.ERRORTYPE.GENERAL_ERROR;
	
	/**
	 * sets the ERROR Type
	 * @param type
	 */
	public void setErrorType(CRException.ERRORTYPE type)
	{
		this.errtype = type;
	}
	
	/**
	 * gets the ERROR type
	 * @return
	 */
	public CRException.ERRORTYPE getErrorType()
	{
		return(this.errtype);
	}
	
	/**
	 * get Error Type
	 * @return
	 */
	public String getType() {
		return type;
	}

	/**
	 * sets Error Type
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * gets the Error Message, usually the message represented by the Exception thrown
	 * @return
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * sets the Error Message, usually the message represented by the Exception thrown
	 * @param message
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * gets StackTrace as String. Only for output and logging.
	 * @return
	 */
	public String getStringStackTrace() {
		return stringStackTrace;
	}

	/**
	 * sets StackTrace as String. Only for output and logging. 
	 * @param stringStackTrace
	 */
	public void setStringStackTrace(String stringStackTrace) {
		this.stringStackTrace = stringStackTrace;
	}
	
	/**
	 * Default Contnstructor for Serialization
	 */
	public CRError()
	{
		
	}

	/**
	 * create new CRError
	 * @param e
	 */
	public CRError(CRException e)
	{
		this.type = e.getType();
		this.message = e.getMessage();
		this.stringStackTrace = e.getStringStackTrace();
		this.errtype = e.getErrorType();
	}
	
	/**
	 * create new CRError
	 * @param type
	 * @param message
	 */
	public CRError(String type, String message)
	{
		this.type = type;
		this.message = message;
	}
	
	/**
	 * create new CRError
	 * @param type
	 * @param message
	 * @param stringStackTrace
	 */
	public CRError(String type, String message, String stringStackTrace)
	{
		this.type = type;
		this.message = message;
		this.stringStackTrace = stringStackTrace;
		
	}
	
	

}
