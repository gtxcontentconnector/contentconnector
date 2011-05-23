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
	 * configured value for periodical indexer execution.
	 */
	private boolean periodical;
	
	/**
	 * 
	 * @param config
	 */
	public PeriodicalIndexStandardConfig(final CRConfig config) {
		periodical = config.getBoolean(PERIODICAL_KEY, PERIODICAL_DEFAULT);
	}
	
	/**
	 * @return configured value for periodical indexer execution.
	 */
	public boolean isPeriodical() {
		return periodical;
	}
}