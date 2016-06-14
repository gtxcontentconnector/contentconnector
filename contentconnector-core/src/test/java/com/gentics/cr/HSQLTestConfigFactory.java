package com.gentics.cr;

import java.io.IOException;
import java.net.URISyntaxException;

import com.gentics.DefaultTestConfiguration;
import com.gentics.cr.configuration.EnvironmentConfiguration;
import com.gentics.cr.plink.PlinkProcessor;

public class HSQLTestConfigFactory {
	
	private static final String CR_1_PREFIX = "RP.1.";
	
	private static final String CR_2_PREFIX = "RP.2.";
	
	/**
	 * This method generates a DB configuration for an in memory HSQL.
	 * A default empty repository will be generated.
	 * Default name: mytestdatasource
	 * @return
	 * @throws URISyntaxException 
	 */
	public static final CRConfigUtil getDefaultHSQLConfiguration(Class testClazz) throws URISyntaxException, IOException {
		return getDefaultHSQLConfiguration("mytestdatasource" + testClazz.toString());
	}
	
	/**
	 * This method generates a DB configuration for an in memory HSQL.
	 * A default empty repository will be generated.
	 * @param dsName name of the datasource
	 * @param cache true if cache should be enabled (default = false)
	 * @return
	 * @throws URISyntaxException 
	 */
	public static final CRConfigUtil getDefaultHSQLConfiguration(String dsName, boolean cache) throws URISyntaxException, IOException {
		EnvironmentConfiguration.setConfigPath(DefaultTestConfiguration.createTempConfigDirectory().getAbsolutePath());
		EnvironmentConfiguration.loadEnvironmentProperties();

		CRConfigUtil config = new CRConfigUtil();
		config.setName(dsName);
		addRequestProcessor(CR_1_PREFIX, dsName, cache, config);
		addRequestProcessor(CR_2_PREFIX, dsName, cache, config);
		return config;
	}

	private static void addRequestProcessor(final String rpPrefix, final String dsName, final boolean cache,
			final CRConfigUtil config) {
		config.set(rpPrefix + "rpClass", "com.gentics.cr.CRRequestProcessor");
		if (!cache) {
			config.set(rpPrefix + RequestProcessor.CONTENTCACHE_KEY, "false");
			config.set(rpPrefix + PlinkProcessor.PLINK_CACHE_ACTIVATION_KEY, "false");
		}
		config.set(rpPrefix + "ds-handle.url", "jdbc:hsqldb:mem:" + dsName);
		config.set(rpPrefix + "ds-handle.driverClass", "org.hsqldb.jdbcDriver");
		config.set(rpPrefix + "ds-handle.shutDownCommand", "SHUTDOWN");
		config.set(rpPrefix + "ds-handle.type", "jdbc");
		config.set(rpPrefix + "ds.sanitycheck2", "true");
		config.set(rpPrefix + "ds.autorepair2", "true");
		config.set(rpPrefix + "ds.sanitycheck", "false");
		config.set(rpPrefix + "ds.table", dsName);
		config.set(rpPrefix + "ds.idcolumn", "contentid");
	}
	
	/**
	 * This method generates a DB configuration for an in memory HSQL.
	 * A default empty repository will be generated.
	 * @param dsName name of the datasource
	 * @return
	 * @throws URISyntaxException 
	 */
	public static final CRConfigUtil getDefaultHSQLConfiguration(String dsName) throws URISyntaxException, IOException {
		return getDefaultHSQLConfiguration(dsName, false);
	}

}
