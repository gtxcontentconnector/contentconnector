package com.gentics.cr.lucene.search;

import java.util.HashMap;

import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;

/**
 * {@link CRResolvableBean} for the Metaresolvable passed to the search result
 * in the {@link LuceneRequestProcessor}.
 * @author PERHAB
 *
 */
public class CRMetaResolvableBean extends CRResolvableBean {

  /**
   * Generated serialVersionUID.
   */
  private static final long serialVersionUID = 5942419514161041700L;

  /**
   * initialize the MetaResolvable for the search result.
   * @param searchResult search result to get the hit count and suggestions from
   * @param request Search request to get the initial query from
   * @param start index of first result to return in the result
   * @param count number of items to return in the result
   */
  public CRMetaResolvableBean(final HashMap<String, Object> searchResult,
      final CRRequest request,final int start, final int count) {
    set(CRSearcher.RESULT_HITS_KEY, searchResult.get(CRSearcher.RESULT_HITS_KEY));
    set(LuceneRequestProcessor.META_START_KEY, start);
    set(LuceneRequestProcessor.META_COUNT_KEY, count);
    set(LuceneRequestProcessor.META_QUERY_KEY, request.getRequestFilter());
    set(CRSearcher.RESULT_SUGGESTIONS_KEY,
        searchResult.get(CRSearcher.RESULT_SUGGESTIONS_KEY));
    set(CRSearcher.RESULT_MAXSCORE_KEY,
        searchResult.get(CRSearcher.RESULT_MAXSCORE_KEY));
    set(CRSearcher.RESULT_BESTQUERY_KEY,
        searchResult.get(CRSearcher.RESULT_BESTQUERY_KEY));
    set(CRSearcher.RESULT_BESTQUERYHITS_KEY,
        searchResult.get(CRSearcher.RESULT_BESTQUERYHITS_KEY));
  }

}
