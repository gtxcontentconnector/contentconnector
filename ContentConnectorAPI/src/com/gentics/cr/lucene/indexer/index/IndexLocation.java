package com.gentics.cr.lucene.indexer.index;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRException;
import com.gentics.cr.configuration.GenericConfiguration;


/**
 * 
 * Last changed: $Date: 2009-07-10 10:49:03 +0200 (Fr, 10 Jul 2009) $
 * @version $Revision: 131 $
 * @author $Author: supnig@constantinopel.at $
 *
 */

public class IndexLocation {
	protected static Logger log = Logger.getLogger(IndexLocation.class);
	private static final String INDEX_LOCATION_KEY = "indexLocation";
	private static final String RAM_IDENTIFICATION_KEY = "RAM";
	private static final String PERIODICAL_KEY = "PERIODICAL";
	private static Hashtable<String,IndexLocation> indexmap;
	
	private Directory dir=null;
	private String name = null;
	private IndexJobQueue queue = null;
	private CRConfig config;
	private boolean periodical = false;
	private int periodical_interval = 60; //60 seconds
		
	private IndexLocation(CRConfig config)
	{
		this.config = config;
		name = config.getName();
		String indexLocation = (String)config.get(INDEX_LOCATION_KEY);
		queue = new IndexJobQueue(config);
		String per = (String)config.get(PERIODICAL_KEY);
		periodical = Boolean.parseBoolean(per);
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
		if(periodical)
		{
			Thread d = new Thread(new Runnable(){
				public void run()
				{
					boolean interrupted = false;
					while(periodical && !interrupted)
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
			d.setName("PeriodicIndexJobCreator");
			d.start();
			this.queue.startWorker();
		}
		
	}
	
	private Directory createRAMDirectory()
	{
		Directory dir = new RAMDirectory();
		log.debug("Creating RAM Directory for Index ["+name+"]");
		return(dir);
	}
	
	private Directory createFSDirectory(File indexLoc) throws IOException
	{
		Directory dir = FSDirectory.getDirectory(indexLoc);
		log.debug("Creating FS Directory for Index ["+name+"]");
		return(dir);
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
	public static synchronized IndexLocation getIndexLocation(CRConfig config)
	{
		IndexLocation dir = null;
		String key = (String)config.get(INDEX_LOCATION_KEY);
		
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
	 * Tests if the IndexLocation contains an existing Index and returns true if it does.
	 * @return true if index exists, otherwise false
	 */
	public boolean isContainingIndex() {
		boolean index = false;
		try
		{
			if(this.dir!=null && this.dir.list()!=null && this.dir.list().length>0)
				index=true;
		}
		catch(IOException iox)
		{
			log.debug("IOX happened during test for existing index. Returning false.");
		}
		
		return index;
	}
	
	/**
	 * Creates a new CRIndexJob for the given CRConfig and adds the job to the queue
	 * @param config
	 * @return
	 */
	public boolean createCRIndexJob(CRConfig config,Hashtable<String,CRConfigUtil> configmap)
	{
		return this.queue.addJob(new CRIndexJob(config,this,configmap));
	}
	
	private static final String CR_KEY = "CR";
	
	
	public void createAllCRIndexJobs() {
		
		Hashtable<String,CRConfigUtil> configs = getCRMap();

		for (Entry<String,CRConfigUtil> e:configs.entrySet()) {
			
				CRConfigUtil crC = e.getValue();
				createCRIndexJob(new CRConfigUtil(crC,crC.getName()),configs);
			
		}
		

	}
	
	
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
	
	public boolean isPeriodical()
	{
		return this.periodical;
	}
	
	
}
