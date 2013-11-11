package com.gentics.cr.events;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class EventsTest {
	
	@Test
	public void testEventManager() throws IOException {
		EventReceiverDummy eRD = new EventReceiverDummy();
		EventManager.getInstance().register(eRD);
		EventManager.getInstance().fireEvent(new Event() {
			
			@Override
			public String getType() {
				return "correctEvent";
			}
			
			@Override
			public Object getData() {
				return "correctEventData";
			}
		});
		Event firedEvent = eRD.getFiredEvent();
		assertEquals("Event does not match","correctEvent", firedEvent.getType());
		assertEquals("Event data does not match","correctEventData", firedEvent.getData());
	}
}
