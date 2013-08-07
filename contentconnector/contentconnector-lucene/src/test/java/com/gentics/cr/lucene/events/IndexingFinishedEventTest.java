package com.gentics.cr.lucene.events;

import org.junit.Assert;
import org.junit.Test;

import com.gentics.cr.events.Event;

public class IndexingFinishedEventTest {

	
	@Test
	public void testIndexingFinishedEvent() {
		Event event = new IndexingFinishedEvent("Testdata");
		Assert.assertEquals("Testdata was not delivered.", "Testdata", event.getData());
		Assert.assertEquals("Type did not match.", IndexingFinishedEvent.INDEXING_FINISHED_EVENT_TYPE, event.getType());
	}
}
