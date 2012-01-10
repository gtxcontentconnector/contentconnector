package com.gentics.cr.util.indexing;

import com.gentics.api.lib.resolving.Resolvable;
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
	 * These checks have to be performed in ascending order, otherwise a {@link WrongOrderException} will be thrown.
	 * @param identifyer - identifier of the object for fast comparisons
	 * @param timestamp - date when the object was last changed in the index. this is a unix timestamp (seconds not milliseconds)
	 * @param timestampattribute - attribute in the resolvable holding the timestamp.
	 * @param object - the resolvable with the current data
	 * @return <code>true</code> if the object in the index is up to date, otherwise  <code>false</code> 
	 * @throws WrongOrderException
	 */	
	public boolean isUpToDate(String identifyer, Object timestamp, String timestampattribute, Resolvable object) throws WrongOrderException
	{
		if(!"".equals(this.lastIdentifyer) && this.lastIdentifyer.compareTo(identifyer)<0)
		{
			throw new WrongOrderException();
		}
		return checkUpToDate(identifyer, timestamp, timestampattribute, object);
	}

	/**
	 * check if the given object is up to date in the index
	 * @param identifyer - identifier of the object for fast comparisons
	 * @param timestamp - date when the object was last changed in the index. this is a unix timestamp (seconds not milliseconds) 
	 * @param timestampattribute - attribute in the resolvable holding the timestamp.
	 * @param object - the resolvable with the current data
	 * @return <code>true</code> if the object in the index is up to date, otherwise  <code>false</code> 
	 */
	//TODO check why the timestamp parameter is not an Integer and document this.
	protected abstract boolean checkUpToDate(String identifyer,Object timestamp, String timestampattribute, Resolvable object);

	/**
	 * delete the objects that are not more existent. usually this is done by holding a list of all objects created at initialization and removing all checked objects from the list. this leaves all not longer existent objects in the list.
	 */
	public abstract void deleteStaleObjects();
}
