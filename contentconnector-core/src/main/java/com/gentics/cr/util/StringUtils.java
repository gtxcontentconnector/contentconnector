package com.gentics.cr.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;

/**
 * Utility class with static methods for Strings.
 * @author bigbear3001
 *
 */
public final class StringUtils {

	/**
	 * Log4j logger for error and debug messages.
	 */
	private static Logger logger = Logger.getLogger(StringUtils.class);

	/**
	 * private constructor as all methods of this class are static.
	 */
	private StringUtils() { }

	/**
	 * Get the md5sum of a {@link String} in hex code.
	 * @param string {@link String} top get the MD5 Sum of
	 * @return MD5 sum as hex code for the given {@link String}.
	 */
	public static String md5sum(final String string) {
		return md5sum(string.getBytes());
	}

	/**
	 * Get the md5sum of the given bytes in hex code.
	 * @param bytes bytes to get the md5sum for
	 * @return MD5 sum as hex code for the given bytes.
	 */
	public static String md5sum(final byte[] bytes) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(bytes);
			byte[] hash = digest.digest();
			return toHex(hash);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Your system is missing an MD5 implementation.", e);
		}
		return null;
	}

	/**
	 * Get the Hex code for the given bytes.
	 * @param bytes array of bytes to generate Hex code for.
	 * @return hex code for bytes
	 */
	public static String toHex(final byte[] bytes) {
		char[] hexCodes = new char[]{'0', '1', '2', '3', '4', '5', '6', '7',
				'8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
		StringBuffer hexCode = new StringBuffer(bytes.length * 2);
		//Somehow byte to int conversion creates signed ints
		//all values below 0 have to be corrected by +256
		final int signedIntFromByteToUnsignedIntCorrection = 256;
		for (byte b : bytes) {
			int i = (int) b;
			if (i < 0) {
				i += signedIntFromByteToUnsignedIntCorrection;
			}
			int x = i % Constants.HEX_BASE;
			int y = (i - x) / Constants.HEX_BASE;
			hexCode.append(new char[]{hexCodes[y], hexCodes[x]});
		}
		return hexCode.toString();
	}
	
	/**
	 * Converts an object to a String. Usefull if you don't want to check for
	 * null before string conversion.
	 * @param obj object to convert to a String.
	 * @return Object as String, <code>null</code> in case object is null.
	 */
	public static String toString(final Object obj) {
		if (obj != null) {
			return obj.toString();
		} else {
			return null;
		}
	}

	/**
	 * Get a {@link Boolean} out of an {@link Object}.
	 * @param parameter {@link Object} to convert into a {@link Boolean}
	 * @param defaultValue value to return if we cannot parse the object into a
	 * boolean
	 * @return {@link Boolean} representing the {@link Object}, defaultValue if
	 * the object cannot be parsed.
	 */
	public static Boolean getBoolean(final Object parameter,
			final boolean defaultValue) {
		if (parameter == null) {
			return defaultValue;
		} else if (parameter instanceof Boolean) {
			return (Boolean) parameter;
		} else if (parameter instanceof String) {
			return Boolean.parseBoolean((String) parameter);
		} else {
			return Boolean.parseBoolean(parameter.toString());
		}

	}
}
