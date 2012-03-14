package com.gentics.cr.lucene.indexer.transformer.other;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;
import com.gentics.cr.util.CRUtil;


/**
 * Get content out of an attribute with a regular expression and copy it to
 * another attribute.
 * 
 * Last changed: $Date: 2009-06-24 17:10:19 +0200 (Mi, 24 Jun 2009) $
 * @version $Revision: 99 $
 * @author $Author: bigbear3001 $
 *
 */
public class RegexCopy extends ContentTransformer {
	private static final String TRANSFORMER_ATTRIBUTE_KEY = "attribute";
	private static final String TARGET_ATTRIBUTE_KEY = "target_attribute";
	private static final String STRIPPER_PATTERN_KEY = "pattern";
	private String attribute = "";
	private String target_attribute = "";
	private String pattern = "(?si)<h1[^>]*>(.*?)</h1>";
	private Pattern c_pattern = null;
	/**
	 * Log4j logger for error and debug messages.
	 */
	private static final Logger logger = Logger.getLogger(RegexCopy.class);
	
	/**
	 * Create Instance of CommentSectionStripper
	 * if the pattern is not configured in the config: the default pattern
	 * {@value #pattern} will be used.
	 * @param config 
	 */
	public RegexCopy(final GenericConfiguration config) {
		super(config);
		attribute = config.getString(TRANSFORMER_ATTRIBUTE_KEY);
		target_attribute = config.getString(TARGET_ATTRIBUTE_KEY);
		pattern = config.getString(STRIPPER_PATTERN_KEY, pattern);
		c_pattern = Pattern.compile(pattern);
	}

	@Override
	public void processBean(final CRResolvableBean bean) {
		if (attribute != null) {
			Object obj = bean.get(attribute);
			if (obj != null) {
				String newString = getStringContents(obj);
				if (newString != null) {
					bean.set(target_attribute, newString);
				}
			}
		} else {
			log.error(
					"Configured attribute is null. Bean will not be processed");
		}
	
	}
	
	private String getStringContents(final Object obj) {
		String str = null;
		if (obj instanceof String) {
			str = (String) obj;
		} else if (obj instanceof byte[]) {
			try {
				str = CRUtil.readerToString(new InputStreamReader(
						new ByteArrayInputStream((byte[]) obj)));
			} catch (IOException e) {
				logger.error("Error converting the object into a string.", e);
			}
		}
		
		// Replace all occurrences of pattern in input
		Matcher matcher = c_pattern.matcher(str);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
