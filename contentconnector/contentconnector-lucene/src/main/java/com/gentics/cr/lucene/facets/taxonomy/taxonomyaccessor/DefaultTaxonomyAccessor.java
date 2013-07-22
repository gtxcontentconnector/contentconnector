package com.gentics.cr.lucene.facets.taxonomy.taxonomyaccessor;

import java.io.IOException;
import java.util.Collection;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.facet.taxonomy.InconsistentTaxonomyException;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.LogByteSizeMergePolicy;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

import com.gentics.cr.lucene.facets.taxonomy.TaxonomyMapping;

/**
 * provides the default implementation for the {@link TaxonomyAccessor}
 * 
 * $Date$
 * 
 * @version $Revision$
 * @author Sebastian Vogel <s.vogel@gentics.com>
 */
public class DefaultTaxonomyAccessor implements TaxonomyAccessor {
	private static final OpenMode DEFAULT_WRITER_OPEN_MODE = OpenMode.CREATE_OR_APPEND;

	// TODO: what about searchers do i need to consider those

	/**
	 * Log4j logger for error and debug messages.
	 */
	private static final Logger LOGGER = Logger
			.getLogger(DefaultTaxonomyAccessor.class);

	// TODO: needed?
	private static final int POOL_SIZE = 10;

	// TODO: needed?
	// private Analyzer analyzer;

	private TaxonomyReader taxoReader = null;

	private TaxonomyWriter taxoWriter = null;

	protected boolean closed = true;

	private Directory directory;

	protected int readerUseCount = 0;

	// TODO: needed?
	protected final ExecutorService pool;

	protected int numReopening = 0;

	protected boolean isReopening = false;

	protected int writerUseCount = 0;

	protected int numSearchersForRetirment = 0;

	protected OpenMode writerOpenMode = null;

	protected static boolean useFacets = false;

	private Vector<TaxonomyMapping> taxonomyMappings = new Vector<TaxonomyMapping>();

	public DefaultTaxonomyAccessor(final OpenMode openMode, Directory dir) {
		directory = dir;
		pool = Executors.newFixedThreadPool(POOL_SIZE);
		this.writerOpenMode = openMode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gentics.cr.lucene.taxonomyaccessor.TaxonomyAccessor#getTaxonomyReader
	 * ()
	 */
	public synchronized TaxonomyReader getTaxonomyReader() throws IOException {
		checkClosed();

		if (taxoReader != null) {
			LOGGER.debug("returning cached taxonomy reader");
			readerUseCount++;
		} else {
			LOGGER.debug("opening new taxonomy reader and caching it");

			taxoReader = new DirectoryTaxonomyReader(directory);
			readerUseCount = 1;
		}
		notifyAll();
		return taxoReader;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gentics.cr.lucene.taxonomyaccessor.TaxonomyAccessor#getTaxonomyWriter
	 * ()
	 */
	public synchronized TaxonomyWriter getTaxonomyWriter() throws IOException {
		OpenMode openMode = writerOpenMode;

		if (openMode == null) {
			openMode = DEFAULT_WRITER_OPEN_MODE;
		}

		checkClosed();

		while (writerUseCount > 0) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}

		if (taxoWriter != null) {
			LOGGER.debug("returning cached writer:"
					+ Thread.currentThread().getId());

			writerUseCount++;
		} else {
			LOGGER.debug("opening new writer and caching it:"
					+ Thread.currentThread().getId());

			taxoWriter = new DirectoryTaxonomyWriter(directory, openMode);
			writerUseCount = 1;
		}

		notifyAll();
		return taxoWriter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gentics.cr.lucene.taxonomyaccessor.TaxonomyAccessor#release(org.apache
	 * .lucene.facet.taxonomy.TaxonomyReader)
	 */
	public synchronized void release(TaxonomyReader reader) {
		if (reader == null) {
			return;
		}

		if (reader != taxoReader) {
			throw new IllegalArgumentException(
					"reading reader not opened by this index accessor");
		}

		readerUseCount--;
		notifyAll();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gentics.cr.lucene.taxonomyaccessor.TaxonomyAccessor#release(org.apache
	 * .lucene.facet.taxonomy.TaxonomyWriter)
	 */
	public synchronized void release(TaxonomyWriter writer) {

		try {
			if (writer != taxoWriter) {
				throw new IllegalArgumentException(
						"writer not opened by this index accessor");
			}
			writerUseCount--;

			if (writerUseCount == 0) {
				LOGGER.debug("closing cached writer:"
						+ Thread.currentThread().getId());

				try {
					taxoWriter.close();
				} catch (IOException e) {
					LOGGER.error("error closing cached Writer:"
							+ Thread.currentThread().getId(), e);
				} finally {
					taxoWriter = null;
				}
			}

		} finally {
			notifyAll();
		}

		if (writerUseCount == 0) {
			numReopening++;
			pool.execute(new Runnable() {
				public void run() {
					synchronized (DefaultTaxonomyAccessor.this) {
						if (numReopening > 5) {
							// there are too many reopens pending, so just bail
							numReopening--;
							return;
						}
						waitForReadersAndRefreshCached();
						numReopening--;
						DefaultTaxonomyAccessor.this.notifyAll();
					}
				}
			});

		}

	}

	/** This method assumes it is invoked in a synchronized context. */
	private void waitForReadersAndRefreshCached() {
		while (readerUseCount > 0) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
		if (numReopening > 1) {
			// there are other calls to reopen pending, so we can bail
			return;
		}
		refreshTaxonomyReader();
	}

	/**
	 * Reopens the cached reading Reader. This method assumes it is invoked in a
	 * synchronized context.
	 */
	private void refreshTaxonomyReader() {
		if (taxoReader == null) {
			return;
		}

		LOGGER.debug("refreshing taxonomy reader");
		try {
			taxoReader.refresh();
		} catch (IOException e) {
			LOGGER.error("error refreshing taxonomy Reader", e);
		} catch (InconsistentTaxonomyException e) {
			LOGGER.info("inconsistent taxononmy found when trying to refresh it", e);
			closeTaxonomyReader();
		} catch(Exception e) {
			LOGGER.info("Could not refresh TaxonomyReader - closing it", e);
			closeTaxonomyReader();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gentics.cr.lucene.taxonomyaccessor.TaxonomyAccessor#close()
	 */
	public synchronized void close() {

		if (closed) {
			return;
		}
		closed = true;
		while ((readerUseCount > 0) || (writerUseCount > 0)
				|| (numReopening > 0)) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}

		closeTaxonomyReader();
		closeTaxonomyWriter();
		shutdownAndAwaitTermination(pool);
	}

	/**
	 * Closes the cached reading Reader if it has been created. This method
	 * assumes it is invoked in a synchronized context.
	 */
	protected void closeTaxonomyReader() {
		if (taxoReader == null) {
			return;
		}
		LOGGER.debug("closing cached taxonomy reader");

		try {
			taxoReader.close();
		} catch (IOException e) {
			LOGGER.error("error closing taxonomy Reader", e);
		} finally {
			taxoReader = null;
		}
	}

	/**
	 * Closes the cached Writer if it has been created. This method is invoked
	 * in a synchronized context.
	 */
	protected void closeTaxonomyWriter() {
		if (taxoWriter == null) {
			return;
		}
		LOGGER.debug("closing cached taxonomy writer");

		try {
			taxoWriter.close();
		} catch (IOException e) {
			LOGGER.error("error closing cached taxonomy Writer", e);
		} finally {
			taxoWriter = null;
		}
	}

	protected void shutdownAndAwaitTermination(ExecutorService pool) {
		pool.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!pool.awaitTermination(20, TimeUnit.SECONDS)) {
				pool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!pool.awaitTermination(60, TimeUnit.SECONDS))
					System.err.println("Pool did not terminate");
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Throws an Exception if TaxonomyAccessor is closed. This method assumes it
	 * is invoked in a synchronized context.
	 */
	private void checkClosed() {
		if (closed) {
			throw new IllegalStateException("taxonomy accessor has been closed");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gentics.cr.lucene.taxonomyaccessor.TaxonomyAccessor#writerUseCount()
	 */
	public int writerUseCount() {
		return writerUseCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gentics.cr.lucene.taxonomyaccessor.TaxonomyAccessor#isOpen()
	 */
	public boolean isOpen() {
		return !closed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gentics.cr.lucene.taxonomyaccessor.TaxonomyAccessor#isLocked()
	 */
	public boolean isLocked() {
		boolean locked = false;
		try {
			locked = IndexWriter.isLocked(directory);
		} catch (IOException e) {
			LOGGER.error(e);
		}
		return locked;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gentics.cr.lucene.taxonomyaccessor.TaxonomyAccessor#open()
	 */
	public synchronized void open() {
		closed = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gentics.cr.lucene.taxonomyaccessor.TaxonomyAccessor#addTaxonomyMapping
	 * (com.gentics.cr.lucene.taxonomyaccessor.TaxonomyMapping)
	 */
	public void addTaxonomyMapping(TaxonomyMapping mapping) {
		taxonomyMappings.add(mapping);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gentics.cr.lucene.taxonomyaccessor.TaxonomyAccessor#getTaxonomyMappings
	 * ()
	 */
	public Collection<TaxonomyMapping> getTaxonomyMappings() {
		return taxonomyMappings;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gentics.cr.lucene.taxonomyaccessor.TaxonomyAccessor#addTaxonomyMappings
	 * (java.util.Collection)
	 */
	public void addTaxonomyMappings(
			Collection<? extends TaxonomyMapping> mappings) {
		taxonomyMappings.addAll(mappings);
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized void refresh() {
		waitForReadersAndRefreshCached();
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized void clearTaxonomy() {
		checkClosed();

		while (writerUseCount > 0) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
		closeTaxonomyWriter();
		
		// Workaround for missing delete all method in the TaxonomyWriter
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_30,
			        new KeywordAnalyzer()).setOpenMode(this.writerOpenMode).setMergePolicy(
			        new LogByteSizeMergePolicy());		 
		try {			
			IndexWriter indexWriter = new IndexWriter(directory, config);
			indexWriter.deleteAll();
			indexWriter.close();
			// the TaxonomyReader should be refreshed after this but only if the IndexReaders have been reopened before
		} catch (IOException e) {
			LOGGER.error("Could not clear the Taxonomy", e);
		}
		notifyAll();

	}
}
