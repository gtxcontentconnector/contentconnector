package com.gentics.cr.lucene.search.query;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.FieldCache.Ints;
import org.apache.lucene.search.Query;

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
	 * @param context AtomicReaderContext for the current reader.
	 */
	protected CustomScoreProvider getCustomScoreProvider(AtomicReaderContext context) throws IOException {
        return new RecencyBooster(context.reader());
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
		private final Ints publishDay;
		
		/**
		 * Initialize the RecencyBooster for the above class with the IndexReader
		 * @param r IndexReader
		 * @throws IOException
		 */
		public RecencyBooster(AtomicReader r) throws IOException {
			super(r.getContext());
			publishDay = FieldCache.DEFAULT.getInts(r, timestampField, false);
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
			long timeAgo = currentTime - publishDay.get(doc);

			float boost;
			
			if (publishDay.get(doc) > (currentTime - timerange)) {
				boost = (float) (multiplier) * (timerange - timeAgo) / timerange;
				return (float) (subQueryScore * (1 + boost));
			}

			return subQueryScore;
		}
	}

}
