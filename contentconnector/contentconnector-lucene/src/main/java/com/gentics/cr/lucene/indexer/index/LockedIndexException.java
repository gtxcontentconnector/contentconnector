package com.gentics.cr.lucene.indexer.index;

/**
 * 
 * Last changed: $Date: 2009-09-02 17:57:48 +0200 (Mi, 02 Sep 2009) $
 * @version $Revision: 180 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class LockedIndexException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7385627770364691660L;

	/**
	 * Create new LockedIndexException
	 * @param ex
	 */
	public LockedIndexException(Exception ex) {
		super(ex.getMessage(), ex);
	}

	/**
	 * Create new LocketIndexException without a causing exception
	 */
	public LockedIndexException() {

	}
}
