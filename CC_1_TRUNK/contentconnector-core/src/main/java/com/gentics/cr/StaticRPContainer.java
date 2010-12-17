package com.gentics.cr;


import java.util.Hashtable;

import org.apache.log4j.Logger;

import com.gentics.cr.exceptions.CRException;
/**
 * 
 * Last changed: $Date: 2009-09-02 17:57:48 +0200 (Mi, 02 Sep 2009) $
 * @version $Revision: 180 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class StaticRPContainer {
	
	private static Logger log = Logger.getLogger(StaticRPContainer.class);
	private static Hashtable<String,RequestProcessor> rpmap = new Hashtable<String,RequestProcessor>(2);
	
	/**
	 * Fetches a already created instance of the request processor. If request processor was not created yet, one will be instantiated.
	 * @param config 
	 * @param rpnumber 
	 * @return 
	 * @throws CRException 
	 */
	public static RequestProcessor getRP(CRConfigUtil config, int rpnumber) throws CRException
	{
		String key = config.getName()+".RP."+rpnumber;
		RequestProcessor rp = rpmap.get(key);
		if(rp==null)
		{
			log.debug("RP not found. Creating new instance");
			rp = config.getNewRequestProcessorInstance(rpnumber);
			if(rp!=null)
			{
				rpmap.put(key, rp);
			}
		}
		
		
		return rp;
	}
}
