package com.gentics.cr.lucene.indexer.index;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import com.gentics.cr.CRConfig;

public class IndexJobQueue{
	
	private static final String INTERVAL_KEY = "CHECKINTERVAL";
	
	private LinkedBlockingQueue<CRIndexJob> queue;
	private Thread d;
	private boolean stop=false;
	private int interval = 5; // Default 5 sec interval for checking
	private CRIndexJob currentJob;
	private ArrayList<CRIndexJob> lastJobs;
	
	public IndexJobQueue(CRConfig config)
	{
		String i = (String)config.get(INTERVAL_KEY);
		if(i!=null)this.interval = new Integer(i);
		queue = new LinkedBlockingQueue<CRIndexJob>();
		lastJobs = new ArrayList<CRIndexJob>(3);
		this.d = new Thread(new Runnable(){
			public void run()
			{
				workQueue();
			}
		});
		this.d.setName("IndexJobQueueWorker");
	}
	
	/**
	 * Returns true if the worker is running and processing the queue
	 * @return
	 */
	public boolean isRunning()
	{
		return (!this.stop && this.d.isAlive());
	}
	
	/**
	 * Get an ArrayList with the three last jobs. If there where no jobs the list is going to be empty.
	 * @return
	 */
	public ArrayList<CRIndexJob> getLastJobs()
	{
		return this.lastJobs;
	}
	
	private void addToLastJobs(CRIndexJob j)
	{
		ArrayList<CRIndexJob> l = new ArrayList<CRIndexJob>(3);
		l.add(j);
		int i=0;
		for(CRIndexJob e:lastJobs)
		{
			if(e!=null)l.add(e);
			i++;
			if(i>=2)break;
		}
		lastJobs = l;
	}
	
	private void workQueue()
	{
		boolean interrupted = false;

		while (!interrupted && !stop) {
			try {
				CRIndexJob j = this.queue.poll();
				if(j!=null)
				{
					currentJob = j;
					j.process();
					addToLastJobs(j);
					currentJob = null;
				}
				// Wait for next cycle
				Thread.sleep(interval * 1000);
			} catch (InterruptedException e) {
				interrupted = true;
				e.printStackTrace();
			}
		}
		this.stop=true;
	}
	
	/**
	 * Starts the worker that is processing the Indexer Queue
	 */
	public void startWorker()
	{
		this.d.start();
		this.stop=false;
	}
	
	public void stopWorker()
	{
		this.stop=true;
		try {
			this.d.join(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Adds a CRIndexJob to the Job Queue
	 * @param job
	 * @return
	 */
	public synchronized boolean addJob(CRIndexJob job)
	{
		if(!queue.contains(job))
		{
			return queue.offer(job);
		}
		return false;
	}
	
	/**
	 * Get Number of Jobs in the Queue
	 * @return
	 */
	public int getSize()
	{
		return this.queue.size();
	}
	
	/**
	 * Returns current job or null
	 * @return
	 */
	public CRIndexJob getCurrentJob()
	{
		return this.currentJob;
	}
	
	/**
	 * Returns configured interval for checking the queue for new jobs
	 * @return
	 */
	public int getInterval()
	{
		return this.interval;
	}
		
}
