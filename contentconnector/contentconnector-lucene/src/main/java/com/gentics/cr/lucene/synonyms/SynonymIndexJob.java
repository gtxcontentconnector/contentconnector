package com.gentics.cr.lucene.synonyms;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.lucene.indexaccessor.IndexAccessor;
import com.gentics.cr.monitoring.MonitorFactory;
import com.gentics.cr.monitoring.UseCase;
import com.gentics.cr.util.indexing.AbstractUpdateCheckerJob;
import com.gentics.cr.util.indexing.IndexLocation;

/**
 * This job is used to re-index (or newly index) the synonym-index.
 * 
 * @author Patrick Höfer <p.hoefer@gentics.com>
 */
public class SynonymIndexJob extends AbstractUpdateCheckerJob {

	/**
	 * Reference to the SynonymIndexExtension.
	 */
	private SynonymIndexExtension synonym;
	
	/**
	 * Config-Key for Rule in Config.
	 */
	private static final String RULE_KEY = "rule";
	
	/**
	 * Config-Key for Deskriptor in Config-File.
	 */
	public static final String DESCRIPTOR_NAME_KEY = "descriptorColumnName";
	
	/**
	 * Config-Key for Synonym in Config-File.
	 */
	public static final String SYNONYM_NAME_KEY = "synonymColumnName";

	/**
	 * The RequestProcessor as attribute.
	 */
	private RequestProcessor rp = null;

	/**
	 * Constructor.
	 * 
	 * @param updateCheckerConfig configuration
	 * @param indexLoc indexLocation
	 * @param synonym the {@link com.gentics.cr.lucene.didyoumean.SynonymIndexExtension} to clear
	 */
	public SynonymIndexJob(final CRConfig updateCheckerConfig, final IndexLocation indexLoc, final SynonymIndexExtension synonym) {
		super(updateCheckerConfig, indexLoc, null);

		this.identifyer = identifyer.concat(":reIndex");
		log = Logger.getLogger(SynonymIndexJob.class);
		this.synonym = synonym;

		try {
			rp = config.getNewRequestProcessorInstance(1);
		} catch (CRException e) {
			log.error("Could not create RequestProcessor instance." + config.getName(), e);
		}
	}

	/**
	 * starts the job - is called by the IndexJobQueue.
	 */
	@Override
	protected final void indexCR(final IndexLocation indexLocation, final CRConfigUtil config) throws CRException {
		try {
			reIndex();
		} catch (IOException e) {
			throw new CRException("Could not access the SYN - index! " + e.getMessage());
		}

	}

	/**
	 * reindex the Syn-Index and write it into synonymLocation.
	 * Write Documents with Deskriptor and Synonym Field, where Deskripor is the base word and Synonym its Synonym
	 * 
	 * @throws IOException if there is a Problem when accessing the Index
	 */
	private synchronized void reIndex() throws IOException {
		UseCase ucReIndex = MonitorFactory.startUseCase("reIndex()");
		
		// build a dictionary (from the spell package)
		log.debug("Starting to reindex SYN index.");
		IndexAccessor synonymAccessor = synonym.getSynonymLocation().getAccessor();

		IndexWriter synonymWriter = synonymAccessor.getWriter();
		Collection<CRResolvableBean> objectsToIndex = null;
		try {
			if (rp == null) {
				throw new CRException("FATAL ERROR", "RequestProcessor not available");
			}

			// and get the current rule
			String rule = (String) config.get(RULE_KEY);
			if (rule == null) {
				rule = "";
			}
			if (rule.length() == 0 || rule == null) {
				rule = "1 == 1";
			}


			try {
				CRRequest req = new CRRequest();
				req.setRequestFilter(rule);
				status.setCurrentStatusString("SYN Get objects to update " + "in the index ...");
				objectsToIndex = getObjectsToUpdate(req, rp, true, null);
			} catch (Exception e) {
				log.error("ERROR while cleaning SYN index", e);
			}


			if (objectsToIndex == null) {
				log.debug("SYN Rule returned no objects to index. Skipping...");
				return;
			}

			status.setObjectCount(objectsToIndex.size());
			log.debug("SYN index job with " + objectsToIndex.size() + " objects to index.");

			String descriptorName = (String) config.get(DESCRIPTOR_NAME_KEY);
			String synonymName = (String) config.get(SYNONYM_NAME_KEY);


			status.setCurrentStatusString("Starting to index slices.");
			int objCount = 0;
			try {
				for (Iterator<CRResolvableBean> iterator = objectsToIndex.iterator(); iterator.hasNext();) {
					CRResolvableBean bean = iterator.next();
					iterator.remove();
					objCount++;
					String descriptorValue = bean.getString(descriptorName);
					String synonymValue = bean.getString(synonymName);
					if (descriptorValue != null && synonymValue != null) {
						descriptorValue = descriptorValue.toLowerCase();
						if (synonymValue != null) {
							synonymValue = synonymValue.toLowerCase();
						}
						Document doc = new Document();
						doc.add(new Field("Deskriptor", descriptorValue, Field.Store.YES, Field.Index.NOT_ANALYZED));
						doc.add(new Field("Synonym", synonymValue, Field.Store.YES, Field.Index.NOT_ANALYZED));
						synonymWriter.addDocument(doc);
						log.debug("WRITE SYN " + objCount + " " + descriptorValue + " " + synonymValue);
						synonymWriter.commit();
						log.debug("Number of actual Synonym: " + synonymWriter.numDocs());
					}
				}
			} finally {
				// if documents where added to the index create a reopen file and
				// optimize the writer
				log.debug("Number of indexed Synonyms finished: " + synonymWriter.numDocs());
				synonymAccessor.release(synonymWriter);
			}
	
			log.debug("Finished reindexing synonym index.");
			ucReIndex.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
