package com.gentics.cr.lucene.didyoumean;

import java.io.IOException;

import org.apache.lucene.search.spell.CustomSpellChecker;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.util.indexing.AbstractUpdateCheckerJob;
import com.gentics.cr.util.indexing.IndexLocation;
import com.gentics.lib.log.NodeLogger;

/**
 * This job is used to clear a lucene didyoumean index.
 * 
 * @author Sebastian Vogel <s.vogel@gentics.com>
 * 
 */
public class DidyoumeanIndexDeleteJob extends AbstractUpdateCheckerJob {

	private DidyoumeanIndexExtension didyoumean;

	/**
	 * Constructor.
	 * 
	 * @param config configuration
	 * @param indexLoc indexLocation
	 * @param didyoumean the {@link com.gentics.cr.lucene.didyoumean.DidyoumeanIndexExtension} to clear
	 */
	public DidyoumeanIndexDeleteJob(final CRConfig config, final IndexLocation indexLoc,
		DidyoumeanIndexExtension didyoumean) {
		super(config, indexLoc, null);
		log = NodeLogger.getNodeLogger(DidyoumeanIndexDeleteJob.class);

		this.identifyer = identifyer.concat(":clear");
		this.didyoumean = didyoumean;
	}

	/**
	 * starts the job - is called by the IndexJobQueue.
	 */
	@Override
	protected final void indexCR(final IndexLocation indexLocation, final CRConfigUtil config) throws CRException {

		log.debug("Starting to clear index.");
		CustomSpellChecker spellchecker = didyoumean.getSpellchecker();
		try {
			spellchecker.clearIndex();
			didyoumean.getDidyoumeanLocation().resetIndexJobCreationTimes();
			didyoumean.getDidyoumeanLocation().getAccessor().reopen();
		} catch (IOException e) {
			log.error("Could not clear index", e);
		}
		log.debug("Finished clearing index.");

	}

}
