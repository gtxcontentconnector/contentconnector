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
   * Base of hex system as int.
   */
  private static final int HEX_BASE = 16;

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
    char[] hexCodes = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8',
        '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    StringBuffer hexCode = new StringBuffer(bytes.length * 2);
    for (byte b : bytes) {
      int i = (int) b;
      int x = i % HEX_BASE;
      int y = (i - x) / HEX_BASE;
      hexCode.append(new char[]{hexCodes[y], hexCodes[x]});
    }
    return hexCode.toString();
  }
}
