package com.gentics.cr.util.indexing;

import com.gentics.cr.CRConfig;

/**
 * Use this interface to write extension for the {@link IndexLocation} <br>
 * 
 * Implementations of IndexExtensions should extend from
 * {@link AbstractIndexExtension}.<br>
 * Every IndexExtension <b>must</b> implement a constructor with two Parameters
 * of the type {@link CRConfig} and {@link IndexLocation}
 * 
 * <pre>
 * 	public MyIndexExtension(CRConfig config, IndexLocation callingLocation) {
 * 	 ...
 * 	}
 * </pre>
 */
public interface IndexExtension {

	/**
	 * Stops the Extension, this method is called when the {@link IndexLocation}
	 * is stopped
	 */
	public void stop();

	/**
	 * Adds a job to the queue
	 * 
	 * @param name
	 *            the name of the job to add
	 * @throws NoSuchMethodException
	 *             if no job with the given name was found
	 */
	public void addJob(String name) throws NoSuchMethodException;

	/**
	 * Adds a job to the queue
	 * 
	 * @param name
	 * @param indexLocation
	 * @throws NoSuchMethodException
	 */
	public void addJob(String name, IndexLocation indexLocation) throws NoSuchMethodException;

	/**
	 * returns all the jobs supported by this extension
	 * 
	 * @return an string array with the names of the jobs
	 */
	public String[] getJobs();
}
