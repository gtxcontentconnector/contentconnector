package com.gentics.cr.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
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

	private static final int WORD_ABBREVIATION_MARGIN = 7;

	/**
	 * Log4j logger for error and debug messages.
	 */
	private static Logger logger = Logger.getLogger(StringUtils.class);

	/**
	 * private constructor as all methods of this class are static.
	 */
	private StringUtils() {
	}

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
		char[] hexCodes = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		StringBuffer hexCode = new StringBuffer(bytes.length * 2);
		//Somehow byte to int conversion creates signed ints
		//all values below 0 have to be corrected by +256
		final int signedIntFromByteToUnsignedIntCorrection = 256;
		for (byte b : bytes) {
			int i = b;
			if (i < 0) {
				i += signedIntFromByteToUnsignedIntCorrection;
			}
			int x = i % Constants.HEX_BASE;
			int y = (i - x) / Constants.HEX_BASE;
			hexCode.append(new char[] { hexCodes[y], hexCodes[x] });
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
		if (obj instanceof InputStream) {
			return streamToString((InputStream) obj);
		} else if (obj != null) {
			return obj.toString();
		} else {
			return null;
		}
	}

	/**
	 * Read an {@link InputStream} into a String.
	 * @param is - InputStream to read the String from.
	 * @return String with contents from the InputStream.
	 */
	public static String streamToString(final InputStream is) {
		if (is != null) {
			char[] buffer = new char[Constants.KILOBYTE];
			StringBuffer result = new StringBuffer();
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				int readBytes;
				while ((readBytes = reader.read(buffer)) != -1) {
					result.append(buffer, 0, readBytes);
				}
				return result.toString();
			} catch (UnsupportedEncodingException e) {
				logger.error("Encoding is not supported.", e);
			} catch (IOException e) {
				logger.error("Cannot read from InputStream", e);
			}
		}
		return null;
	}

	/**
	 * Get a {@link Boolean} out of an {@link Object}.
	 * @param parameter {@link Object} to convert into a {@link Boolean}
	 * @param defaultValue value to return if we cannot parse the object into a
	 * boolean
	 * @return {@link Boolean} representing the {@link Object}, defaultValue if
	 * the object cannot be parsed.
	 */
	public static Boolean getBoolean(final Object parameter, final boolean defaultValue) {
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
	 * @return String of comma separated values of the Collection.
	 */
	public static String getCollectionSummary(final Collection<?> collection) {
		return getCollectionSummary(collection, ",");
	}

	/**
	 * Create a summary of a collection of objects.
	 * they are seperated by the provided seperator.
	 * @param collection Collection containing the objects to display.
	 * @param seperator Seperator to use.
	 * @return String of seperated values of the collection.
	 */
	public static String getCollectionSummary(final Collection<?> collection, final String seperator) {
		StringBuilder result = new StringBuilder(collection.size() * AVERAGE_WORD_LENGTH);
		for (Object object : collection) {
			if (result.length() != 0) {
				result.append(seperator);
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
				ByteArrayInputStream bais = new ByteArrayInputStream(objectBytes);
				ObjectInputStream ois = new ObjectInputStream(bais);
				return ois.readObject();
			} catch (IOException e) {
				logger.error("Error while deserializing object.", e);
			} catch (ClassNotFoundException e) {
				logger.error("Cannot deserialize object because the class of " + "the object or one of its dependencies is not known "
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
	 * <li>"ä" is converted into "ae"</li>
	 * <li>"Ä" is converted into "Ae"</li>
	 * <li>"ö" is converted into "oe"</li>
	 * <li>"Ö" is converted into "Oe"</li>
	 * <li>"ü" is converted into "ue"</li>
	 * <li>"Ü" is converted into "Ue"</li>
	 * <li>All characters except a-z, A-Z, 0-9, ., _, / and - are replaces with
	 * _</li>
	 * </ul>
	 */
	public static String toCMSFolder(final String folderPubDir) {
		return folderPubDir.replace("&", "und").replace("\u00DF", "ss").replace("\u00FC", "ue").replace("\u00DC", "Ue")
				.replace("\u00F6", "oe").replace("\u00D6", "Oe").replace("\u00E4", "ae").replace("\u00C4", "Ae")
				.replaceAll("[^a-zA-Z0-9._/-]", "_");
	}

	/**
	 * Read from InputStream into String until the given byte sequence is
	 * matched. The end String is already read from the InputStream when the
	 * method returns.
	 * @param is - input stream to read
	 * @param end - String defining the end of the section to read
	 * @return String with the contents from the current position of the
	 * InputStream to the position where the end String starts.
	 * @throws IOException - if there was an error reading the input stream.
	 */
	public static String readUntil(final InputStream is, final String end) throws IOException {
		return readUntil(is, end.getBytes());
	}

	/**
	 * Read from InputStream into String until the given byte sequence is
	 * matched. The end byte sequence is already read from the InputStream when
	 * the method returns.
	 * @param is - input stream to read
	 * @param end - byte sequence defining the end of the section top read
	 * @return String with the contents from the current position of the
	 * InputStream to the position where the end byte sequence starts.
	 * @throws IOException - if there was an error reading the input stream.
	 */
	public static String readUntil(final InputStream is, final byte[] end) throws IOException {
		StringBuilder result = new StringBuilder();
		int matchposition = 0;
		byte read;
		byte[] buffer = new byte[end.length];
		while ((read = (byte) is.read()) != -1) {
			if (read == end[matchposition]) {
				buffer[matchposition++] = read;
				if (matchposition == end.length) {
					break;
				}
			} else if (matchposition != 0) {
				for (int i = 0; i < matchposition; i++) {
					result.append((char) buffer[i]);
				}
				matchposition = 0;
				result.append((char) read);
			} else {
				result.append((char) read);
			}
		}
		is.mark(0);
		return result.toString();
	}

	/**
	 * If there are &gt; or &lt; characters in the search result wicket throws a malformed markup exception, 
	 * so we have to convert them into html entities.
	 * 
	 * @param input input to escape
	 * @return escaped input
	 */
	public static String escapeSearchContent(final String input) {
		String output = input;
		output = org.apache.commons.lang.StringUtils.replace(output, "<", "&lt;");
		output = org.apache.commons.lang.StringUtils.replace(output, ">", "&gt;");
		return output;
	}

	/**
	 * @param input input string to abbreviate
	 * @param cutPosition length at which to start searching for the first whitespace character to abbreviate
	 * @param separator seperator string to use at the end
	 * @return the abbreviated string at the first whitespace character found after length.
	 * if nothing is found after length + margin than just cut it at that length
	 */
	public static String abbreviate(final String input, final int cutPosition, final String separator) {
		if (input.length() > cutPosition) {

			StringBuilder output = new StringBuilder(cutPosition * 2);
			int index = input.indexOf(' ', cutPosition);
			int computedCutPosition = cutPosition;

			// no whitespace after length in input
			/// so just cut it
			if (index == -1 && input.length() < (cutPosition + WORD_ABBREVIATION_MARGIN)) {

				return input;

			} else if (index == -1 || index >= (cutPosition + WORD_ABBREVIATION_MARGIN)) {

				computedCutPosition = cutPosition;

			} else {

				computedCutPosition = index;

			}

			output.append(input.substring(0, computedCutPosition));

			if (!input.substring(0, computedCutPosition).endsWith(" ")) {

				output.append(" ");

			}

			output.append(separator);

			return output.toString();

		} else {

			return (input);

		}
	}

	/**
	 * @param input input string
	 * @param wordcount wordcount to cut off
	 * @param separator separator to use (e.g. "...")
	 * @return abbreviated string after number of wordcount
	 */
	public static String abbreviateWordCount(final String input, final int wordcount, final String separator) {
		int words = 0;
		int currentIndex = 0;

		while (words < wordcount) {

			words++;
			currentIndex = input.indexOf(' ', currentIndex) + 1;

			if (currentIndex == -1) {
				return input;
			}

		}

		return input.substring(0, currentIndex) + separator;
	}

}
