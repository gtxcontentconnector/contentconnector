package com.gentics.cr.util.indexing;

/**
 * @author Christopher
 *
 */
public interface IIndexUpdateChecker {
	
	/**
	 * @param identifyer
	 * @param timestamp
	 * @return
	 */
	public boolean isUpToDate(String identifyer,int timestamp);
	
	/**
	 * 
	 */
	public void deleteStaleObjects();
}
