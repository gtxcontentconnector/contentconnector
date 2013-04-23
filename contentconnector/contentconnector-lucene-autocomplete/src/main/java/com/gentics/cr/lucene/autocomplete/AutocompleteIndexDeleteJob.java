package com.gentics.cr.lucene.autocomplete;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexWriter;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.lucene.indexaccessor.IndexAccessor;
import com.gentics.cr.lucene.indexer.index.LuceneIndexLocation;
import com.gentics.cr.util.indexing.AbstractUpdateCheckerJob;
import com.gentics.cr.util.indexing.IndexLocation;

/**
 * This job is used to clear a lucene autocomplete index.
 * 
 * @author Sebastian Vogel <s.vogel@gentics.com>
 * 
 */
public class AutocompleteIndexDeleteJob extends AbstractUpdateCheckerJob {

	private AutocompleteIndexExtension autocompleter;

	/**
	 * Constructor.
	 * 
	 * @param config
	 *            configuration
	 * @param indexLoc
	 *            indexLocation
	 * @param autocompleter
	 *            the {@link AutocompleteIndexExtension} to clear
	 */
	public AutocompleteIndexDeleteJob(final CRConfig config, final IndexLocation indexLoc,
		AutocompleteIndexExtension autocompleter) {
		super(config, indexLoc, null);
		log = Logger.getLogger(AutocompleteIndexDeleteJob.class);

		this.identifyer = identifyer.concat(":clear");
		this.autocompleter = autocompleter;
	}

	/**
	 * starts the job - is called by the IndexJobQueue
	 */
	@Override
	protected final void indexCR(final IndexLocation indexLocation, final CRConfigUtil config) throws CRException {

		log.debug("Starting to clear index.");
		LuceneIndexLocation autocompleteLocation = autocompleter.getAutocompleteLocation();
		IndexAccessor ia = autocompleteLocation.getAccessor();
		IndexWriter writer = null;
		try {
			writer = ia.getWriter();
			writer.deleteAll();
			autocompleteLocation.resetIndexJobCreationTimes();
			autocompleteLocation.createReopenFile();
		} catch (IOException e) {
			log.error("Could not clear index", e);
		} finally {
			ia.release(writer);
		}
		log.debug("Finished clearing index.");

	}

}
