package com.gentics.cr.lucene.indexer.transformer;

import org.apache.lucene.index.IndexWriter;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.exceptions.CRException;

/**
 * 
 * Last changed: $Date: 2009-06-24 17:10:19 +0200 (Mi, 24 Jun 2009) $
 * @version $Revision: 99 $
 * @author $Author: supnig@constantinopel.at $
 *
 */

public interface LuceneContentTransformer {
	/**
	 * Processes the specified bean
	 * @param bean
	 * @throws CRException throws exception if bean could not be processed
	 */
	public abstract void processBean(CRResolvableBean bean, IndexWriter writer) throws CRException;
}
