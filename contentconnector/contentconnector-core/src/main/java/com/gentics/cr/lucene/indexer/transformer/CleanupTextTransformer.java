package com.gentics.cr.lucene.indexer.transformer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.exceptions.CRException;

/**
 * Cleanup an attribute from not readable characters and not well readable
 * characters such es endless lines of point in the index pages of word
 * documents.
 * 
 * Last changed: $Date: 2009-06-24 17:10:19 +0200 (Mi, 24 Jun 2009) $
 * @version $Revision: 99 $
 * @author $Author: bigbear3001 $
 *
 */
public class CleanupTextTransformer extends ContentTransformer {

	/**
	 * int value of the unicode en whitespace character.
	 */
	private static final int EN_WHITESPACE = 8194;

	/**
	 * int value of the unicode em whitespace character.
	 */
	private static final int EM_WHITESPACE = 8195;

	/**
	 * int value of the non breaking whitesapce character.
	 */
	private static final int NON_BREAKING_WHITESPACE = 160;

	/**
	 * at the beginning of the ASCII character map there are the control
	 * characters. 31 is the last control character of this section.
	 * 
	 */
	private static final int LAST_ASCII_CONTROL_CHARACTER = 31;

	/**
	 * number of index point to keep for readability.
	 */
	private static final int INDEX_POINTS_TO_KEEP = 3;

	/**
	 * Replacing defines if we are in a multicharacter replacing mode.
	 */
	private static enum Replacing {
		/**
		 * no replacing mode active.
		 */
		NONE,
		/**
		 * index point replacing mode active.
		 */
		INDEX_POINT,
		/**
		 * multiple spaces replacing mode active.
		 */
		SPACES
	}

	/**
	 * State holds the current state of the replacer like buffers and the
	 * replacing state.
	 */
	private final class State {
		/**
		 * private constructor (checkstyle is so picky).
		 */
		private State() {
		}

		/**
		 * Result buffer.
		 */
		private StringBuilder result = new StringBuilder();
		/**
		 * Buffer for multicharacter replacings.
		 */
		private StringBuilder buffer = new StringBuilder();
		/**
		 * if there is a pending whitespace which is not contained in the
		 * buffer. this acts like a second whitespace buffer, since we only
		 * display one whitespace max we can do this with a boolean.
		 */
		private boolean whitespacePending = false;
		/**
		 * stores the active multi character replacing mode.
		 */
		private Replacing activeReplacing = Replacing.NONE;

		/**
		 * set a new multicharacter replacing mode and empties all buffers
		 * except the result buffer.
		 * @param replacing - replacing mode to set
		 */
		private void setReplacing(final Replacing replacing) {
			if (activeReplacing != replacing) {
				result.append(buffer);
				buffer.replace(0, buffer.length(), "");
				if (whitespacePending) {
					result.append(' ');
					whitespacePending = false;
				}
				activeReplacing = replacing;
			}
		}
	}

	/**
	 * configuration key to read the attribute.
	 */
	private static final String TRANSFORMER_ATTRIBUTE_KEY = "attribute";

	private static final String TRANSFORMER_TRIM_CONTENT_KEY = "trimContent";

	/**
	 * attribute to cleanup in all beans.
	 */
	private String attribute = "";

	private boolean trimContent = false;

	/**
	 * Create Instance of the transformer.
	 *  @param config - configuration of the transformer holding the following
	 *  options:
	 *  - attribute @see {@link #attribute}
	 */
	public CleanupTextTransformer(final GenericConfiguration config) {
		super(config);
		attribute = config.getString(TRANSFORMER_ATTRIBUTE_KEY);
		if (config.getString(TRANSFORMER_TRIM_CONTENT_KEY) != null) {
			trimContent = config.getBoolean(TRANSFORMER_TRIM_CONTENT_KEY);
		}
	}

	@Override
	public final void processBean(final CRResolvableBean bean) throws CRException {
		try {
			if (attribute != null) {
				Object obj = bean.get(attribute);
				if (obj != null) {
					boolean changed = false;
					State state = new State();
					Reader reader = getStreamContents(obj);
					int cInt;
					while ((cInt = reader.read()) != -1) {
						char character = (char) cInt;
						boolean whitespace = checkWhitespaceCharacter(character, cInt);
						if (character == '.' || (state.activeReplacing == Replacing.INDEX_POINT && whitespace)) {
							state.setReplacing(Replacing.INDEX_POINT);
							if (whitespace) {
								state.whitespacePending = true;
								changed = true;
							} else if (state.buffer.length() < INDEX_POINTS_TO_KEEP) {
								state.buffer.append(character);
							} else {
								changed = true;
							}
						} else if (whitespace) {
							state.setReplacing(Replacing.SPACES);
							if (state.buffer.length() == 0) {
								state.buffer.append(' ');
							} else {
								changed = true;
							}
						} else if (cInt <= LAST_ASCII_CONTROL_CHARACTER) {
							//ASCII Controll Characters
							changed = true;
						} else {
							state.setReplacing(Replacing.NONE);
							state.result.append(character);
						}
					}
					state.setReplacing(Replacing.NONE);
					if (changed) {
						String content = state.result.toString();
						if (trimContent) {
							bean.set(attribute, content.trim());
						} else {
							bean.set(attribute, content);
						}
					}
				}
			} else {
				LOGGER.error("Configured attribute is null. " + "Bean will not be processed");
			}
		} catch (IOException e) {
			throw new CRException("Cannot read the attribute " + attribute + ".", e);
		}
	}

	/**
	 * check if the current character is a whitespace character.
	 * @param character - char value of the character
	 * @param cInt - int value of the character
	 * @return <code>true</code> if the character is a whitespace character
	 */
	private boolean checkWhitespaceCharacter(final char character, final int cInt) {
		return character == '\r' || character == '\n' || character == '\t' || character == ' ' || cInt == NON_BREAKING_WHITESPACE
				|| cInt == EM_WHITESPACE || cInt == EN_WHITESPACE;
	}

	/**
	 * convert the object to a StreamReader.
	 * @param obj - object to convert
	 * @return stream of the object or <code>null</code> if we cannot create a
	 * StreamReader for it.
	 */
	private Reader getStreamContents(final Object obj) {
		if (obj != null) {
			if (obj instanceof String) {
				return new StringReader((String) obj);
			} else if (obj instanceof byte[]) {
				try {
					return new InputStreamReader(new ByteArrayInputStream((byte[]) obj), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					LOGGER.fatal("UTF-8 has to be supported.", e);
				}
			}
		}
		return null;
	}

	@Override
	public void destroy() {

	}

}
