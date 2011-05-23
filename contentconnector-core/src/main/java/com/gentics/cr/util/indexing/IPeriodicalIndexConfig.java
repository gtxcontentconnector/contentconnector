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
	 * @return configured value for periodical indexer execution.
	 */
	public boolean isPeriodical();
}