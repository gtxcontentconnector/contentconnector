package com.gentics.cr.events;
/**
 * 
 * Last changed: $Date: 2009-07-10 10:49:03 +0200 (Fr, 10 Jul 2009) $
 * @version $Revision: 131 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public abstract class Event {
	
	public abstract String getType();
	
	public abstract Object getData();
}
