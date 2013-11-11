package com.gentics.cr.events;

public class EventReceiverDummy implements IEventReceiver {

	private Event firedEvent;
	
	@Override
	public void processEvent(Event event) {
		firedEvent = event;
	}
	
	public Event getFiredEvent() {
		return firedEvent;
	}

}
