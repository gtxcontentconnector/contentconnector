package com.gentics.cr.lucene.indexer.transformer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.util.CRUtil;

/**
 * Create a file extension based on the whole filename (src field).
 * Store the extension in the target field.
 * Last changed: $Date: 2009-06-24 17:10:19 +0200 (Mi, 24 Jun 2009) $
 * @version $Revision: 99 $
 * @author $Author: supnig@constantinopel.at $
 */
public class FileExtensionCreator extends ContentTransformer {

	/**
	 * Config attribute to specify the source.
	 */
	private static final String SRC_ATTRIBUTE_KEY = "srcattribute";

	/**
	 * Config attribute to specify the target.
	 */
	private static final String TARGET_ATTRIBUTE_KEY = "targetattribute";

	/**
	 * Field to store the src attribute to.
	 */
	private String srcAttribute = "filename";

	/**
	 * Field to store the target attribute to.
	 */
	private String targetAttribute = "extension";

	/**
	 * Create Instance of FileExtensionCreator.
	 * @param config configuration for FileExtensionCreator.
	 * E.g.: source for retrieval of extension
	 */
	public FileExtensionCreator(final GenericConfiguration config) {
		super(config);
		srcAttribute = (String) config.get(SRC_ATTRIBUTE_KEY);
		targetAttribute = (String) config.get(TARGET_ATTRIBUTE_KEY);
	}

	@Override
	public void processBean(final CRResolvableBean bean) {
		if (this.srcAttribute != null) {
			Object obj = bean.get(this.srcAttribute);
			if (obj != null) {
				String newString = getStringContents(obj);
				if (newString != null) {
					newString = newString.replaceAll(".*\\.(.+)$", "$1");
					bean.set(this.targetAttribute, newString);
				}
			}
		} else {
			log.error("Configured attribute is null. Bean will not be processed");
		}

	}

	private String getStringContents(final Object obj) {
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
		return str;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
