package com.gentics.cr.lucene.indexaccessor;

import java.io.IOException;
import java.util.Set;

import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.Directory;

/**
 * A MultiIndexAccessor allows you to retrieve a MultiSearcher across Multiple
 * indexes.
 * 
 * The MultiIndexAccessor is a convenience class that retrieves an IndexAccessor
 * from the IndexAccessorFactory for each underlying index and returns a
 * MultiSearcher across those indexes. Releasing the MultiSearcher releases all
 * of the associated IndexAccessors.
 * 
 * <pre>
 * Searcher searcher = multiIndexAccessor.getMultiSearcher(indexes); &lt;br/&gt;
 * try {&lt;br/&gt;
 *   searcher.search();&lt;br/&gt;
 * } finally {&lt;br/&gt;
 *   multiIndexAccessor.release(searcher);&lt;br/&gt;
 * }
 * </pre>
 * 
 */
public interface MultiIndexAccessor {

  /**
   * @param indexes
   * @return new or cached MultiSearcher
   * @throws IOException 
   */
  public Searcher getMultiSearcher(Set<Directory> indexes) throws IOException;

  /**
   * @param searcher
   */
  public void release(Searcher searcher);
}
