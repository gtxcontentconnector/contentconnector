package com.gentics.cr.events;

import java.util.Vector;

/**
 * This class is used to fire events to registered receivers, register recievers and remove receivers.
 * 
 * Take care that this singleton implementation only works for a single webapp unless the lib is loaded with the shared loader.
 * 
 * Last changed: $Date: 2009-07-10 10:49:03 +0200 (Fr, 10 Jul 2009) $
 * @version $Revision: 131 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class EventManager {

	private Vector<IEventReceiver> receivers;
	
	private static EventManager instance;
	
	private EventManager()
	{
		
	}
	
	/**
	 * Get the singleton instance of eventmanager
	 * @return
	 */
	public static EventManager getInstance()
	{
		if(instance==null)instance = new EventManager();
		return instance;
	}
	
	/**
	 * Fire a event to the registered receivers
	 * @param event
	 */
	public synchronized void fireEvent(Event event)
	{
		if(this.receivers!=null)
		{
			for(IEventReceiver ir:receivers)
			{
				ir.processEvent(event);
			}
		}
	}
	
	/**
	 * Register a eventreiceiver with the event manager
	 * @param receiver to register
	 */
	public synchronized void register(IEventReceiver receiver)
	{
		if(this.receivers==null)this.receivers=new Vector<IEventReceiver>();
		this.receivers.add(receiver);
	}
	
	/**
	 * Unregister a eventreceiver with the event manager
	 * @param receiver to unregister
	 * @return true if the receiver was registered and has now been unregistered
	 */
	public synchronized boolean unregister(IEventReceiver receiver)
	{
		if(this.receivers!=null)
		{
			return this.receivers.remove(receiver);
		}
		return false;
	}
}
