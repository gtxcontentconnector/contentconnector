package com.gentics.cr.lucene.search.query;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

import com.gentics.cr.CRRequest;
import com.gentics.cr.configuration.GenericConfiguration;

/**
 * This class is boosting according to the configuration of the user,
 * whether the score gets boosted or not.
 * @author Stefan Binder
 *
 */
public class CRRecencyBoostingQueryParser extends CRQueryParser {

	/**
	 * GenericConfiguration object of the factory.
	 */
	private GenericConfiguration config;

	/**
	 * Is configurable by the user.
	 * the multiplier for all queries, within the timerange
	 */
	private double multiplicatorBoost;

	/**
	 * Is configurable by the user.
	 * Which field is used for the calculations and comparisons
	 */
	private String boostAttribute;

	/**
	 * Is configurable by the user.
	 * Timerange between today and today - range
	 */
	private int timerange;

	/**
	   * initialize a CRRecencyBostingQuery with time-attributes for calculation.
	   * @param pconfig Generic Configuration object
	   * @param version Version
	   * @param searchedAttributes All searched Attributes
	   * @param analyzer Analyzer
	   * @param crRequest CRRequest
	   */
	public CRRecencyBoostingQueryParser(GenericConfiguration pconfig, Version version,
			String[] searchedAttributes, Analyzer analyzer, CRRequest crRequest) {
		super(version, searchedAttributes, analyzer, crRequest);
		this.config = pconfig;

		this.multiplicatorBoost = Double.parseDouble(this.config.getProperties().getProperty("MULTIPLICATORBOOST"));
		this.boostAttribute = this.config.getProperties().getProperty("BOOSTATTRIBUTE");
		this.timerange = Integer.parseInt(this.config.getProperties().getProperty("TIMERANGE"));
	}

	/**
	 * parse the query for lucene.
	 * @param query as {@link String}
	 * @return parsed lucene query
	 * @throws ParseException when the query cannot be successfully parsed
	 */
	public Query parse(final String query) throws ParseException {
		String crQuery = query;

		getLogger().debug("parsing query: " + crQuery);
		crQuery = replaceBooleanMnoGoSearchQuery(crQuery);
		if (getAttributesToSearchIn().size() > getOne()) {
			crQuery = addMultipleSearchedAttributes(crQuery);
		}
		crQuery = addWildcardsForWordmatchParameter(crQuery);
		crQuery = replaceSpecialCharactersFromQuery(crQuery);
		getLogger().debug("parsed query: " + crQuery);

		if (this.multiplicatorBoost <= 0) {
			getLogger().error("No multiplicator is set! "
					+ "Please change the search.properties config e.g.: rp.1.queryparser.multiplicatorBoost=XX");
		}
		if (this.timerange <= 0) {
			getLogger().error("No timerange is set! "
					+ "Please change the search.properties config e.g.: rp.1.queryparser.timerange=XX");
		}
		if ("".equals(this.boostAttribute)) {
			getLogger().error("No boostAttribute is set! "
					+ "Please change the search.properties config e.g.: rp.1.queryparser.boostAttribute=XX");
		}

		Query resultQuery = new CRRecencyBoostingQuery(super.parse(crQuery),
				this.multiplicatorBoost, this.timerange, this.boostAttribute);

		return resultQuery;
	}

}
