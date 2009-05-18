package com.gentics.cr.lucene.indexer;

import java.util.Date;
/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class IndexerStatus {
	
	private boolean running=false;
	
	private int objectCount = 0;
	
	private int objectsDone = 0;
	
	private Date startTime = null;
	
	private long lastRunDuration = 0;

	public long getLastRunDuration() {
		return lastRunDuration;
	}

	public void setLastRunDuration(long lastRunDuration) {
		this.lastRunDuration = lastRunDuration;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public int getObjectCount() {
		return objectCount;
	}

	public void setObjectCount(int objectCount) {
		this.objectCount = objectCount;
	}

	public int getObjectsDone() {
		return objectsDone;
	}

	public void setObjectsDone(int objectsDone) {
		this.objectsDone = objectsDone;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	
	public void reset()
	{
		this.objectCount=0;
		this.objectsDone=0;
		this.setRunning(false);
		
	}
		
}
