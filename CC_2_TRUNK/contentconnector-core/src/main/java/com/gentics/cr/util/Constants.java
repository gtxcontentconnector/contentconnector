package com.gentics.cr.util;

/**
 * Class containing default constants that are often used througout the code.
 * @author Christopher
 *
 */
public final class Constants {
	
	
	/**
	 * Base of hex system as int.
	 */
	public static final int HEX_BASE = 16;
	
	/**
	 * Milliseconds in a second.
	 */
	public static final int MILLISECONDS_IN_A_SECOND = 1000;
	
	/**
	 * defines how many bytes a kilobyte has.
	 */
	public static final int KILOBYTE = 1024;
	
	/**
	 * megabytes per byte.
	 */
	public static final double MEGABYTES_PER_BYTE =
		(1.0 / (((float) KILOBYTE) * ((float) KILOBYTE)));
	
	/**
	 * Prevents from instantiation.
	 */
	private Constants() { }

}
