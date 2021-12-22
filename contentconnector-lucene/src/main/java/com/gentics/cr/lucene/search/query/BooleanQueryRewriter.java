package com.gentics.cr.lucene.search.query;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import com.gentics.lib.log.NodeLogger;

public class BooleanQueryRewriter {

	/**
	 * Log4j logger for error and debug messages.
	 */
	private static final NodeLogger LOGGER = NodeLogger.getNodeLogger(BooleanQueryRewriter.class);

	/**
	 * private constructor to prevents generating an instance.
	 */
	private BooleanQueryRewriter() {
	}

	/**
	 * Replace all occurences of the needle with the replacement in the query; cleans query of empty clauses.
	 * @param query - lucene query to rewrite
	 * @param needle - lucene term to replace
	 * @param replacement - replacement term to replace the needle in the query.
	 * @return rewritten query, the original query if the Query is not an instance of BooleanQuery or TermQuery
	 */
	public static Query replaceTerm(Query query, Term needle, Term replacement) {
		HashMap<Term, Term> replacements = new HashMap<Term, Term>(1);
		replacements.put(needle, replacement);
		return replaceTerms(query, replacements);
	}

	/**
	 * Replace all occurences of the given terms in the query; cleans query of empty clauses.
	 * @param query - lucene query to rewrite
	 * @param replacements - map consisting of entries of terms, the key is the term to replace,
	 * the value is the replacement.
	 * @return rewritten query, the original query if the Query is not an instance of BooleanQuery or TermQuery
	 */
	public static Query replaceTerms(Query query, Map<Term, Term> replacements) {
		if (LOGGER.isDebugEnabled()) {
			for (Term origTerm : replacements.keySet()) {
				LOGGER.debug("Replace " + origTerm + " by " + replacements.get(origTerm));
			}
			LOGGER.debug("\tin " + query);
		}
		if (query instanceof TermQuery) {
			for (Term origTerm : replacements.keySet()) {
				if (((TermQuery) query).getTerm().compareTo(origTerm) == 0) {
					return new TermQuery(replacements.get(origTerm));
				}
			}
		} else if (query instanceof BooleanQuery) {
			BooleanQuery newQuery = new BooleanQuery();
			for (BooleanClause clause : ((BooleanQuery) query).getClauses()) {
				// recursive function call to handle all terms of the query
				Query replacedTerms = replaceTerms(clause.getQuery(), replacements);

				// we need to check if the query is null because 
				// removeEmptyClauses returns null if there are no clauses in it (except empty ones)
				if (replacedTerms != null) {
					BooleanClause newClause = new BooleanClause(replacedTerms, clause.getOccur());
					newQuery.add(newClause);
				}
			}
			// if the query contains empty clauses we want them removed
			// if the query does not contain any non empty clauses this method returns null
			newQuery = removeEmptyClauses(newQuery);
			return newQuery;
		} else {
			LOGGER.error("Cannot rewrite query '" + query + "' because it is an unkonwn type of query.");
		}
		return query;
	}

	/**
	 * Remove all empty clauses from the query.
	 * @param query Query to be cleaned up
	 * @return Null if the query only consists of empty clauses or the cleaned up query.
	 */
	public static BooleanQuery removeEmptyClauses(final BooleanQuery query) {
		BooleanQuery cleanedQuery = new BooleanQuery();

		BooleanClause[] clauses = query.getClauses();
		int clauseCount = 0;
		for (BooleanClause clause : clauses) {
			if (clause != null && !clause.toString().equals("")) {
				cleanedQuery.add(clause);
				clauseCount++;
			}
		}
		if (clauseCount == 0) {
			return null;
		} else {
			return cleanedQuery;
		}
	}
}
