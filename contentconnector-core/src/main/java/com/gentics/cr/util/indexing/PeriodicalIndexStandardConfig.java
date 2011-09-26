package com.gentics.cr.util.indexing;

import com.gentics.cr.CRConfig;

/**
 *
 * @author Markus Burchhart, s IT Solutions
 *
 */
public class PeriodicalIndexStandardConfig implements IPeriodicalIndexConfig {
	
	/**
	 * Configuration key for periodical indexer jobs.
	 */
	public static final String PERIODICAL_KEY = "PERIODICAL";
	
	/**
	 * Configuration key of the delay before first index job is triggered
	 */
	private static final String FIRSTJOBSTARTDELAY_KEY = "FIRSTJOBSTARTDELAY";

	/**
	 * configured value for periodical indexer execution.
	 */
	private boolean periodical;
	
	/**
	 * delay before first index job is triggered
	 */
	private int firstJobStartDelay;
	
	/**
	 * 
	 * @param config
	 */
	public PeriodicalIndexStandardConfig(final CRConfig config) {
		periodical = config.getBoolean(PERIODICAL_KEY, PERIODICAL_DEFAULT);
		firstJobStartDelay = config.getInteger(FIRSTJOBSTARTDELAY_KEY,
				PERIODICAL_FIRSTJOBSTARTDELAY);
	}
	
	/**
	 * @return configured value for periodical indexer execution.
	 */
	public boolean isPeriodical() {
		return periodical;
	}
	
	/**
	 * @return delay before first index job is triggered
	 */
	public int getFirstJobStartDelay() {
		return firstJobStartDelay;
	}
	
}