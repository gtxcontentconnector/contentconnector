package com.gentics.cr.lucene.indexer;

import java.util.Hashtable;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.gentics.cr.CRConfigFileLoader;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.indexer.index.IndexLocation;

public class IndexController {
	
	private static Logger log = Logger.getLogger(IndexController.class);
	
	private static final String INDEX_KEY = "index";
	
	private CRConfigUtil crconfig;
	private Hashtable<String,IndexLocation> indextable;
	
	public IndexController(String name)
	{
		crconfig = new CRConfigFileLoader(name, null);
		this.indextable = buildIndexTable();
	}
	
	
	/**
	 * Get table of configured indexes
	 * @return
	 */
	public Hashtable<String,IndexLocation> getIndexes()
	{
		return this.indextable;
	}
	
	
	private Hashtable<String,IndexLocation> buildIndexTable()
	{
		Hashtable<String,IndexLocation> indexes = new Hashtable<String,IndexLocation>(1);
		GenericConfiguration Ic = (GenericConfiguration)crconfig.get(INDEX_KEY);
		if(Ic!=null)
		{
			Hashtable<String,GenericConfiguration> configs = Ic.getSubConfigs();

			for (Entry<String,GenericConfiguration> e:configs.entrySet()) {
				indexes.put(e.getKey(), IndexLocation.getIndexLocation(new CRConfigUtil(e.getValue(),INDEX_KEY+"."+e.getKey())));
			}
		}
		else
		{
			log.error("THERE ARE NO INDEXES CONFIGURED FOR INDEXING.");
		}
		return indexes;
	}
}
