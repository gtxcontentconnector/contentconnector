package com.gentics.cr.lucene.indexer.transformer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;

import org.apache.lucene.util.CharacterUtils.CharacterBuffer;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.util.CRUtil;


/**
 * Regex Replacing within the content can be quite costful in terms of performance.
 * 
 * Last changed: $Date: 2009-06-24 17:10:19 +0200 (Mi, 24 Jun 2009) $
 * @version $Revision: 99 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class CleanupText extends ContentTransformer{
	private static enum Replacing {
		NONE,
		INDEX_POINT,
		SPACES
	}
	
	private static final String TRANSFORMER_ATTRIBUTE_KEY="attribute";
	
	private String attribute="";
	
	/**
	 * Create Instance of CommentSectionStripper
	 *  @param config
	 */
	public CleanupText(final GenericConfiguration config) {
		super(config);
		attribute = config.getString(TRANSFORMER_ATTRIBUTE_KEY);
	}

	@Override
	public void processBean(final CRResolvableBean bean) throws CRException {
		try {
			if (attribute != null) {
				Object obj = bean.get(attribute);
				if (obj != null) {
					boolean changed = false;
					StringBuilder result = new StringBuilder();
					StringBuilder buffer = new StringBuilder();
					Reader reader = getStreamContents(obj);
					int cInt;
					Replacing activeReplacing = Replacing.NONE;
					while ((cInt = reader.read()) != -1) {
						char character = (char) cInt;
						if (character == '.') {
							activeReplacing = setReplacing(activeReplacing, result, buffer, Replacing.INDEX_POINT);
							if (buffer.length() < 3) {
								buffer.append(character);
							} else {
								changed = true;
							}
						} else if (character == '\r' || character == '\n' || character == '\t' || character == ' '
							//http://www.cs.sfu.ca/~ggbaker/reference/characters/#space
							|| cInt == 160 //non breaking space
							|| cInt == 8195 //em-space
							|| cInt == 8194 //en-space
							) {
							activeReplacing = setReplacing(activeReplacing, result, buffer, Replacing.SPACES);
							if (buffer.length() == 0) {
								buffer.append(' ');
							} else {
								changed = true;
							}
						} else if (cInt <= 31) {
							//ASCII Controll Characters
							changed = true;
						} else {
							activeReplacing = setReplacing(activeReplacing, result, buffer, Replacing.NONE);
							result.append(character);
						}
					}
					activeReplacing = setReplacing(activeReplacing, result, buffer, Replacing.NONE);
					if (changed) {
						bean.set(attribute, result.toString());
					}
				}
			} else {
				log.error("Configured attribute is null. Bean will not be processed");
			}
		} catch (IOException e) {
			throw new CRException("Cannot read the attribute " + attribute + ".", e);
		}
	}

	private Replacing setReplacing(Replacing activeReplacing, StringBuilder result, StringBuilder buffer, Replacing replacing) {
		if (activeReplacing != replacing) {
			result.append(buffer);
			buffer.replace(0, buffer.length(), "");
			activeReplacing = replacing;
		}
		return activeReplacing;
	}
	
	
	
	
	private Reader getStreamContents(Object obj) {
		if (obj != null) {
			if (obj instanceof String) {
				return new StringReader((String) obj);
			} else if (obj instanceof byte[]) {
				return new InputStreamReader(new ByteArrayInputStream((byte[]) obj));
			}
		}
		return null;
	}

	@Override
	public void destroy() {
		
	}

}
