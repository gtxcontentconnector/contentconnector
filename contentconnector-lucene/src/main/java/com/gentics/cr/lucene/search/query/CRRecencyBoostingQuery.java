package com.gentics.cr.lucene.search.query;

import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.CustomScoreProvider;
import org.apache.lucene.search.function.CustomScoreQuery;
import org.apache.lucene.search.function.ValueSourceQuery;

/**
 * CRRecencyBoostingQueryParser needs this query to boost the newest entries.
 * @author Stefan Binder
 *
 */
public class CRRecencyBoostingQuery extends CustomScoreQuery {
	/**
	 *
	 */
	private static final long serialVersionUID = -4859771582357330891L;

	/**
	 * Is configurable by the user.
	 * the multiplier for all queries, within the timerange
	 */
	private double multiplier;

	/**
	 * Is configurable by the user.
	 * Timerange between today and today - range
	 */
	private int timerange;

	/**
	 * Is configurable by the user.
	 * Which field is used for the calculations and comparisons
	 */
	private String timestampField;

	/**
	 * Logger for Console.
	 */
	protected static final Logger LOGGER = Logger.getLogger(CRRecencyBoostingQuery.class);


	/**
	   * initialize a CRRecencyBostingQuery with time-attributes for calculation.
	   * @param q original query
	   * @param multiplier the multiplier for all queries, within the timerange
	   * @param timerange Timerange between today and today - range
	   * @param timestampField Which field is used for the calculations and comparisons
	   */
	public CRRecencyBoostingQuery(Query q, double multiplier, int timerange, String timestampField) {
		super(q);
		this.multiplier = multiplier;
		this.timerange = timerange;
		this.timestampField = timestampField;
	}

	/**
	 * returns the a new RecencyBooster (innerClass of this).
	 * @param r IndexReader for the queries
	 */
	public CustomScoreProvider getCustomScoreProvider(IndexReader r) throws IOException {
		return new RecencyBooster(r);
	}

	/**
	 * Inner Class of CRRecencyBoostingQuery for calculating the score.
	 * @author stefanbinder
	 *
	 */
	private class RecencyBooster extends CustomScoreProvider {
		
		/**
		 * Array of the actual fields in the entry
		 */
		private final long[] publishDay;
		
		/**
		 * Initialize the RecencyBooster for the above class with the IndexReader
		 * @param r IndexReader
		 * @throws IOException
		 */
		public RecencyBooster(IndexReader r) throws IOException {
			super(r);
			publishDay = FieldCache.DEFAULT.getLongs(r, timestampField);
		}

		/**
		 * Get the current score and boost this score, if the user-config applies to the entry
		 * @param doc is the id of the current entry in the result
		 * @param subQueryScore is the currenct score for this entry
		 * @param valSrcScore is the value Source Score
		 * @overwrite overwrites the customScore-method of CustomScoreProvider
		 * @return the score within the calculated boost
		 */
		public float customScore(int doc, float subQueryScore, float valSrcScore) {
			long currentTime = System.currentTimeMillis() / 1000;
			long timeAgo = currentTime - publishDay[doc];

			float boost;
			if (publishDay[doc] > (currentTime - timerange)) {
				boost = (float) (multiplier) * (timerange - timeAgo) / timerange;
				return (float) (subQueryScore * (1 + boost));
			}

			return subQueryScore;
		}
	}

}
