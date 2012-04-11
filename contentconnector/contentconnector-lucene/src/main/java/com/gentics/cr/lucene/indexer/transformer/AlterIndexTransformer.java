package com.gentics.cr.lucene.indexer.transformer;

import java.io.IOException;

import org.apache.lucene.index.IndexWriter;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.exceptions.CRException;

/**
 * 
 * Last changed: $Date: 2009-06-24 17:10:19 +0200 (Mi, 24 Jun 2009) $
 * @version $Revision: 99 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class AlterIndexTransformer extends ContentTransformer implements LuceneContentTransformer {

	/**
	 * Create Instance of CommentSectionStripper
	 * @param config
	 */
	public AlterIndexTransformer(GenericConfiguration config) {
		super(config);

	}

	@Override
	public void processBean(CRResolvableBean bean) {
		System.out.println("NOTHING");

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	public void processBean(CRResolvableBean bean, IndexWriter writer) throws CRException {

		try {
			System.out.println(writer.numDocs());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
