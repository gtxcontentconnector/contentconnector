package com.gentics.cr.lucene.didyoumean;

import java.io.IOException;
import java.util.Collection;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.spell.CustomSpellChecker;
import org.apache.lucene.search.spell.LuceneDictionary;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.lucene.indexaccessor.IndexAccessor;
import com.gentics.cr.lucene.util.CRLuceneUtil;
import com.gentics.cr.monitoring.MonitorFactory;
import com.gentics.cr.monitoring.UseCase;
import com.gentics.cr.util.indexing.AbstractUpdateCheckerJob;
import com.gentics.cr.util.indexing.IndexLocation;
import com.gentics.lib.log.NodeLogger;

/**
 * This job is used to re-index (or newly index) the didyoumean-index.
 */
public class DidyoumeanIndexJob extends AbstractUpdateCheckerJob {

	private DidyoumeanIndexExtension didyoumean;

	public DidyoumeanIndexJob(CRConfig updateCheckerConfig, IndexLocation indexLoc, DidyoumeanIndexExtension didyoumean) {
		super(updateCheckerConfig, indexLoc, null);

		this.identifyer = identifyer.concat(":reIndex");
		log = NodeLogger.getNodeLogger(DidyoumeanIndexJob.class);
		this.didyoumean = didyoumean;
	}

	/**
	 * starts the job - is called by the IndexJobQueue.
	 */
	@Override
	protected void indexCR(IndexLocation indexLocation, CRConfigUtil config) throws CRException {
		try {
			reIndex();
		} catch (IOException e) {
			throw new CRException("Could not access the DidYouMean- index! " + e.getMessage());
		}

	}

	private synchronized void reIndex() throws IOException {
		UseCase ucReIndex = MonitorFactory.startUseCase("reIndex()");
		// build a dictionary (from the spell package)
		log.debug("Starting to reindex didyoumean index.");
		IndexAccessor sourceAccessor = didyoumean.getSourceLocation().getAccessor();
		IndexReader sourceReader = sourceAccessor.getReader();
		CustomSpellChecker spellchecker = didyoumean.getSpellchecker();
		Collection<String> fields = null;

		if (didyoumean.isAll()) {
			fields = CRLuceneUtil.getFieldNames(sourceReader);
		} else {
			fields = didyoumean.getDym_fields();
		}
		try {
			for (String fieldname : fields) {
				LuceneDictionary dict = new LuceneDictionary(sourceReader, fieldname);				
				spellchecker.indexDictionary(dict);
			}
		} finally {
			if (sourceAccessor != null && sourceReader != null) {
				sourceAccessor.release(sourceReader);
			}
		}
		log.debug("Finished reindexing didyoumean index.");
		ucReIndex.stop();
	}

}
