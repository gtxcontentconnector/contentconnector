package com.gentics.cr.lucene.search.query;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Version;

import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRRequest;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.indexaccessor.IndexAccessor;
import com.gentics.cr.lucene.indexer.index.LuceneIndexLocation;

/**
 * The SynonymQueryParser change the users Query,
 * if there are any synonyms for his searchTerm.
 *
 * @author Patrick Höfer <p.hoefer@gentics.com>
 */
public class SynonymQueryParser extends CRQueryParser {

	/**
	 * maximum count of fetched Synonyms.
	 */
	private static final int MAX_SYNONYMS = 20;

	/**
	 * 
	 * GenericConfiguration object of the factory.
	 */
	private GenericConfiguration config;

	/**
	 * attributes which are searched in query.
	 */
	private String[] searchedAttributes;

	/**
	 * static log4j {@link Logger} to log errors and debug.
	 */
	private static Logger log = Logger.getLogger(SynonymQueryParser.class);

	/**
	 * sub Query Parser, which is used as "super QueryParser".
	 */
	private QueryParser childQueryParser;

	/**
	 * Constructor.
	 * 
	 * @param pconfig Generic Configuration object
	 * @param version Version
	 * @param searchedAttributes All searched Attributes
	 * @param analyzer Analyzer
	 * @param crRequest CRRequest
	 */
	public SynonymQueryParser(final GenericConfiguration pconfig, Version version, final String[] searchedAttributes, Analyzer analyzer,
		CRRequest crRequest) {
		super(version, searchedAttributes, analyzer, crRequest);
		this.config = pconfig;
		this.searchedAttributes = searchedAttributes;
		//get SubqueryParser if available
		this.childQueryParser = CRQueryParserFactory.getConfiguredParser(searchedAttributes, analyzer, crRequest, new CRConfigUtil(pconfig,
				"Subconfig"));
	}

	/**
	 * parse the query for lucene.
	 * 
	 * @param query as {@link String}
	 * @return parsed lucene query
	 * @throws ParseException when the query cannot be successfully parsed
	 */
	public final Query parse(final String query) throws ParseException {
		String crQuery = query;

		crQuery = replaceBooleanMnoGoSearchQuery(crQuery);
		if (getAttributesToSearchIn().size() > getOne()) {
			crQuery = addMultipleSearchedAttributes(crQuery);
		}
		crQuery = addWildcardsForWordmatchParameter(crQuery);
		crQuery = replaceSpecialCharactersFromQuery(crQuery);

		Query resultQuery = childQueryParser.parse(crQuery);

		try {
			resultQuery = childQueryParser.parse(includeSynonyms(crQuery));
		} catch (IOException e) {
			log.debug("Error while adding synonyms to query.", e);
		}

		return resultQuery;

	}

	/**
	 * look for synonyms in specified Synonymlocation.
	 * add the synonyms to search query
	 * 
	 * @param query the search query, before the synonyms are added
	 * @return searchQuery as String, with added synonyms
	 * @throws IOException when theres a problem with accessing the Index
	 */
	public final String includeSynonyms(String query) throws IOException {

		GenericConfiguration autoConf = (GenericConfiguration) config.get("synonymlocation");
		LuceneIndexLocation synonymLocation = LuceneIndexLocation.getIndexLocation(new CRConfigUtil(autoConf, "synonymlocation"));

		IndexAccessor ia = synonymLocation.getAccessor();
		IndexSearcher synonymSearcher = ia.getPrioritizedSearcher();
		IndexReader synonymReader = ia.getReader(false);

		try {
			HashSet<String> searchedTerms = new HashSet<String>();

			//get all searched Terms out of query
			for (int i = 0; i < searchedAttributes.length; i++) {
				String subquery = query;
				while (subquery.indexOf(searchedAttributes[i] + ":") > 0) {
					subquery = subquery.substring(subquery.indexOf(searchedAttributes[i] + ":") + searchedAttributes[i].length() + 1);
					int substringUntil = -1;
					int pos1 = subquery.indexOf(")");
					int pos2 = subquery.indexOf(" ");
					if (pos1 != -1) {
						substringUntil = pos1;
					}
					if (pos2 != -1) {
						substringUntil = pos2;
					}
					if (pos1 != -1 && pos2 != -1) {
						if (pos1 <= pos2) {
							substringUntil = pos1;
						} else {
							substringUntil = pos2;
						}
					}
					if (substringUntil == -1) {
						substringUntil = subquery.length();
					}
					String addtoSet = subquery.substring(0, substringUntil).replaceAll("\\*", "").replaceAll("\\(", "")
							.replaceAll("\\)", "");
					searchedTerms.add(addtoSet);
					subquery = subquery.substring(substringUntil);
				}

			}

			//create the query-String for synonym-Index with all searchedTerms
			Iterator<String> it = searchedTerms.iterator();
			StringBuilder queryString = new StringBuilder();
			while (it.hasNext()) {
				queryString.append("Deskriptor:" + it.next() + " ");
			}
			Query querySynonym;
			try {
				querySynonym = super.parse(queryString.toString());
			} catch (ParseException e) {
				e.printStackTrace();
				log.debug("Error while parsing query for accessing the synonym Index.", e);
				return query;
			}

			//get all Synonyms from SynonymIndex and add them to searchQuery
			log.debug("Synonym Query String: " + querySynonym.toString());
			TopDocs docs = synonymSearcher.search(querySynonym, MAX_SYNONYMS);
			log.debug("total found synonyms: " + docs.totalHits);
			for (ScoreDoc doc : docs.scoreDocs) {
				Document d = synonymReader.document(doc.doc);
				for (int i = 0; i < searchedAttributes.length; i++) {
					query = query + " OR " + searchedAttributes[i] + ":" + d.get("Synonym");
				}
			}
		} finally {
			ia.release(synonymSearcher);
			ia.release(synonymReader, false);
		}

		return query;
	}

}
