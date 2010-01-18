package com.gentics.cr.util.indexing;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexWriter;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.lucene.indexaccessor.IndexAccessor;
import com.gentics.cr.lucene.indexaccessor.IndexAccessorFactory;
import com.gentics.cr.lucene.indexer.index.LockedIndexException;


/**
 * 
 * Last changed: $Date: 2009-07-10 10:49:03 +0200 (Fr, 10 Jul 2009) $
 * @version $Revision: 131 $
 * @author $Author: supnig@constantinopel.at $
 *
 */

public class IndexLocation {
	//STATIC MEMBERS
	protected static final Logger log = Logger.getLogger(IndexLocation.class);
	private static final String REOPEN_CHECK_KEY = "reopencheck";
	private static final String REOPEN_FILENAME = "reopen";
	private static final String INDEX_LOCATION_KEY = "indexLocation";
	private static final String PERIODICAL_KEY = "PERIODICAL";
	private static Hashtable<String,IndexLocation> indexmap;
	private static final String LOCK_DETECTION_KEY = "LOCKDETECTION";
	/**
	 * The key in the configuration for specifying the update job implementation class
	 */
	public static final String UPDATEJOBCLASS_KEY = "updatejobclass";
	private static final String DEFAULT_UPDATEJOBCLASS = "com.gentics.cr.lucene.indexer.index.CRLuceneIndexJob";
	
	
	//Instance Members
	private IndexJobQueue queue = null;
	private CRConfig config;
	private boolean periodical = false;
	private int periodical_interval = 60; //60 seconds
	private Thread periodical_thread;
	private boolean lockdetection = false;
	private boolean reopencheck = false;
	private String indexLocation ="";
	
	/**
	 * Returns the filename of the reopen file.
	 * @return filename of the reopen file.
	 */
	public String getReopenFilename(){
		return this.indexLocation+"/"+REOPEN_FILENAME;
	}
	
	/**
	 * Creates the reopen file to make portlet reload the index.
	 */
	public void createReopenFile(){
		boolean write_reopen_file = Boolean.parseBoolean((String)config.get("writereopenfile"));
		
		if(write_reopen_file == true){
		
			log.debug("Writing reopen to " + this.getReopenFilename());
			try {
				new File(this.getReopenFilename()).createNewFile();
			} catch (IOException e) {
				log.warn("Cannot create reopen file! " + e);
			}
		}
	}
	
	/**
	 * Checks Lock and throws Exception if Lock exists
	 * @throws LockedIndexException 
	 * @throws IOException 
	 */
	public void checkLock() throws LockedIndexException
	{
	}
	
	
	protected IndexLocation(CRConfig config)
	{
		this.config = config;
		indexLocation = (String)config.get(INDEX_LOCATION_KEY);
		queue = new IndexJobQueue(config);
		String per = (String)config.get(PERIODICAL_KEY);
		periodical = Boolean.parseBoolean(per);
		String s_reopen = (String)config.get(REOPEN_CHECK_KEY);
		if(s_reopen!=null)
		{
			reopencheck = Boolean.parseBoolean(s_reopen);
		}
		String s_lockdetect = (String)config.get(LOCK_DETECTION_KEY);
		if(s_lockdetect!=null)
		{
			lockdetection = Boolean.parseBoolean(s_lockdetect);
		}
		if(periodical)
		{
			periodical_thread = new Thread(new Runnable(){
				public void run()
				{
					boolean interrupted = false;
					while(periodical && !interrupted && !Thread.currentThread().isInterrupted())
					{
						try
						{
							createAllCRIndexJobs();
							Thread.sleep(periodical_interval*1000);
							
						}catch(InterruptedException ex)
						{
							interrupted = true;
						}
					}
				}
			});
			periodical_thread.setName("PeriodicIndexJobCreator");
			periodical_thread.start();
			
		}
		this.queue.startWorker();
	}
	
	/**
	 * Returns if the users has configured lockdetection for the index location
	 * @return
	 */
	public boolean hasLockDetection()
	{
		return(this.lockdetection);
	}
	
	/**
	 * Gets the index location configured in config
	 * @param config
	 * 			if the config does not hold the param indexLocation or if indexLocation = "RAM", an RAM Directory will be created and returned
	 * @return initialized IndexLocation
	 */
	public static synchronized IndexLocation getIndexLocation(CRConfig config)
	{
		IndexLocation dir = null;
		String key = (String)config.get(INDEX_LOCATION_KEY);
		if(key==null)
		{
			log.error("COULD NOT FIND CONFIG FOR INDEXLOCATION. check config @ "+config.getName());
			return null;
		}
		if(indexmap==null)
		{
			indexmap = new Hashtable<String,IndexLocation>();
			dir = new IndexLocation(config);
			indexmap.put(key, dir);
		}
		else
		{
			dir = indexmap.get(key);
			if(dir==null)
			{
				dir = new IndexLocation(config);
				indexmap.put(key, dir);
			}
		}
		
		return dir;
	}
	
	/**
	 * Get number of documents in Index
	 * @return doccount
	 */
	public int getDocCount()
	{
		//TODO implement this generic
		int count = 0;
		return count;
	}
	
	/**
	 * Checks for reopen file and reopens indexAccessor
	 * @param indexAccessor
	 * @return true if indexAccessor has been reopened
	 */
	public boolean reopenCheck(IndexAccessor indexAccessor)
	{
		boolean reopened = false;
		if(this.reopencheck)
		{
			try
			{
				log.debug("Check for reopen file at "+this.getReopenFilename());
				File reopenFile = new File(this.getReopenFilename());
				if(reopenFile.exists())
				{
					reopenFile.delete();

					//release writer (Readers and Searchers are refreshed after a Writer is released.)
					IndexWriter tempWriter = indexAccessor.getWriter();
					indexAccessor.release(tempWriter);
					reopened = true;
					log.debug("Reopened index.");
				}
			}catch(Exception ex)
			{
				log.error(ex.getMessage());
				ex.printStackTrace();
			}
		}
		return reopened;
	}

	/**
	 * Tests if the IndexLocation contains an existing Index and returns true if it does.
	 * @return true if index exists, otherwise false
	 */
	public boolean isContainingIndex() {
		
		return getDocCount()>0;
	}
	
	/**
	 * Creates a new CRIndexJob for the given CRConfig and adds the job to the queue
	 * @param config
	 * @param configmap 
	 * @return
	 */
	public boolean createCRIndexJob(CRConfig config,Hashtable<String,CRConfigUtil> configmap)
	{
		String updatejobimplementationClassName = config.getString(UPDATEJOBCLASS_KEY);
		if(updatejobimplementationClassName == null){
			updatejobimplementationClassName = DEFAULT_UPDATEJOBCLASS;
		}
		Class<?> updatejobImplementationClassGeneric;
		Class<? extends AbstractUpdateCheckerJob> updatejobImplementationClass;
		AbstractUpdateCheckerJob indexJob = null;
		try {
			updatejobImplementationClassGeneric = Class.forName(updatejobimplementationClassName);
			updatejobImplementationClass = updatejobImplementationClassGeneric.asSubclass(AbstractUpdateCheckerJob.class);
			Constructor<? extends AbstractUpdateCheckerJob> updatejobImplementationClassConstructor = updatejobImplementationClass.getConstructor(new Class[]{CRConfig.class, IndexLocation.class, Hashtable.class});
			Object indexJobObject = updatejobImplementationClassConstructor.newInstance(config,this,configmap);
			indexJob = (AbstractUpdateCheckerJob) indexJobObject;
			return this.queue.addJob(indexJob);
		} catch (ClassCastException e){
			log.error("Please configure an implementation of "+AbstractUpdateCheckerJob.class+" ",e);
		} catch (ClassNotFoundException e) {
			log.error("Cannot load class for creating a new IndexJob",e);
		} catch (SecurityException e) {
			log.error("Cannot load class for creating a new IndexJob",e);
		} catch (NoSuchMethodException e) {
			log.error("Cannot find constructor for creating a new IndexJob",e);
		} catch (IllegalArgumentException e) {
			log.error("Error creating a new IndexJob",e);
		} catch (InstantiationException e) {
			log.error("Error creating a new IndexJob",e);
		} catch (IllegalAccessException e) {
			log.error("Error creating a new IndexJob",e);
		} catch (InvocationTargetException e) {
			log.error("Error creating a new IndexJob",e);
		}
		return false;
	}
	
	private static final String CR_KEY = "CR";
	
	/**
	 * Creats jobs for all configured CRs
	 */
	public void createAllCRIndexJobs() {
		
		Hashtable<String,CRConfigUtil> configs = getCRMap();

		for (Entry<String,CRConfigUtil> e:configs.entrySet()) {
			
				CRConfigUtil crC = e.getValue();
				createCRIndexJob(new CRConfigUtil(crC,crC.getName()),configs);
			
		}
		

	}
	
	/**
	 * Creates a map of the configured CRs
	 * @return
	 */
	public Hashtable<String,CRConfigUtil> getCRMap()
	{
		CRConfig crconfig = this.config;
		Hashtable<String,CRConfigUtil> map = new Hashtable<String,CRConfigUtil>();
		
		GenericConfiguration CRc = (GenericConfiguration)crconfig.get(CR_KEY);
		if(CRc!=null)
		{
			Hashtable<String,GenericConfiguration> configs = CRc.getSubConfigs();

			for (Entry<String,GenericConfiguration> e:configs.entrySet()) {
				try {
					map.put(crconfig.getName()+"."+e.getKey(), new CRConfigUtil(e.getValue(),crconfig.getName()+"."+e.getKey()));
				} catch (Exception ex){
					String name="<no config name>";
					String key ="<no key>";
					CRException cex = new CRException(ex);
					if(e!=null && e.getKey()!=null)key=e.getKey();
					if(crconfig!=null && crconfig.getName()!=null)name=crconfig.getName();
					log.error("Error while creating cr map for "+name+"."+key+"  - "+cex.getMessage()+" - "+cex.getStringStackTrace());
					cex.printStackTrace();
				}
			}
		}
		else
		{
			log.error("THERE ARE NO CRs CONFIGURED FOR INDEXING.");
		}
		return map;
	}
	
	/**
	 * Returns the IndexJobQueue
	 * @return
	 */
	public IndexJobQueue getQueue()
	{
		return this.queue;
	}
	
	/**
	 * Tests if this IndexLocation has turned on periodical indexing
	 * @return
	 */
	public boolean isPeriodical()
	{
		return this.periodical;
	}
	
	/**
	 * Stops all Index workers
	 */
	public void stop()
	{
		if(this.periodical_thread!=null && this.periodical_thread.isAlive())
		{
			this.periodical_thread.interrupt();
			try {
				this.periodical_thread.join();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(this.queue!=null)
		{
			this.queue.stop();
		}
		IndexAccessorFactory.getInstance().close();
	}
	
}
