package com.gentics.cr;

import java.io.Serializable;

import com.gentics.cr.exceptions.CRException;

/**
 * 
 * Last changed: $Date: 2010-04-01 15:24:02 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 541 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class CRError implements Serializable {

	/**
	 * unique serialization id.
	 */
	private static final long serialVersionUID = -4462449550837801315L;

	private String type;
	private String message;
	private String stringStackTrace;
	private CRException.ERRORTYPE errtype = CRException.ERRORTYPE.GENERAL_ERROR;

	/**
	 * sets the ERROR Type.
	 * @param type
	 */
	public void setErrorType(final CRException.ERRORTYPE type) {
		this.errtype = type;
	}

	/**
	 * gets the ERROR type.
	 * @return by default a GENERAL_ERROR
	 */
	public CRException.ERRORTYPE getErrorType() {
		return (this.errtype);
	}

	/**
	 * get Error Type.
	 * @return type of the error as string.
	 */
	public String getType() {
		return type;
	}

	/**
	 * sets Error Type.
	 * @param type
	 */
	public void setType(final String type) {
		this.type = type;
	}

	/**
	 * Gets the Error Message.
	 * @return usually the message represented by the Exception thrown
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * sets the Error Message, usually the message represented by the Exception thrown.
	 * @param message
	 */
	public void setMessage(final String message) {
		this.message = message;
	}

	/**
	 * gets StackTrace as String. Only for output and logging.
	 * @return Stacktrace
	 */
	public String getStringStackTrace() {
		return stringStackTrace;
	}

	/**
	 * sets StackTrace as String. Only for output and logging. 
	 * @param stringStackTrace
	 */
	public void setStringStackTrace(final String stringStackTrace) {
		this.stringStackTrace = stringStackTrace;
	}

	/**
	 * Default constructor for Serialization.
	 */
	public CRError() {

	}

	/**
	 * create new CRError.
	 * @param e Exception
	 */
	public CRError(final CRException e) {
		this.type = e.getType();
		this.message = e.getMessage();
		this.stringStackTrace = e.getStringStackTrace();
		this.errtype = e.getErrorType();
	}

	/**
	 * create new CRError.
	 * @param type
	 * @param message
	 */
	public CRError(final String type, final String message) {
		this.type = type;
		this.message = message;
	}

	/**
	 * create new CRError.
	 * @param type
	 * @param message
	 * @param stringStackTrace
	 */
	public CRError(final String type, final String message, final String stringStackTrace) {
		this.type = type;
		this.message = message;
		this.stringStackTrace = stringStackTrace;
	}

}
