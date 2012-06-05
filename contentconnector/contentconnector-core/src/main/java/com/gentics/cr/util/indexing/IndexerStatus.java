package com.gentics.cr.util.indexing;

import java.util.Date;

/**
 * 
 * Last changed: $Date: 2010-04-01 15:24:17 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 542 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class IndexerStatus {

	private boolean running = false;

	private int objectCount = 0;

	private int objectsDone = 0;

	private Date startTime = null;

	private long lastRunDuration = 0;

	private String currStatusString = "";

	private boolean error = false;

	private String errorMessage = "";

	/**
	 * set the error message.
	 * @param message error message
	 */
	public final void setError(final String message) {
		error = true;
		errorMessage = message;
	}

	/**
	 * Check if job had an error.
	 * @return true if error.
	 */
	public final boolean hasError() {
		return error;
	}

	/**
	 * Fetch error message.
	 * @return error message if set.
	 */
	public final String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Get current Status String.
	 */
	public String getCurrentStatusString() {
		String stat = "";
		synchronized (this) {
			stat = this.currStatusString;
		}
		return stat;
	}

	/**
	 * Set current Status string.
	 * @param statusstring
	 */
	public void setCurrentStatusString(final String statusstring) {
		synchronized (this) {
			this.currStatusString = statusstring;
		}
	}

	/**
	 * Duration of last run in ms.
	 */
	public long getLastRunDuration() {
		return lastRunDuration;
	}

	/**
	 * Sets the duration of the last run in ms.
	 * @param lastRunDuration
	 */
	public void setLastRunDuration(final long lastRunDuration) {
		this.lastRunDuration = lastRunDuration;
	}

	/**
	 * returns true if thread is running.
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * set to true if thread is running.
	 * @param running
	 */
	public void setRunning(final boolean running) {
		this.running = running;
	}

	/**
	 * Count of objects.
	 */
	public int getObjectCount() {
		return objectCount;
	}

	/**
	 * sets the count of objects.
	 * @param objectCount
	 */
	public void setObjectCount(final int objectCount) {
		this.objectCount = objectCount;
	}

	/**
	 * already indexed objects in this run.
	 */
	public int getObjectsDone() {
		return objectsDone;
	}

	/**
	 * sets already indexed objects in this run.
	 * @param objectsDone
	 */
	public void setObjectsDone(final int objectsDone) {
		this.objectsDone = objectsDone;
	}

	/**
	 * gets the time of start of the last run.
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * sets the time of the start of the last run.
	 * @param startTime
	 */
	public void setStartTime(final Date startTime) {
		this.startTime = startTime;
	}

	/**
	 * resets the status object.
	 */
	public void reset() {
		this.objectCount = 0;
		this.objectsDone = 0;
		this.setRunning(false);

	}

}
