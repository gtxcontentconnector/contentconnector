package com.gentics.cr.configuration.reloadable;

import com.gentics.cr.configuration.GenericConfiguration;


/**
 * Operates as an Interface between the servlet and the Indexer Engine
 * Last changed: $Date: 2009-09-02 17:57:48 +0200 (Mi, 02 Sep 2009) $
 * @version $Revision: 180 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public abstract class ReloadableConfiguration{
	private static final int CHECK_INTERVAL = 5;
	private ReloadListener listener;
	private Thread reloadChecker;
	private Object syn = "";
	
	/**
	 * Sets the current check interval
	 * @param interval
	 */
	public void setCheckInterval(int interval)
	{
		synchronized(syn)
		{
			this.checkInterval = interval;
		}
	}
	/**
	 * Gets the current check interval
	 * @return
	 */
	public int getCheckInterval()
	{
		int i = 0;
		synchronized(syn)
		{
			i = this.checkInterval;
		}
		return i;
	}
	
	protected int checkInterval = CHECK_INTERVAL;
	/**
	 * Creates a new instance of ReloadableContainer
	 * @param listener 
	 */
	public ReloadableConfiguration(ReloadListener listener)
	{
		this.listener = listener;
	}
	
	/**
	 * Reloads the current configuration.
	 * It is recommended that this method is implemented in a thread safe manner.
	 * @return newly loaded configuration
	 */
	public abstract GenericConfiguration reloadConfiguration();
	
	/**
	 * Checks if the current configuration has been changed.
	 * @return true if the configuration has been changed and should be reloaded.
	 */
	public abstract boolean hasConfigChanged();
	
	
	/**
	 * This Method has to be called after the first time the configuration has finishd loading.
	 */
	public void startChangeListener()
	{
		this.reloadChecker = new Thread(new Runnable(){

			public void run() {
				while(!Thread.currentThread().isInterrupted())
				{
					try {
						Thread.sleep(getCheckInterval()*1000);
					} catch (InterruptedException e) {
						;
					}
					if(hasConfigChanged())
					{
						if(listener!=null)
						{
							listener.onBeforeReload();
						}
						GenericConfiguration c = reloadConfiguration();
						if(listener!=null)
						{
							listener.onReloadFinished(c);
						}
					}
				}
			}
			
		});
	}
	
	/**
	 * This Method should be called right before the application stops.
	 * It will stop the reload checker Thread.
	 */
	public void destroy()
	{
		if(this.reloadChecker!=null && this.reloadChecker.isAlive())
		{
			if(!this.reloadChecker.isInterrupted()) 
			{
				this.reloadChecker.interrupt();
			}
			try {
				this.reloadChecker.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
}
