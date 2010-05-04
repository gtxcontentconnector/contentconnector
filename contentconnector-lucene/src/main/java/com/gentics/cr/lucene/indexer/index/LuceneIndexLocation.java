package com.gentics.cr.lucene.indexer.index;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;

import com.gentics.cr.CRConfig;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.indexaccessor.IndexAccessor;
import com.gentics.cr.util.indexing.IndexLocation;

/**
 * 
 * Last changed: $Date: 2009-07-10 10:49:03 +0200 (Fr, 10 Jul 2009) $
 * 
 * @version $Revision: 131 $
 * @author $Author: supnig@constantinopel.at $
 * 
 */

public abstract class LuceneIndexLocation extends
		com.gentics.cr.util.indexing.IndexLocation {
	// STATIC MEMBERS
	protected static final Logger log = Logger
			.getLogger(LuceneIndexLocation.class);
	protected static final String RAM_IDENTIFICATION_KEY = "RAM";

	protected String name = null;

	protected Analyzer getConfiguredAnalyzer() {
		return LuceneAnalyzerFactory
				.createAnalyzer((GenericConfiguration) config);
	}

	/**
	 * Checks Lock and throws Exception if Lock exists
	 * 
	 * @throws LockedIndexException
	 * @throws IOException
	 */
	public void checkLock() throws LockedIndexException {
		IndexAccessor indexAccessor = getAccessor();
		IndexWriter indexWriter;
		try {
			indexWriter = indexAccessor.getWriter();
			indexAccessor.release(indexWriter);
		} catch (LockObtainFailedException e) {
			throw new LockedIndexException(e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	// TODO should be protected, is public because it is used in
	// IndexLocation#createNewIndexLocation
	/**
	 * @param config
	 * 
	 */
	public LuceneIndexLocation(CRConfig config) {
		super(config);

		name = config.getName();
	}

	public static synchronized LuceneIndexLocation getIndexLocation(
			CRConfig config) {
		IndexLocation genericIndexLocation = IndexLocation
				.getIndexLocation(config);
		if (genericIndexLocation instanceof LuceneIndexLocation) {
			return (LuceneIndexLocation) genericIndexLocation;
		} else {
			log
					.error("IndexLocation is not created for Lucene. Using the "
							+ CRLuceneIndexJob.class.getName()
							+ " requires that you use the "
							+ LuceneIndexLocation.class.getName()
							+ ". You can configure another Job by setting the "
							+ IndexLocation.UPDATEJOBCLASS_KEY
							+ " key in your config.");
			return null;
		}
	}

	protected Directory createRAMDirectory() {
		Directory dir = new RAMDirectory();
		log.debug("Creating RAM Directory for Index [" + name + "]");
		return (dir);
	}

	protected Directory createFSDirectory(File indexLoc) throws IOException {
		if (!indexLoc.exists()) {
			log.debug("Indexlocation did not exist. Creating directories...");
			indexLoc.mkdirs();
		}
		Directory dir = FSDirectory.open(indexLoc);
		log.debug("Creating FS Directory for Index [" + name + "]");
		return (dir);
	}

	/**
	 * Returns the directory used by this index location
	 * 
	 * @return
	 */
	protected abstract Directory[] getDirectories();

	/**
	 * Get number of documents in Index
	 * 
	 * @return doccount
	 */
	public abstract int getDocCount();

	protected abstract IndexAccessor getAccessorInstance();

	/**
	 * Returns an index Accessor, which can be used to share access to an index
	 * over multiple threads
	 * 
	 * @return
	 */
	public IndexAccessor getAccessor() {
		IndexAccessor indexAccessor = getAccessorInstance();
		reopenCheck(indexAccessor);
		return indexAccessor;
	}

	/**
	 * Checks for reopen file and reopens indexAccessor
	 * 
	 * @param indexAccessor
	 * @return true if indexAccessor has been reopened
	 */
	public abstract boolean reopenCheck(IndexAccessor indexAccessor);

	/**
	 * Tests if the IndexLocation contains an existing Index and returns true if
	 * it does.
	 * 
	 * @return true if index exists, otherwise false
	 */
	public boolean isContainingIndex() {

		return getDocCount() > 0;
	}

}
