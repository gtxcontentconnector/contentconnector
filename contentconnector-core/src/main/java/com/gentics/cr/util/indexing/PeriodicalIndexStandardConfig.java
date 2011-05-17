package com.gentics.cr.util.indexing;

import com.gentics.cr.CRConfig;

/**
 *
 * @author Markus Burchhart, s IT Solutions
 *
 */
public class PeriodicalIndexStandardConfig implements IPeriodicalIndexConfig {

	/**
	 * Configuration key for periodical indexer jobs 
	 */
	public static final String PERIODICAL_KEY = "PERIODICAL";
	
	private boolean periodical;
	
	public PeriodicalIndexStandardConfig(CRConfig config) {
		periodical = config.getBoolean(PERIODICAL_KEY, PERIODICAL_DEFAULT);
	}
	
	public boolean isPeriodical() {
		return periodical;
	}
}