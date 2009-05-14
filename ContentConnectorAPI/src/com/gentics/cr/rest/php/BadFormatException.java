package com.gentics.cr.rest.php;
/*
 * BadFormatException.java
 *
 * Author : Ludovic Martin <ludovic DOT martin AT laposte DOT net>
 * This code is free for any use and modification but comes without any warranty.
 */


/**
 * Bad format exception
 * Ludovic Martin <ludovic.martin@laposte.net>
 */
public class BadFormatException extends Exception{
    /**
	 * 
	 */
	private static final long serialVersionUID = 00006L;

	public BadFormatException(String message){
        super(message);
    }
}