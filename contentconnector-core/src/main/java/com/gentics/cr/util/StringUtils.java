package com.gentics.cr.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import org.apache.log4j.Logger;

/**
 * Utility class with static methods for Strings.
 * @author bigbear3001
 *
 */
public final class StringUtils {

	/**
	 * Variable for the average word length to calculate the size of the
	 * StringBuilder based on how many items we have.
	 */
	public static final int AVERAGE_WORD_LENGTH = 7;
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

	/**
	 * Create a summary of a collection of objects.
	 * e.g. ["a", "b"] is transformed to the String "a, b".
	 * @param collection - Collection containing the objects
	 * @return String of comma seperated values of the Collection.
	 */
	public static String getCollectionSummary(final Collection<?> collection) {
		StringBuilder result =
			new StringBuilder(collection.size() * AVERAGE_WORD_LENGTH);
		for (Object object : collection) {
			if (result.length() != 0) {
				result.append(",");
			}
			result.append(' ');
			result.append(object);
		}
		return result.toString();
	}
	
	/**
	 * Serialize the given object into a byte array.
	 * @param object - object to serialize into the byte array.
	 * @return serializedObject as byte array.
	 */
	public static byte[] serializeToByteArray(final Serializable object) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			return baos.toByteArray();
		} catch (IOException e) {
			logger.error("Error while serializing object.", e);
			return null;
		}
	}

	/**
	 * Serialize the given object into a String.
	 * @param object - object to serialize into the String.
	 * @return serializedObject as String.
	 */
	public static String serialize(final Serializable object) {
		return new String(serializeToByteArray(object));
	}
	
	/**
	 * Deserialize an object from a given string.
	 * @param objectString - string containing a serialized object
	 * @return object contained in the string
	 * @see #serialize(Serializable)
	 */
	public static Object deserialize(final String objectString) {
		if (objectString != null && objectString.length() > 0) {
			return deserialize(objectString.getBytes());
		}
		return null;
		
	}

	/**
	 * Deserialize an object from a given byteArray.
	 * @param objectBytes - byte array containing a serialized object
	 * @return object contained in the byte array
	 * @see #serializeToByteArray(Serializable)
	 */
	public static Object deserialize(final byte[] objectBytes) {
		if (objectBytes != null && objectBytes.length > 0) {
			try {
				ByteArrayInputStream bais =
					new ByteArrayInputStream(objectBytes);
				ObjectInputStream ois = new ObjectInputStream(bais);
				return ois.readObject();
			} catch (IOException e) {
				logger.error("Error while deserializing object.", e);
			} catch (ClassNotFoundException e) {
				logger.error("Cannot deserialize object because the class of "
						+ "the object or one of its dependencies is not known "
						+ "on this system.", e);
			}
		}
		return null;
	}
	/**
	 * Converts a folder name into a Gentics Content.Node compatible folder
	 * name.
	 * @param folderPubDir - folder name to convert.
	 * @return CMS compatible folder name.
	 * <ul>
	 * <li>"&" is converted into "und"</li>
	 * <li>"ß" is converted into "ss"</li>
	 * <li>All characters except a-z, A-Z, 0-9, ., _, / and - are replaces with
	 * _</li>
	 * </ul>
	 */
	public static String toCMSFolder(final String folderPubDir) {
		return folderPubDir.replaceAll("&", "und")
			.replaceAll("ß", "ss")
			.replaceAll("[^a-zA-Z0-9._/-]", "_");
	}
}
