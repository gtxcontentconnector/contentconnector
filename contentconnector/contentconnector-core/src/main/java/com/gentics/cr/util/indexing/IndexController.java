package com.gentics.cr.util.indexing;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

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
	
	private static final ConcurrentHashMap<String, IndexController> controllers = new ConcurrentHashMap<String, IndexController>(1);

	/**
	 * Configuration used internally.
	 */
	private CRConfigUtil crconfig;

	/**
	 * 
	 */
	private ConcurrentHashMap<String, IndexLocation> indextable;

	/**
	 * Create new instance of IndexController.
	 * This constructor will read the config file with this name
	 * @param name of the config file
	 */
	private IndexController(final String name) {
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
	 */
	public ConcurrentHashMap<String, IndexLocation> getIndexes() {
		return this.indextable;
	}

	/**
	 * Build the index table.
	 * Reads the config with the index_key, reads all subconfigs and creates IndexLocations for each Index.
	 * @return IndexLocation hash identified by the indexkey.indexname
	 */
	private ConcurrentHashMap<String, IndexLocation> buildIndexTable() {
		ConcurrentHashMap<String, IndexLocation> indexes = new ConcurrentHashMap<String, IndexLocation>(1);
		GenericConfiguration indexConfiguration = (GenericConfiguration) crconfig.get(INDEX_KEY);
		if (indexConfiguration != null) {
			ConcurrentHashMap<String, GenericConfiguration> configs = indexConfiguration.getSubConfigs();

			for (Entry<String, GenericConfiguration> e : configs.entrySet()) {
				String indexLocationName = e.getKey();
				IndexLocation indexLocation = IndexLocation.getIndexLocation(new CRConfigUtil(e.getValue(), INDEX_KEY
						+ "." + indexLocationName));
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

	/**
	 * Fetch the indexcontroller with the given name.
	 * @param name name of the indexcontroller to fetch.
	 * @return index controller instance.
	 */
	public static IndexController get(final String name) {
		IndexController ic = controllers.get(name);
		if (ic == null) {
			return createNewIndexController(name);
		}
		return ic;
	}
	
	/**
	 * Creates a new instance of indexController.
	 * @param name name of the indexController
	 * @return index controller.
	 */
	private static synchronized IndexController
		createNewIndexController(final String name) {
		IndexController ic = controllers.get(name);
		if (ic == null) {
			IndexController newIC = new IndexController(name); 
			ic = controllers.putIfAbsent(name, newIC);
			if (ic == null) {
				ic = newIC;
			}
		}
		return ic;
	}
}
