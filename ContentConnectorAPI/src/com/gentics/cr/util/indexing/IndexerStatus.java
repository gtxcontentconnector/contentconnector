package com.gentics.cr.util.indexing;

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
	
	private String currStatusString="";

	/**
	 * Get current Status String
	 * @return
	 */
	public String getCurrentStatusString()
	{
		String stat ="";
		synchronized(this)
		{
			stat =  this.currStatusString;
		}
		return stat;
	}
	/**
	 * Set current Status string
	 * @param statusstring
	 */
	public void setCurrentStatusString(String statusstring)
	{
		synchronized(this)
		{
			this.currStatusString=statusstring;
		}
	}
	/**
	 * Duration of last run in ms
	 * @return
	 */
	public long getLastRunDuration() {
		return lastRunDuration;
	}

	/**
	 * Sets the duration of the last run in ms
	 * @param lastRunDuration
	 */
	public void setLastRunDuration(long lastRunDuration) {
		this.lastRunDuration = lastRunDuration;
	}

	/**
	 * returns true if thread is running
	 * @return
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * set to true if thread is running
	 * @param running
	 */
	public void setRunning(boolean running) {
		this.running = running;
	}

	/**
	 * Count of objects
	 * @return
	 */
	public int getObjectCount() {
		return objectCount;
	}

	/**
	 * sets the count of objects
	 * @param objectCount
	 */
	public void setObjectCount(int objectCount) {
		this.objectCount = objectCount;
	}

	/**
	 * already indexed objects in this run
	 * @return
	 */
	public int getObjectsDone() {
		return objectsDone;
	}

	/**
	 * sets already indexed objects in this run
	 * @param objectsDone
	 */
	public void setObjectsDone(int objectsDone) {
		this.objectsDone = objectsDone;
	}

	/**
	 * gets the time of start of the last run
	 * @return
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * sets the time of the start of the last run
	 * @param startTime
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	
	/**
	 * resets the status object
	 */
	public void reset()
	{
		this.objectCount=0;
		this.objectsDone=0;
		this.setRunning(false);
		
	}
		
}
