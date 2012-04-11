package com.gentics.cr.lucene.indexer.transformer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.util.CRUtil;

/**
 * Last changed: $Date: 2009-06-24 17:10:19 +0200 (Mi, 24 Jun 2009) $
 * @version $Revision: 99 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class FileExtensionCreator extends ContentTransformer {
	private static final String SRC_ATTRIBUTE_KEY = "srcattribute";
	private static final String TARGET_ATTRIBUTE_KEY = "targetattribute";
	private String src_attribute = "filename";
	private String target_attribute = "extension";

	/**
	 * Create Instance of FileExtensionCreator
	 * @param config
	 */
	public FileExtensionCreator(GenericConfiguration config) {
		super(config);
		src_attribute = (String) config.get(SRC_ATTRIBUTE_KEY);
		target_attribute = (String) config.get(TARGET_ATTRIBUTE_KEY);
	}

	@Override
	public void processBean(CRResolvableBean bean) {
		if (this.src_attribute != null) {
			Object obj = bean.get(this.src_attribute);
			if (obj != null) {
				String newString = getStringContents(obj);
				if (newString != null) {
					newString = newString.replaceAll(".*\\.(.+)$", "$1");
					bean.set(this.target_attribute, newString);
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
		return str;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
