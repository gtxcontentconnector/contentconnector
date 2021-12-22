package com.gentics.cr.lucene.synonyms;

import java.io.IOException;

import org.apache.lucene.index.IndexWriter;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.lucene.indexaccessor.IndexAccessor;
import com.gentics.cr.util.indexing.AbstractUpdateCheckerJob;
import com.gentics.cr.util.indexing.IndexLocation;
import com.gentics.lib.log.NodeLogger;

/**
 * This job is used to clear a lucene synonym index.
 * 
 * @author Patrick Höfer <p.hoefer@gentics.com>
 */
public class SynonymIndexDeleteJob extends AbstractUpdateCheckerJob {

	/**
	 * The SynonymIndexExtension as a attribute.
	 */
	private SynonymIndexExtension synonym;

	/**
	 * Constructor.
	 * 
	 * @param config configuration
	 * @param indexLoc indexLocation
	 * @param synonym the {@link com.gentics.cr.lucene.didyoumean.SynonymIndexExtension} to clear
	 */
	public SynonymIndexDeleteJob(final CRConfig config, final IndexLocation indexLoc,
			SynonymIndexExtension synonym) {
		super(config, indexLoc, null);
		log = NodeLogger.getNodeLogger(SynonymIndexDeleteJob.class);

		this.identifyer = identifyer.concat(":clear");
		this.synonym = synonym;
	}

	/**
	 * starts the job - is called by the IndexJobQueue.
	 * 
	 * @param indexLocation the indexLocation which will be Indexed
	 * @param config the config
	 * 
	 * @throws CRException if something unexpected will happen
	 */
	@Override
	protected final void indexCR(final IndexLocation indexLocation, final CRConfigUtil config) throws CRException {
		log.debug("Starting to clear Extension-index.");
		
		IndexAccessor accessor = null;
		IndexWriter writer = null;
		try {
			accessor = synonym.getSynonymLocation().getAccessor();
			writer = accessor.getWriter();
			writer.deleteAll();
			writer.commit();			
			indexLocation.createReopenFile();
			synonym.getSynonymLocation().resetIndexJobCreationTimes();
		} catch (IOException e) {
			log.error("Could not clear extension-index", e);
		} finally {
			if (accessor != null && writer != null) {
				accessor.release(writer);
			}
		}
		
		log.debug("Finished clearing Extension-index.");

	}

}
