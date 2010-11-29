package com.gentics.cr.exceptions;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

import com.gentics.cr.CRError;

/**
 * 
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 545 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class CRException extends Exception implements Serializable{

	
	private static final long serialVersionUID = -3702075481737853571L;

	private String message;
	
	private String type;
	
	private ERRORTYPE errType = ERRORTYPE.GENERAL_ERROR;
	
	@SuppressWarnings("unused")
	private String stringStack;

	/**
	 * Set the error type that can later be evaluated by a client when sending
	 * over javaxml.
	 * @param type
	 */
	public void setErrorType(ERRORTYPE type) {
		this.errType = type;
	}

	/**
	 * gets the error type that can later be evaluated by a client when sending over javaxml
	 * @return type
	 */
	public ERRORTYPE getErrorType()
	{
		return(this.errType);
	}
	
	/**
	 * gets the error message
	 * @return message
	 */
	public String getMessage()
	{
		return(this.message);
	}
	
	/**
	 * gets the error type as string
	 * @return type
	 */
	public String getType()
	{
		return(this.type);
	}
	
	/**
	 * Set the error message
	 * @param newmessage 
	 */
	public void setMessage(String newmessage)
	{
		this.message=newmessage;
	}
	
	/**
	 * Set the error type as string
	 * @param newType 
	 */
	public void setType(String newType)
	{
		this.type=newType;
	}
	
	/**
	 * default constructor to create a new CRException
	 */
	public CRException()
	{
		super();
	}
	
	/**
	 * Create new CRException from CRError
	 * @param err
	 */
	public CRException(CRError err)
	{
		super(err.getMessage());
		this.message = err.getMessage();
		this.type = err.getType();
		this.stringStack = err.getStringStackTrace();
		this.errType = err.getErrorType();
	}
	
	/**
	 * 
	 * @param newtype
	 * @param newmessage
	 */
	public CRException(final String newtype, final String newmessage) {
		this(newtype, newmessage, ERRORTYPE.GENERAL_ERROR);
	}

	/**
	 * creates new CRException with ERRORTYPE
	 * @param newtype
	 * @param newmessage
	 * @param type
	 */
	public CRException(final String newtype, final String newmessage,
			final ERRORTYPE type) {
		super(newmessage);
		this.message = newmessage;
		this.type = newtype;
		this.errType = type;
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
	 * gets the Stacktrace as String
	 * @return stringstacktrace
	 */
		public String getStringStackTrace()
		{
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw, true);
				this.printStackTrace(pw);
				pw.flush();
				sw.flush();
				return sw.toString();
		}
		
		/**
		 * sets the stacktrace as string that is returned by getStringStackTrace()
		 * @param str
		 */
		public void setStringStackTrace(String str) {
				this.stringStack = str;
		}
		/**
		 * has to be called before serialization
		 * DEPRICATED => USE CRError to serialize an Error
		 */
		public void initStringStackForSerialization() {
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
