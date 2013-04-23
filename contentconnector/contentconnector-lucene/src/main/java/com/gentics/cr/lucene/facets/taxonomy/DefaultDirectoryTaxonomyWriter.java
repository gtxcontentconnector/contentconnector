package com.gentics.cr.lucene.facets.taxonomy;

import java.io.IOException;

import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.facet.taxonomy.writercache.TaxonomyWriterCache;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;

/**
 * This is an extension of the {@link LuceneTaxonomyWriter} to provide a delete
 * taxonomy function
 * 
 * @author Sebastian Vogel <s.vogel@gentics.com>
 * 
 */
public class DefaultDirectoryTaxonomyWriter extends DirectoryTaxonomyWriter {
	
	private Directory dir;
	private OpenMode openMode;

	// convenience constructor
	public DefaultDirectoryTaxonomyWriter(Directory directory)
			throws CorruptIndexException, LockObtainFailedException,
			IOException {
		super(directory);
		this.dir = directory;
		
	}

	/**
	 * Overwrite constructor of {@link LuceneTaxonomyWriter}
	 * 
	 * @see LuceneTaxonomyWriter#LuceneTaxonomyWriter(Directory, OpenMode)
	 * @param directory
	 * @param openMode
	 * @param cache
	 * @throws CorruptIndexException
	 * @throws LockObtainFailedException
	 * @throws IOException
	 */
	public DefaultDirectoryTaxonomyWriter(Directory directory, OpenMode openMode,
			TaxonomyWriterCache cache) throws CorruptIndexException,
			LockObtainFailedException, IOException {
		super(directory, openMode, cache);
		this.dir = directory;
		this.openMode = openMode;
	}

	/**
	 * Overwrite constructor of {@link LuceneTaxonomyWriter}
	 * 
	 * @see LuceneTaxonomyWriter#LuceneTaxonomyWriter(Directory, OpenMode, TaxonomyWriterCache)
	 * @param directory
	 * @param openMode
	 * @throws CorruptIndexException
	 * @throws LockObtainFailedException
	 * @throws IOException
	 */
	public DefaultDirectoryTaxonomyWriter(Directory directory, OpenMode openMode)
			throws CorruptIndexException, LockObtainFailedException,
			IOException {
		super(directory, openMode);
		this.dir = directory;
		this.openMode = openMode;
	}

	/**
	 * <p>
	 * Delete all category paths in the taxonomy.
	 * </p>
	 * 
	 * <p>
	 * Calls deleteAll() on the {@link IndexWriter} used by the
	 * {@link LuceneTaxonomyWriter} to manage the taxonomy
	 * </p>
	 * 
	 * @see IndexWriter#deleteAll()
	 * @throws IOException
	 * @throws IllegalStateException
	 * @author Sebastian Vogel <s.vogel@gentics.com>
	 */
	public synchronized IndexWriter getIndexWriter() throws IOException,
			IllegalStateException {
		this.close();
		IndexWriter indexWriter = this.openIndexWriter(this.dir, this.openMode);
		
		if (indexWriter != null) {
			return indexWriter;
						
		} else {
			throw new IllegalStateException("TaxonomyIndexWriter ist null");
		}
	}

}
