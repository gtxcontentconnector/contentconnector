package com.gentics.cr.lucene.indexer.index;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.util.indexing.AbstractUpdateCheckerJob;
import com.gentics.cr.util.indexing.IndexLocation;
@Deprecated
public class CRLuceneOptimizeJob extends AbstractUpdateCheckerJob {

	public CRLuceneOptimizeJob(CRConfig config, IndexLocation indexLoc, ConcurrentHashMap<String, CRConfigUtil> configmap) {
		super(config, indexLoc, configmap);
		log = Logger.getLogger(CRLuceneOptimizeJob.class);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected final void indexCR(final IndexLocation indexLocation, final CRConfigUtil config) throws CRException {
		//Optimize is bad for you and has been removed in Lucene 4.0
	}

}
