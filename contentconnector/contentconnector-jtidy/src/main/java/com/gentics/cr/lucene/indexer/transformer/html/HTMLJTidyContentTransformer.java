package com.gentics.cr.lucene.indexer.transformer.html;

import java.io.ByteArrayInputStream;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;
import com.gentics.cr.util.CRUtil;

/**
 * 
 * Last changed: $Date: 2009-06-24 17:10:19 +0200 (Mi, 24 Jun 2009) $
 * @version $Revision: 99 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class HTMLJTidyContentTransformer extends ContentTransformer {

	/**
	 * Get new instance of HTMLContentTransformer.
	 * @param config
	 */
	public HTMLJTidyContentTransformer(GenericConfiguration config) {
		super(config);
	}

	/**
	 * Converts a string containing html to a String that does not contain html tags can be indexed by lucene.
	 * @param obj
	 */
	public String getStringContents(Object obj) {
		String ret = null;
		HTMLStripReader sr = getContents(obj);
		try {
			if (sr != null) {
				ret = CRUtil.readerToString(sr);
			}
			sr.close();
		} catch (Exception ex) {
			//Catch all exceptions here to not disturb the indexer
			ex.printStackTrace();
		}
		return (ret);
	}

	/**
	 * Converts a object containing html to a String that does not contain html tags can be indexed by lucene.
	 * @param obj
	 * @return HTMLStripReader of contents
	 */
	public HTMLStripReader getContents(Object obj) {
		String contents = null;
		if (obj instanceof String) {
			contents = (String) obj;
		} else {
			throw new IllegalArgumentException();
		}
		return new HTMLStripReader(TidyHelper.tidy(new ByteArrayInputStream(contents.getBytes())));
	}

	@Override
	public void processBean(CRResolvableBean bean) {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}
}
