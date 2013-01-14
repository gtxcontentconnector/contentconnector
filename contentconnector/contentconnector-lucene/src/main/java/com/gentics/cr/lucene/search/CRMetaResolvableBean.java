package com.gentics.cr.lucene.search;

import java.util.HashMap;

import org.apache.lucene.search.Query;

import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.lucene.facets.search.FacetsSearchConfigKeys;

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
	 * Key for the value of the query parameter in the request wrapper to store
	 * in the meta resolvable.
	 */
	public static final String QUERY_PARAMETER_KEY = "queryParameter";

	/**
	 * Parameter in the request used for retrieving the query.
	 */
	public static final String REQUEST_QUERY_PARAMETER = "q";

	/**
	 * initialize the MetaResolvable for the search result.
	 * @param searchResult search result to get the hit count and suggestions from
	 * @param request Search request to get the initial query from
	 * @param start index of first result to return in the result
	 * @param count number of items to return in the result
	 */
	public CRMetaResolvableBean(final HashMap<String, Object> searchResult, final CRRequest request, final int start,
		final int count) {
		set(LuceneRequestProcessor.META_HITS_KEY, searchResult.get(CRSearcher.RESULT_HITS_KEY));
		set(CRSearcher.RESULT_HITS_KEY, searchResult.get(CRSearcher.RESULT_HITS_KEY));
		set(LuceneRequestProcessor.META_START_KEY, start);
		set(LuceneRequestProcessor.META_COUNT_KEY, count);
		set(LuceneRequestProcessor.META_QUERY_KEY, request.getRequestFilter());
		if (request.getRequestWrapper() != null) {
			set(QUERY_PARAMETER_KEY, request.getRequestWrapper().getParameter(REQUEST_QUERY_PARAMETER));
		}
		set(CRSearcher.RESULT_SUGGESTIONS_KEY, searchResult.get(CRSearcher.RESULT_SUGGESTIONS_KEY));
		set(CRSearcher.RESULT_MAXSCORE_KEY, searchResult.get(CRSearcher.RESULT_MAXSCORE_KEY));
		set(CRSearcher.RESULT_BESTQUERY_KEY, searchResult.get(CRSearcher.RESULT_BESTQUERY_KEY));
		set(CRSearcher.RESULT_BESTQUERYHITS_KEY, searchResult.get(CRSearcher.RESULT_BESTQUERYHITS_KEY));
		
		if (searchResult
				.containsKey(FacetsSearchConfigKeys.RESULT_FACETS_LIST_KEY)) {
			set(FacetsSearchConfigKeys.RESULT_FACETS_LIST_KEY,
					searchResult
							.get(FacetsSearchConfigKeys.RESULT_FACETS_LIST_KEY));
		}
	}

	/**
	 * initialize the MetaResolvable for the search result.
	 * @param searchResult search result to get the hit count and suggestions from
	 * @param request Search request to get the initial query from
	 * @param parsedQuery parsed query which is set to the object
	 * @param start index of first result to return in the result
	 * @param count number of items to return in the result
	 */
	public CRMetaResolvableBean(final HashMap<String, Object> searchResult, final CRRequest request,
		final Query parsedQuery, final int start, final int count) {
		this(searchResult, request, start, count);
		set(LuceneRequestProcessor.PARSED_QUERY_KEY, parsedQuery);
	}

}
