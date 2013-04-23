package com.gentics.cr.rest.php;

/*
 * PHPSerializable.java
 *
 * Author : Ludovic Martin <ludovic DOT martin AT laposte DOT net>
 * This code is free for any use and modification but comes without any warranty.
 */

/**
 * 
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 545 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public interface PHPSerializable {

	/**
	 * Called by PHPSerializer when the objet is serialized
	 * @return serialized data
	 */
	public String phpSerialize();
}
