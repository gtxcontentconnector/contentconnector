package com.gentics.cr.lucene.indexer.index;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import com.gentics.cr.CRConfig;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.indexaccessor.IndexAccessor;
import com.gentics.cr.lucene.indexaccessor.IndexAccessorFactory;

/**
 * @author Christopher
 *
 */
public class LuceneSingleIndexLocation extends LuceneIndexLocation {
	//Instance Members
	private Directory dir=null;
	private String indexLocation;
	
	
	private String getFirstIndexLocation(CRConfig config)
	{
		String path="";
		GenericConfiguration locs = (GenericConfiguration)config.get(INDEX_LOCATIONS_KEY);
		if(locs!=null)
		{
			Map<String,GenericConfiguration> locationmap = locs.getSortedSubconfigs();
			if(locationmap!=null)
			{
				for(GenericConfiguration locconf:locationmap.values())
				{
					String p = locconf.getString(INDEX_PATH_KEY);
					if(p!=null && !"".equals(p))
					{
						path=p;
						return path;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Create a new Instance of LuceneSingleIndexLocation. This is the Default IndexLocation for Lucene.
	 * @param config
	 */
	public LuceneSingleIndexLocation(CRConfig config) {
		super(config);
		indexLocation = getFirstIndexLocation(config);
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

	@Override
	protected IndexAccessor getAccessorInstance() 
	{
		Directory directory = this.getDirectory();
		if(directory == null){
			directory = this.getDirectory();
		}
		IndexAccessor indexAccessor = IndexAccessorFactory.getInstance().getAccessor(directory);
		return indexAccessor;
	}

	@Override
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

	@Override
	protected Directory[] getDirectories() {
		Directory[] dirs = new Directory[]{this.getDirectory()};
		return dirs;
	}
	
	private Directory getDirectory()
	{
		return this.dir;
	}
	
	
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

	@Override
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
					
					indexAccessor.reopen();
					
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

	@Override
	public void finalize() {
		IndexAccessorFactory.getInstance().close();
		
	}

}
