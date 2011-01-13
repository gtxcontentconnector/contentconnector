package com.gentics.cr.lucene.search.highlight;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;

import com.gentics.cr.configuration.GenericConfiguration;
/**
 * Content highlighter with index reader.
 * @author Christopher
 *
 */
public abstract class AdvancedContentHighlighter extends ContentHighlighter {

  /**
   * Log4j logger for error and debug messages.
   */
  private static Logger logger =
    Logger.getLogger(AdvancedContentHighlighter.class);

  /**
   * Constructor.
   * @param config config
   */
  protected AdvancedContentHighlighter(final GenericConfiguration config) {
    super(config);
  }

  @Override
  public final String highlight(final String attribute,
		  final Query parsedQuery) {
    logger.error("Please use the highlight(Query, IndexReader, int, String)"
        + " method instead of highlight(String, Query)");
    return null;
  }
/**
 * highlighter method.
 * @param query query
 * @param reader reader
 * @param docId docid
 * @param fieldName field name
 * @return highlighted string
 */
  public abstract String highlight(Query query, final IndexReader reader,
      final int docId, final String fieldName);

}
