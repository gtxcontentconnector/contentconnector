package com.gentics.cr.lucene.indexer.transformer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.util.CRUtil;

/**
 * Regex Replacing within the content can be quite costful in terms of performance.
 * 
 * Last changed: $Date: 2009-06-24 17:10:19 +0200 (Mi, 24 Jun 2009) $
 * @version $Revision: 99 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class RegexReplacer extends ContentTransformer {
	private static final String TRANSFORMER_ATTRIBUTE_KEY = "attribute";
	private static final String STRIPPER_PATTERN_KEY = "pattern";
	private static final String REPLACEMENT_PATTERN_KEY = "replacement";
	private String attribute = "";
	private String pattern = "(?s)(<!--[ \t\n\r]*noindexstart[^>]*-->.*?<!--[ \t\n\r]*noindexend[^>]*-->)";
	private String replacement = "";
	private Pattern c_pattern = null;

	/**
	 * Create Instance of CommentSectionStripper
	 * if the pattern is not configured in the config: the default pattern
	 * {@value #pattern} will be used.
	 * @param config 
	 */
	public RegexReplacer(final GenericConfiguration config) {
		super(config);
		attribute = config.getString(TRANSFORMER_ATTRIBUTE_KEY);
		pattern = config.getString(STRIPPER_PATTERN_KEY, pattern);
		c_pattern = Pattern.compile(pattern);
		replacement = config.getString(REPLACEMENT_PATTERN_KEY, replacement);
	}

	@Override
	public void processBean(final CRResolvableBean bean) {
		if (this.attribute != null) {
			Object obj = bean.get(this.attribute);
			if (obj != null) {
				String newString = getStringContents(obj);
				if (newString != null) {
					bean.set(this.attribute, newString);
				}
			}
		} else {
			log.error("Configured attribute is null. Bean will not be processed");
		}

	}

	private String getStringContents(Object obj) {
		String str = null;
		if (obj instanceof String) {
			str = (String) obj;
		} else if (obj instanceof byte[]) {
			try {
				str = CRUtil.readerToString(new InputStreamReader(new ByteArrayInputStream((byte[]) obj)));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Replace all occurrences of pattern in input
		Matcher matcher = c_pattern.matcher(str);
		str = matcher.replaceAll(replacement);
		return str;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
