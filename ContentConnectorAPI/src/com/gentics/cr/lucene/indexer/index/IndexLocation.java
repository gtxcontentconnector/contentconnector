package com.gentics.cr.lucene.indexer.index;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import com.gentics.cr.CRConfig;


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
	private static Hashtable<String,IndexLocation> indexmap;
	
	private Directory dir=null;
	private String name = null;
		
	private IndexLocation(CRConfig config)
	{
		name = config.getName();
		String indexLocation = (String)config.get(INDEX_LOCATION_KEY);
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
	
}
