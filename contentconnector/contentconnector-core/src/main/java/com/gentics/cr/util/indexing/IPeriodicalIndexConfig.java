package com.gentics.cr.util.indexing;

/**
 * @author Markus Burchhart, s IT Solutions
 */
public interface IPeriodicalIndexConfig {
	/**
	 * default value for periodical indexer execution.
	 */
	public static final boolean PERIODICAL_DEFAULT = false;

	/**
	 * default value for periodical indexer execution.
	 */
	public static final long PERIODICAL_FIRSTJOBSTARTDELAY = 0;

	/**
	 * @return configured value for periodical indexer execution.
	 */
	public boolean isPeriodical();

	/**
	 * the index job creation wait for the configured amount of second till the first job is triggered
	 */
	public long getFirstJobStartDelay();
}
