package com.gentics.cr.rest.php;
/**
 * BadFormatException.java
 *
 * Author : Ludovic Martin <ludovic DOT martin AT laposte DOT net>
 * This code is free for any use and modification but comes without any warranty.
 *
 * Bad format exception
 * 
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 545 $
 * @author $Author: supnig@constantinopel.at $
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