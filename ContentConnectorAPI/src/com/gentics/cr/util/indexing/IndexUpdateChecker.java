package com.gentics.cr.util.indexing;

import com.gentics.cr.exceptions.WrongOrderException;

/**
 * Walks an Index and compares Identifyer/Timestamp pairs to the Objects in the Index
 * 
 * Last changed: $Date: 2009-09-02 17:57:48 +0200 (Mi, 02 Sep 2009) $
 * @version $Revision: 180 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public abstract class IndexUpdateChecker {
	
	String lastIdentifyer="";
	
	/**
	 * Checks the identifyer/timestamp pair against the index iterator if the object in the index exists and if it is up to date.
	 * These checks have to be performed in ascending order, otherwise a @link WrongOrderException will be thrown.
	 * @param identifyer
	 * @param timestamp
	 * @return
	 * @throws WrongOrderException
	 */	
	public boolean isUpToDate(String identifyer, int timestamp) throws WrongOrderException
	{
		if(!"".equals(this.lastIdentifyer) && this.lastIdentifyer.compareTo(identifyer)<0)
		{
			throw new WrongOrderException();
		}
		return checkUpToDate(identifyer,timestamp);
	}
	
	/**
	 * @param identifyer
	 * @param timestamp
	 * @return
	 */
	protected abstract boolean checkUpToDate(String identifyer,int timestamp);
	
	/**
	 * 
	 */
	public abstract void deleteStaleObjects();
}
