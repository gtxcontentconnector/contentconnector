package com.gentics.cr.lucene.events;

import com.gentics.cr.events.Event;

public class IndexingFinishedEvent extends Event {

	public static final String INDEXING_FINISHED_EVENT_TYPE = "LUCENEINDEXINGFINISHEDEVENT";
	private Object data;

	/**
	 * Create a new instance of the IndexingFinishedEvent
	 * @param data
	 */
	public IndexingFinishedEvent(Object data) {
		this.data = data;
	}

	@Override
	public Object getData() {
		return this.data;
	}

	@Override
	public String getType() {
		return INDEXING_FINISHED_EVENT_TYPE;
	}

}
