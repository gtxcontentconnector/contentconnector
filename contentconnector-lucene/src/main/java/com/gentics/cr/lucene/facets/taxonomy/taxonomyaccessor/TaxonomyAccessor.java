package com.gentics.cr.lucene.facets.taxonomy.taxonomyaccessor;

import java.io.IOException;
import java.util.Collection;

import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;

import com.gentics.cr.lucene.facets.taxonomy.TaxonomyMapping;

/**
 * An TaxonomyAccessor coordinates access to Writers and Readers in a way that
 * allows multiple threads to share the same access objects. Also, Readers are
 * refreshed after a Writer is released.
 * 
 * <pre>
 * // get writer instance for both the index and the taxonomy
 * IndexWriter indexWriter = indexAccessor.getWriter..&lt;br/&gt;
 * &lt;br/&gt;
 * ...&lt;br/&gt;
 * &lt;br/&gt;
 * TaxonomyWriter taxoWriter = taxoAccessor.getWriter(); &lt;br/&gt;
 * &lt;br/&gt;
 * try {&lt;br/&gt;
 *   //build the category paths...&lt;br/&gt;
 *   CategoryDocumentBuilder categoryDocBuilder = new CategoryDocumentBuilder(taxoWriter);&lt;br/&gt;
 *   categoryDocBuilder.setCategoryPaths(categories);&lt;br/&gt;
 *   &lt;br/&gt;
 *   //combine document and taxonomy
 *   categoryDocBuilder.build(doc);&lt;br/&gt;
 *   &lt;br/&gt;
 *   //and add document to index
 *   indexWriter.addDocument(doc);&lt;br/&gt;
 * } finally {&lt;br/&gt;
 *   indexAccessor.release(indexWriter);&lt;br/&gt;
 *   taxoAccessor.release(taxoWriter);&lt;br/&gt;
 * }
 * </pre>
 * 
 * $Date$
 * 
 * @version $Revision$
 * @author Sebastian Vogel <s.vogel@gentics.com>
 */
public interface TaxonomyAccessor {

	/**
	 * Return the {@link TaxonomyReader} or a new one if it hasn't been opened
	 * already.
	 * 
	 * @return the new or cached reader
	 */
	public abstract TaxonomyReader getTaxonomyReader() throws IOException;

	/**
	 * Return the {@link TaxonomyWriter} or a new one if it hasn't been opened
	 * already.
	 * 
	 * @return the new or cached writer
	 */
	public abstract TaxonomyWriter getTaxonomyWriter() throws IOException;

	/**
	 * Release the opened {@link TaxonomyReader}
	 * 
	 * @param reader
	 *            the opened reader
	 */
	public abstract void release(TaxonomyReader reader);

	/**
	 * Release the opened {@link TaxonomyWriter}
	 * 
	 * @param reader
	 *            the opened writer
	 */
	public abstract void release(TaxonomyWriter writer);

	/**
	 * Releases any resources held by this IndexAccessor.
	 */
	public abstract void close();

	/**
	 * returns the writer use count
	 * 
	 * @return the use count
	 */
	public abstract int writerUseCount();

	/**
	 * tests if the {@link TaxonomyAccessor} is open
	 * 
	 * @return true if {@link TaxonomyAccessor} is open
	 */
	public abstract boolean isOpen();

	/**
	 * test if the {@link TaxonomyAccessor} is locked
	 * 
	 * @return true if the accessor is locked
	 */
	public abstract boolean isLocked();

	/**
	 * open the {@link TaxonomyAccessor}
	 */
	public abstract void open();

	/**
	 * Adds a {@link TaxonomyMapping} to the mappings collection in the
	 * {@link TaxonomyAccessor}
	 * 
	 * @param mapping
	 *            the mapping to add to the accessor
	 */
	public abstract void addTaxonomyMapping(TaxonomyMapping mapping);

	/**
	 * Returns the {@link Collection} of {@link TaxonomyMapping} stored in the
	 * {@link TaxonomyAccessor}
	 * 
	 * @return {@link Collection} of {@link TaxonomyMapping}
	 */
	public abstract Collection<TaxonomyMapping> getTaxonomyMappings();

	/**
	 * Adds a collection of {@link TaxonomyMapping} to the existing collection
	 * of mappings in the {@link TaxonomyAccessor}
	 * 
	 * @param mappings
	 *            {@link Collection} of {@link TaxonomyMapping}
	 */
	public abstract void addTaxonomyMappings(
			Collection<? extends TaxonomyMapping> mappings);

	/**
	 * refreshes the underlying {@link TaxonomyReader}s
	 */
	public abstract void refresh();

	/**
	 * Clears the Taxonomy. This method should only be called if the main index
	 * was cleared before. Afterwards the taxonomy should also be refreshed, do
	 * this after IndexWriter was reopened. Use {@link TaxonomyAccessor}
	 * refresh() method to do this.
	 */
	public abstract void clearTaxonomy();

}