package com.gentics.cr.lucene.indexer.index;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.lucene.facets.taxonomy.taxonomyaccessor.TaxonomyAccessor;
import com.gentics.cr.lucene.indexaccessor.IndexAccessor;
import com.gentics.cr.util.indexing.AbstractUpdateCheckerJob;
import com.gentics.cr.util.indexing.IndexLocation;

/**
 * This job is used to clear a lucene index.
 * @author Christopher
 *
 */
public class CRLuceneDeleteJob extends AbstractUpdateCheckerJob {

	/**
	 * Constructor.
	 * @param config configuration
	 * @param indexLoc indexLocation
	 * @param configmap index config map
	 */
	public CRLuceneDeleteJob(final CRConfig config, final IndexLocation indexLoc,
		final ConcurrentHashMap<String, CRConfigUtil> configmap) {
		super(config, indexLoc, configmap);
		log = Logger.getLogger(CRLuceneDeleteJob.class);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected final void indexCR(final IndexLocation indexLocation, final CRConfigUtil config) throws CRException {
		if (indexLocation instanceof LuceneIndexLocation) {
			log.debug("Starting to clear index.");
			LuceneIndexLocation luceneIndexLoccation = (LuceneIndexLocation) indexLocation;
			IndexAccessor ia = luceneIndexLoccation.getAccessor();
			IndexWriter writer = null;
			TaxonomyAccessor ta = null;
			if (luceneIndexLoccation.useFacets()) {
				ta = luceneIndexLoccation.getTaxonomyAccessor();
			}
			try {
				writer = ia.getWriter();
				writer.deleteAll();
				if (ta != null) {
					ta.clearTaxonomy();
				}
				luceneIndexLoccation.resetIndexJobCreationTimes();
			} catch (IOException e) {
				log.error("Could not clear index", e);
			} finally {
				ia.release(writer);
				if (ta != null) {
					ta.refresh();
				}
			}
			log.debug("Finished clearing index.");
		} else {
			log.error("Index does not seem to be a Lucene index. Therfore no " + "clearing will be done.");
		}
	}

}
