package com.gentics.cr.lucene.search.query;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

public class BooleanQueryRewriter {
	
	/**
	 * Log4j logger for error and debug messages.
	 */
	private static final Logger LOGGER = Logger.getLogger(BooleanQueryRewriter.class);

	/**
	 * private constructor to prevents generating an instance.
	 */
	private BooleanQueryRewriter() { }

	/**
	 * Replace all occurences of the needle with the replacement in the query.
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
	 * Replace all occurences of the given terms in the query.
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
			for(Term origTerm : replacements.keySet()) {
				if (((TermQuery) query).getTerm().compareTo(origTerm) == 0) {
					return new TermQuery(replacements.get(origTerm));
				}
			}
		} else if (query instanceof BooleanQuery) {
			BooleanQuery newQuery = new BooleanQuery();
			for (BooleanClause clause : ((BooleanQuery) query).getClauses()) {
				newQuery.add(new BooleanClause(replaceTerms(clause.getQuery(), replacements), clause.getOccur()));
			}
			return newQuery;
		} else {
			LOGGER.error("Cannot rewrite query '" + query + "' because it is an unkonwn type of query.");
		}
		return query;
	}
}
