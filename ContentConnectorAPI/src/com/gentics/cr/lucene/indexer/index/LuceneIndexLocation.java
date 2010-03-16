package com.gentics.cr.lucene.indexer.index;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;

import com.gentics.cr.CRConfig;
import com.gentics.cr.lucene.indexaccessor.IndexAccessor;
import com.gentics.cr.lucene.indexaccessor.IndexAccessorFactory;
import com.gentics.cr.util.indexing.IndexLocation;


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
	private static final String RAM_IDENTIFICATION_KEY = "RAM";
	//TODO Clean this
	/*private static final String REOPEN_CHECK_KEY = "reopencheck";
	private static final String INDEX_LOCATION_KEY = "indexLocation";
	
	private static final String PERIODICAL_KEY = "PERIODICAL";

	private static final String LOCK_DETECTION_KEY = "LOCKDETECTION";*/


	
	
	//Instance Members
	private Directory dir=null;
	private String name = null;
	/*private IndexJobQueue queue = null;
	private boolean periodical = false;
	private int periodical_interval = 60; //60 seconds
	private Thread periodical_thread;
	private boolean lockdetection = false;
	private boolean reopencheck = false;*/
	
	private Analyzer getConfiguredAnalyzer() 
	{
		return LuceneAnalyzerFactory.createAnalyzer(config);
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
			log.error(e.getMessage(),e);
		}
	}
	
	
	//TODO should be protected, is public because it is used in IndexLocation#createNewIndexLocation
	/**
	 * @param config 
	 * 
	 */
	public LuceneIndexLocation(CRConfig config)
	{
		super(config);
		name = config.getName();
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
	}
	
	public static synchronized LuceneIndexLocation getIndexLocation(CRConfig config) {
		IndexLocation genericIndexLocation = IndexLocation.getIndexLocation(config);
		if (genericIndexLocation instanceof LuceneIndexLocation) {
			return (LuceneIndexLocation) genericIndexLocation;
		} else {
			log.error("IndexLocation is not created for Lucene. Using the "+CRLuceneIndexJob.class.getName()+" requires that you use the "+LuceneIndexLocation.class.getName()+". You can configure another Job by setting the "+IndexLocation.UPDATEJOBCLASS_KEY+" key in your config.");
			return null;
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
	 * Returns the directory used by this index location
	 * @return
	 */
	public Directory getDirectory()
	{
		return(this.dir);
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
	
	
	
	
	
	private IndexAccessor getAccessorInstance()
	{
		Directory directory = this.getDirectory();
		if(directory == null){
			directory = this.getDirectory();
		}
		IndexAccessor indexAccessor = IndexAccessorFactory.getInstance().getAccessor(directory);
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
	
}
