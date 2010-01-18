package com.gentics.cr.lucene.indexer.index;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.lucene.indexaccessor.IndexAccessor;
import com.gentics.cr.lucene.indexaccessor.IndexAccessorFactory;
import com.gentics.cr.lucene.indexer.IndexerUtil;
import com.gentics.cr.util.indexing.IndexJobQueue;


/**
 * 
 * Last changed: $Date: 2009-07-10 10:49:03 +0200 (Fr, 10 Jul 2009) $
 * @version $Revision: 131 $
 * @author $Author: supnig@constantinopel.at $
 *
 */

public class LuceneIndexLocation extends com.gentics.cr.util.indexing.IndexLocation{
	//STATIC MEMBERS
	protected static final Logger log = Logger.getLogger(LuceneIndexLocation.class);
	private static final String REOPEN_CHECK_KEY = "reopencheck";
	private static final String REOPEN_FILENAME = "reopen";
	private static final String INDEX_LOCATION_KEY = "indexLocation";
	private static final String RAM_IDENTIFICATION_KEY = "RAM";
	private static final String PERIODICAL_KEY = "PERIODICAL";
	private static Hashtable<String,LuceneIndexLocation> indexmap;
	private static final String STEMMING_KEY = "STEMMING";
	private static final String STEMMER_NAME_KEY = "STEMMERNAME";
	private static final String STOP_WORD_FILE_KEY = "STOPWORDFILE";
	private static final String LOCK_DETECTION_KEY = "LOCKDETECTION";
	
	
	//Instance Members
	private Directory dir=null;
	private String name = null;
	private IndexJobQueue queue = null;
	private CRConfig config;
	private boolean periodical = false;
	private int periodical_interval = 60; //60 seconds
	private Thread periodical_thread;
	private boolean lockdetection = false;
	private boolean reopencheck = false;
	private String indexLocation ="";
	
	private Analyzer getConfiguredAnalyzer() 
	{
		//Update/add Documents
		Analyzer analyzer;
		boolean doStemming = Boolean.parseBoolean((String)config.get(STEMMING_KEY));
		if(doStemming)
		{
			analyzer = new SnowballAnalyzer(Version.LUCENE_CURRENT,(String)config.get(STEMMER_NAME_KEY));
		}
		else
		{
			
			//Load StopWordList
			File stopWordFile = IndexerUtil.getFileFromPath((String)config.get(STOP_WORD_FILE_KEY));
			
			if(stopWordFile!=null)
			{
				//initialize Analyzer with stop words
				try
				{
					analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT,stopWordFile);
				}
				catch(IOException ex)
				{
					log.error("Could not open stop words file. Will create standard analyzer. "+ex.getMessage());
					analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
				}
			}
			else
			{
				//if no stop word list exists load fall back
				analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
			}
		}
		return analyzer;
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
		IndexAccessor indexAccessor = getAccessor();
		IndexWriter indexWriter;
		try {
			indexWriter = indexAccessor.getWriter();
			indexAccessor.release(indexWriter);
		}catch (LockObtainFailedException e)
		{
			throw new LockedIndexException(e);
		} catch (IOException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
	}
	
	
	private LuceneIndexLocation(CRConfig config)
	{
		super(config);
		this.config = config;
		name = config.getName();
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
		if(RAM_IDENTIFICATION_KEY.equalsIgnoreCase(indexLocation) || indexLocation==null || indexLocation.startsWith(RAM_IDENTIFICATION_KEY))
		{
			dir = new RAMDirectory();
			
		}
		else
		{
			File indexLoc = new File(indexLocation);
			try
			{
				dir = createFSDirectory(indexLoc);
				if(dir==null) dir = createRAMDirectory();
			}
			catch(IOException ioe)
			{
				dir = createRAMDirectory();
			}
		}
		//Create index accessor
		IndexAccessorFactory IAFactory = IndexAccessorFactory.getInstance();
		if(!IAFactory.hasAccessor(dir)){
			try
			{
				IAFactory.createAccessor(dir, getConfiguredAnalyzer());
			}
			catch(IOException ex)
			{
				log.fatal("COULD NOT CREATE INDEX ACCESSOR"+ex.getMessage());
			}
		}
		else{
			log.debug("Accessor already present. we will not create a new one.");
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
	
	private Directory createRAMDirectory()
	{
		Directory dir = new RAMDirectory();
		log.debug("Creating RAM Directory for Index ["+name+"]");
		return(dir);
	}
	
	private Directory createFSDirectory(File indexLoc) throws IOException
	{
		if(!indexLoc.exists())
		{
			log.debug("Indexlocation did not exist. Creating directories...");
			indexLoc.mkdirs();
		}
		Directory dir = FSDirectory.open(indexLoc);
		log.debug("Creating FS Directory for Index ["+name+"]");
		return(dir);
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
	 * Returns the directory used by this index location
	 * @return
	 */
	public Directory getDirectory()
	{
		return(this.dir);
	}
	
	/**
	 * Gets the index location configured in config
	 * @param config
	 * 			if the config does not hold the param indexLocation or if indexLocation = "RAM", an RAM Directory will be created and returned
	 * @return initialized IndexLocation
	 */
	public static synchronized LuceneIndexLocation getIndexLocation(CRConfig config)
	{
		LuceneIndexLocation dir = null;
		String key = (String)config.get(INDEX_LOCATION_KEY);
		if(key==null)
		{
			log.error("COULD NOT FIND CONFIG FOR INDEXLOCATION. check config @ "+config.getName());
			return null;
		}
		if(indexmap==null)
		{
			indexmap = new Hashtable<String,LuceneIndexLocation>();
			dir = new LuceneIndexLocation(config);
			indexmap.put(key, dir);
		}
		else
		{
			dir = indexmap.get(key);
			if(dir==null)
			{
				dir = new LuceneIndexLocation(config);
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
		IndexAccessor indexAccessor = this.getAccessor();
		IndexReader reader  = null;
		int count = 0;
		try
		{
			reader = indexAccessor.getReader(false);
			count = reader.numDocs();
		}catch(IOException ex)
		{
			log.error("IOX happened during test of index. "+ex.getMessage());
		}
		finally{
			indexAccessor.release(reader, false);
		}
		
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
	
	
	
	
	
	private IndexAccessor getAccessorInstance()
	{
		IndexAccessor indexAccessor = IndexAccessorFactory.getInstance().getAccessor(this.getDirectory());
		return indexAccessor;
	}
	
	/**
	 * Returns an index Accessor, which can be used to share access to an index over multiple threads
	 * @return
	 */
	public IndexAccessor getAccessor()
	{
		IndexAccessor indexAccessor = getAccessorInstance();
		reopenCheck(indexAccessor);
		return indexAccessor;
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
		return this.queue.addJob(new CRLuceneIndexJob(config,this,configmap));
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
