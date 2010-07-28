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
	 * These checks have to be performed in ascending order, otherwise a @link WrongOrderException will be thrown.
	 * @param identifyer
	 * @param timestamp
	 * @param object
	 * @return
	 * @throws WrongOrderException
	 */	
	public boolean isUpToDate(String identifyer, Object timestamp, String timestampattribute, Resolvable object) throws WrongOrderException
	{
		if(!"".equals(this.lastIdentifyer) && this.lastIdentifyer.compareTo(identifyer)<0)
		{
			throw new WrongOrderException();
		}
		return checkUpToDate(identifyer,timestamp,timestampattribute, object);
	}
	/*
	protected abstract Resolvable getIndexedResolvable(String identifyer);
	
	protected abstract GenericConfiguration getConfig();
	
	private GenericConfiguration getUpdateConfig()
	{
		GenericConfiguration conf = getConfig();
		if(conf==null)
		{
			//PREFILL DEFAULT CONFIG
		}
		return conf;
	}
	*/
	/**
	 * @param identifyer
	 * @param timestamp
	 * @return
	 */
	protected abstract boolean checkUpToDate(String identifyer,Object timestamp, String timestampattribute, Resolvable object);
	/*
	protected boolean checkUpToDate(String identifyer, int timestamp, Resolvable object) {
		Resolvable indexedObject = getIndexedResolvable(identifyer);
	  if(indexedObject!=null){
	   log.debug("Found "+identifyer+" in stored files.");
	   
	   GenericConfiguration checkAttributesConfiguration = (GenericConfiguration) config.get(UPDATEJOBCLASS_CHECKATTRIBUTES_KEY);
	   Enumeration<Object> checkAttributesEnumeration = checkAttributesConfiguration.getProperties().keys();
	   Map<String,GenericConfiguration> confs = checkAttributesConfiguration.getSortedSubconfigs();
	   for(Entry<String,GenericConfiguration> e: confs.entrySet())
	   {
		   e.getKey();
		   e.getValue();
	   }
	   
	   
	   
	   
	   while(checkAttributesEnumeration.hasMoreElements()){
	    String attributeName = checkAttributesEnumeration.nextElement().toString();
	    String attributeIdentifyer = identifyer + "." + attributeName;
	    log.debug("Checking attribute "+attributeName);
	    if(stored_files.containsKey(attributeIdentifyer)){
	     String rule = checkAttributesConfiguration.getString(attributeName);
	     if(evaluateStringForBoolean(rule,object)){
	      String attributeValueFromObject = getCaseInsensitiveString(object,attributeName);
	      Object attributeValueFromStoredFiles = stored_files.get(attributeIdentifyer);
	      if(!attributeValueFromStoredFiles.equals(attributeValueFromObject)){
	       logger.debug("Attribute "+attributeName+" value ("+attributeValueFromObject+")differs from last call ("+attributeValueFromStoredFiles+"), mark object as old.");
	       return false;
	      }
	     }
	     else{
	      logger.debug("Configured Rule ("+rule+") doesn't match object ("+object+"), so the attribute "+attributeName+" is not considered for updatecheck.");
	     }
	     
	    }
	   }
	   return true;
	  }
	  else{
	   logger.debug("Not found "+identifyer+" in stored files, mark as old");
	   return false;
	  }
	 }
	*/
	
	/**
	 * 
	 */
	public abstract void deleteStaleObjects();
}
