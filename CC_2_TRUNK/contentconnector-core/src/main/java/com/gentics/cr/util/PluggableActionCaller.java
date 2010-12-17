package com.gentics.cr.util;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.gentics.api.portalnode.action.PluggableActionInvoker;
import com.gentics.api.portalnode.action.PluggableActionRequest;
import com.gentics.api.portalnode.action.PluggableActionResponse;

/**
 * 
 * Last changed: $Date: 2010-04-01 15:24:41 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 543 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class PluggableActionCaller{

	private static Logger log = Logger.getLogger(PluggableActionCaller.class);
	
	/**
	 * Static Class helps you to call PluggableActions from Java
	 * @param pluggableActionName Name of the Pluggable Action see http://www.gentics.com/help/ for valid Pluggable Actions
	 * @param parameters Hashmap with parameters for the Pluggable Action
	 * @return returns response, see response.getFeedbackParameters() and response.getFeedbackMessage() for details on errors.
	 */
	
	public static PluggableActionResponse call(String pluggableActionName, HashMap<String,Object> parameters){
		log.debug("PluggableAction "+pluggableActionName+" start");
		PluggableActionRequest request = PluggableActionInvoker.createRequestObject();
		PluggableActionResponse response = PluggableActionInvoker.createResponseObject();
		Iterator<String> keyIterator = parameters.keySet().iterator();
		while(keyIterator.hasNext()){
			String key =  keyIterator.next();
			request.setParameter(key, parameters.get(key));
		}
		boolean sucess = PluggableActionInvoker.invokeAction(pluggableActionName, request, response);
		if(sucess == false){
			log.error("PluggableAction "+pluggableActionName+" wasn't finished sucessfull. Returned false.");
		}
		else{
			log.debug("PluggableAction "+pluggableActionName+" was finished sucessfull. Returned true.");
		}
		return response;
	}
	
}
