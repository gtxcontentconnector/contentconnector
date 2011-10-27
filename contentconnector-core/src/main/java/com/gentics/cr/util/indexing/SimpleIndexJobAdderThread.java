package com.gentics.cr.util.indexing;

/**
 * Use this thread to add jobs to the {@link IndexJobQueue} in the {@link IndexLocation}
 *
 *  
 * @author Sebastian Vogel <s.vogel@gentics.com>
 *
 */
public class SimpleIndexJobAdderThread extends Thread {
	IndexLocation loc;
	AbstractUpdateCheckerJob job;
	
	
	/**
	 * Creates a new thread to add jobs to the {@link IndexLocation}
	 * 
	 * @param loc the IndexLocation to which the job should be added
	 * @param job the job to add
	 */
	public SimpleIndexJobAdderThread(IndexLocation loc, AbstractUpdateCheckerJob job) {
		super();
		this.loc = loc;
		this.job = job;
	}


	/**
	 * adds the job to the {@link IndexJobQueue}
	 */
	public void run(){
		loc.getQueue().addJob(job);
	}

}
