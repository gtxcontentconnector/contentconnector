package com.gentics.cr.events;

import java.util.Vector;

/**
 * This class is used to fire events to registered receivers, 
 * register recievers and remove receivers.
 * 
 * Take care that this singleton implementation only works for a single 
 * webapp unless the lib is loaded with the shared loader.
 * 
 * Last changed: $Date: 2009-07-10 10:49:03 +0200 (Fr, 10 Jul 2009) $
 * @version $Revision: 131 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public final class EventManager {

	/**
	 * Event receiver collection.
	 */
	private Vector<IEventReceiver> receivers;

	/**
	 * Singleton instance of the EventManager.
	 */
	private static EventManager instance = new EventManager();

	/**
	 * Private constructor prevents instantiation.
	 */
	private EventManager() {

	}

	/**
	 * Get the singleton instance of eventmanager.
	 * @return singleton instance of EventManager.
	 */
	public static EventManager getInstance() {
		return instance;
	}

	/**
	 * Fire a event to the registered receivers.
	 * @param event fired event
	 */
	public synchronized void fireEvent(final Event event) {
		if (this.receivers != null) {
			for (IEventReceiver ir : receivers) {
				ir.processEvent(event);
			}
		}
	}

	/**
	 * Register a eventreiceiver with the event manager.
	 * @param receiver to register
	 */
	public synchronized void register(final IEventReceiver receiver) {
		if (this.receivers == null) {
			this.receivers = new Vector<IEventReceiver>();
		}
		this.receivers.add(receiver);
	}

	/**
	 * Unregister a eventreceiver with the event manager.
	 * @param receiver to unregister
	 * @return true if the receiver was registered and has now been unregistered
	 */
	public synchronized boolean unregister(final IEventReceiver receiver) {
		if (receivers != null) {
			return receivers.remove(receiver);
		}
		return false;
	}
}
