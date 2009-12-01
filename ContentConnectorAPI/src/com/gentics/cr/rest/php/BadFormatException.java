package com.gentics.cr.rest.php;
/**
 * BadFormatException.java
 *
 * Author : Ludovic Martin <ludovic DOT martin AT laposte DOT net>
 * This code is free for any use and modification but comes without any warranty.
 *
 * Bad format exception
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class BadFormatException extends Exception{
    /**
	 * 
	 */
	private static final long serialVersionUID = 00006L;

	/**
	 * Create new instance
	 * @param message
	 */
	public BadFormatException(String message){
        super(message);
    }
}