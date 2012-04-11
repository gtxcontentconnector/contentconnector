package com.gentics.cr.events;

/**
 * General Interface for Content Connector Events.
 * Last changed: $Date: 2009-07-10 10:49:03 +0200 (Fr, 10 Jul 2009) $
 * @version $Revision: 131 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public abstract class Event {
	/**
	 * Get the Type of the Event.
	 * @return type as String
	 */
	public abstract String getType();

	/**
	 * Get the Data of the Event.
	 * @return data
	 */
	public abstract Object getData();
}
