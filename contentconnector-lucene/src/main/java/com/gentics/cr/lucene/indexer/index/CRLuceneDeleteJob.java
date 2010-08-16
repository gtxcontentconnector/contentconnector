package com.gentics.cr.lucene.indexer.index;

import java.io.IOException;
import java.util.Hashtable;

import org.apache.lucene.index.IndexWriter;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.lucene.indexaccessor.IndexAccessor;
import com.gentics.cr.util.indexing.AbstractUpdateCheckerJob;
import com.gentics.cr.util.indexing.IndexLocation;

public class CRLuceneDeleteJob extends AbstractUpdateCheckerJob {

	public CRLuceneDeleteJob(CRConfig config, IndexLocation indexLoc,
			Hashtable<String, CRConfigUtil> configmap) {
		super(config, indexLoc, configmap);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void indexCR(IndexLocation indexLocation, CRConfigUtil config)
			throws CRException {
		if(indexLocation instanceof LuceneIndexLocation)
		{
			log.debug("Starting to clear index.");
			LuceneIndexLocation lindexloc = (LuceneIndexLocation)indexLocation;
			IndexAccessor ia = lindexloc.getAccessor();
			IndexWriter writer=null;
			try {
				writer = ia.getWriter();
				writer.deleteAll();
			} catch (IOException e) {
				log.error("Could not clear index",e);
			}
			finally{
				ia.release(writer);
			}
			log.debug("Finished clearing index.");
		}
		else
		{
			log.error("Index does not seem to be a Lucene index. Therfore no clearing will be done.");
		}
	}

}
