package com.gentics.cr.lucene.indexer.transformer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

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
public class AppenderTransformer extends ContentTransformer {
	private static final String TRANSFORMER_ATTRIBUTE_KEY = "attribute";
	private static final String APPEND_TEXT_KEY = "text";
	private static final String POSITION_KEY = "position";

	private String attribute = "";
	private String text = "";
	private String position = "";

	boolean after = true;

	/**
	 * Create Instance of CommentSectionStripper
	 *  @param config
	 */
	public AppenderTransformer(GenericConfiguration config) {
		super(config);
		attribute = (String) config.get(TRANSFORMER_ATTRIBUTE_KEY);
		String atxt = (String) config.get(APPEND_TEXT_KEY);
		if (atxt != null)
			text = atxt;

		String pos = (String) config.get(POSITION_KEY);
		if (pos != null)
			position = pos;

		if ("before".equalsIgnoreCase(position))
			after = false;
	}

	@Override
	public void processBean(CRResolvableBean bean) {
		if (this.attribute != null) {
			Object obj = bean.get(this.attribute);
			if (obj != null) {
				String newString = getStringContents(obj);
				if (newString != null) {
					bean.set(this.attribute, newString);
				}
			}
		} else {
			LOGGER.error("Configured attribute is null. Bean will not be processed");
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

		String newString = null;

		if (this.after) {
			newString = str + this.text;
		} else {
			newString = this.text + str;
		}

		return newString;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
