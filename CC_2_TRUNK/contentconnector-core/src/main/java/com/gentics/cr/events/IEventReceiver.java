package com.gentics.cr.events;

/**
 * General Interface for Content Connector Event Receivers.
 * @author Christopher
 *
 */
public interface IEventReceiver {

	/**
	 * Method that will be executed if an event is thrown.
	 * @param event fired event.
	 */
	void processEvent(Event event);
}
