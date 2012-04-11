package com.gentics.cr.util.indexing;

import java.util.Hashtable;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.gentics.cr.CRConfigFileLoader;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRDatabaseFactory;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.monitoring.MonitorFactory;

/**
 * Operates as an Interface between the servlet and the Indexer Engine.
 * Each instance of this class spawns IndexJobQueueWorker threads for each index.
 * Therefore be careful with initializing this class in webservices!
 * NOT threadsafe atm.
 * 
 * Last changed: $Date: 2009-09-02 17:57:48 +0200 (Mi, 02 Sep 2009) $
 * @version $Revision: 180 $
 * @author $Author: supnig@constantinopel.at $
 */
public class IndexController {

	/**
	 * Log4J Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(IndexController.class);

	/**
	 * Configuration key.
	 */
	private static final String INDEX_KEY = "index";

	/**
	 * Configuration used internally.
	 */
	private CRConfigUtil crconfig;

	/**
	 * 
	 */
	private Hashtable<String, IndexLocation> indextable;

	/**
	 * Create new instance of IndexController.
	 * This constructor will read the config file with this name
	 * @param name of the config file
	 */
	public IndexController(final String name) {
		crconfig = new CRConfigFileLoader(name, null);
		MonitorFactory.init(crconfig);
		this.indextable = buildIndexTable();

	}

	/**
	 * Create new instance of IndexController.
	 * @param config to use for configuration
	 */
	public IndexController(final CRConfigUtil config) {
		crconfig = config;
		this.indextable = buildIndexTable();
	}

	/**
	 * Returns the current config of the indexController.
	 * @return currently in use config.
	 */
	public CRConfigUtil getConfig() {
		return this.crconfig;
	}

	/**
	 * Get table of configured indexes.
	 * @return
	 */
	public Hashtable<String, IndexLocation> getIndexes() {
		return this.indextable;
	}

	/**
	 * Build the index table.
	 * Reads the config with the index_key, reads all subconfigs and creates IndexLocations for each Index.
	 * @return IndexLocation hash identified by the indexkey.indexname
	 */
	private Hashtable<String, IndexLocation> buildIndexTable() {
		Hashtable<String, IndexLocation> indexes = new Hashtable<String, IndexLocation>(1);
		GenericConfiguration indexConfiguration = (GenericConfiguration) crconfig.get(INDEX_KEY);
		if (indexConfiguration != null) {
			Hashtable<String, GenericConfiguration> configs = indexConfiguration.getSubConfigs();

			for (Entry<String, GenericConfiguration> e : configs.entrySet()) {
				String indexLocationName = e.getKey();
				IndexLocation indexLocation = 
						IndexLocation.getIndexLocation(
							new CRConfigUtil(e.getValue(), INDEX_KEY + "." + indexLocationName));
				if (indexLocation == null) {
					LOGGER.error("Cannot get index location for " + indexLocationName);
				} else {
					indexes.put(indexLocationName, indexLocation);
				}
			}
		} else {
			LOGGER.error("THERE ARE NO INDEXES CONFIGURED FOR INDEXING.");
		}
		return indexes;
	}

	/**
	 * Finalizes all index Locations.
	 */
	public final void stop() {
		if (this.indextable != null) {
			for (Entry<String, IndexLocation> e : this.indextable.entrySet()) {
				IndexLocation il = e.getValue();
				il.stop();
			}
		}
		CRDatabaseFactory.destroy();
	}
}
