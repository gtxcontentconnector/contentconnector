package com.gentics.cr.exceptions;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

import com.gentics.cr.CRError;

/**
 * General Exception class for the ContentConnector.
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 545 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class CRException extends Exception implements Serializable {

	/**
	 * Serial Version ID.
	 */
	private static final long serialVersionUID = -3702075481737853571L;

	/**
	 * Message.
	 */
	private String message;
	/**
	 * Type.
	 */
	private String type;
	/**
	 * Type as Enum.
	 */
	private ERRORTYPE errType = ERRORTYPE.GENERAL_ERROR;
	
	/**
	 * String stacktrace for serializeable transfers.
	 */
	private String stringStack;

	/**
	 * Set the error type that can later be evaluated by a client when sending
	 * over javaxml.
	 * @param etype type
	 */
	public final void setErrorType(final ERRORTYPE etype) {
		this.errType = etype;
	}

	/**
	 * gets the error type that can later be evaluated by a client
	 * when sending over javaxml.
	 * @return type
	 */
	public final ERRORTYPE getErrorType() {
		return (errType);
	}
	
	/**
	 * gets the error message.
	 * @return message
	 */
	public final String getMessage() {
		return (message);
	}
	
	/**
	 * gets the error type as string.
	 * @return type
	 */
	public final String getType() {
		return (type);
	}
	
	/**
	 * Set the error message.
	 * @param newmessage message.
	 */
	public final void setMessage(final String newmessage) {
		this.message = newmessage;
	}
	
	/**
	 * Set the error type as string.
	 * @param newType type.
	 */
	public final void setType(final String newType) {
		this.type = newType;
	}
	
	/**
	 * default constructor to create a new CRException.
	 */
	public CRException() {
		super();
	}
	
	/**
	 * Create new CRException from CRError.
	 * @param err error
	 */
	public CRException(final CRError err) {
		super(err.getMessage());
		this.message = err.getMessage();
		this.type = err.getType();
		this.stringStack = err.getStringStackTrace();
		this.errType = err.getErrorType();
	}
	
	/**
	 * Create a new instance.
	 * @param newtype type
	 * @param newmessage message
	 */
	public CRException(final String newtype, final String newmessage) {
		this(newtype, newmessage, ERRORTYPE.GENERAL_ERROR);
	}

	/**
	 * creates new CRException with ERRORTYPE.
	 * @param newtype type
	 * @param newmessage message
	 * @param etype type as enum
	 */
	public CRException(final String newtype, final String newmessage,
			final ERRORTYPE etype) {
		super(newmessage);
		this.message = newmessage;
		this.type = newtype;
		this.errType = etype;
	}

	/**
	 * Creates a new CRException from an Exception with a new message.
	 * @param crMessage - message for the CRException
	 * @param ex - Exception to wrap in the CRException
	 */
	public CRException(final String crMessage, final Exception ex) {
		super(crMessage, ex);
		message = crMessage;
		if (message == null) {
			message = ex.getClass().getName();
		}
		type = ex.getClass().getSimpleName();
		setStackTrace(ex.getStackTrace());
	}
	
	/**
	 * Creates a new CRException from an Exception.
	 * @param ex Exception to wrap
	 */
	public CRException(final Exception ex) {
		this(ex.getMessage(), ex);
	}

	/**
	 * Create a new {@link CRException} with the given Message.
	 * @param crMessage - message for the Exception
	 */
	public CRException(final String crMessage) {
		super(crMessage);
		message = crMessage;
		type = "ERROR";
	}

	/**
	 * gets the Stacktrace as String.
	 * @return stringstacktrace
	 */
	public final String getStringStackTrace() {
		if (stringStack == null) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw, true);
			this.printStackTrace(pw);
			pw.flush();
			sw.flush();
			stringStack = sw.toString();
		}
		return stringStack;
	}
		
	/**
	 * sets the stacktrace as string that is returned by getStringStackTrace().
	 * @param str string
	 */
	public final void setStringStackTrace(final String str) {
			this.stringStack = str;
	}
	/**
	 * has to be called before serialization.
	 * DEPRICATED => USE CRError to serialize an Error
	 */
	public final void initStringStackForSerialization() {
		this.stringStack = getStringStackTrace();
	}

	/**
	 * Enum holding the different error types for CRExceptions.
	 */
	public enum ERRORTYPE {
		/**
		 * Field when no data has been found.
		 */
		NO_DATA_FOUND,
		/**
		 * Field when a general Error occurred.
		 */
		GENERAL_ERROR,
		/**
		 * Field when a fatal Error occurred.
		 */
		FATAL_ERROR
	}
}
