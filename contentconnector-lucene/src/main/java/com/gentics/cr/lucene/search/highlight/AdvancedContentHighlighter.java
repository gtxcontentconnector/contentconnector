package com.gentics.cr.lucene.search.highlight;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.TokenGroup;

import com.gentics.cr.configuration.GenericConfiguration;

public abstract class AdvancedContentHighlighter extends ContentHighlighter {

  /**
   * Log4j logger for error and debug messages.
   */
  private static Logger logger =
    Logger.getLogger(AdvancedContentHighlighter.class);

  protected AdvancedContentHighlighter(GenericConfiguration config) {
    super(config);
  }

  @Override
  public final String highlight(final String attribute, final Query parsedQuery)
  {
    logger.error("Please use the highlight(Query, IndexReader, int, String)"
        + " method instead of highlight(String, Query)");
    return null;
  }

  public abstract String highlight(Query query, final IndexReader reader,
      final int docId, final String fieldName);

  public abstract String highlightTerm(String arg0, TokenGroup arg1);

}
