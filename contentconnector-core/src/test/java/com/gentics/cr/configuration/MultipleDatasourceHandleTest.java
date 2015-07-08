package com.gentics.cr.configuration;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.gentics.api.lib.datasource.Datasource;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.util.CRUtil;

/**
 * Test with mutliple Datasources Handles Used config:
 * multipleDatasourceHandle.properties Create 2 HSQL-DBs and register them for a
 * datasource. Check if 2 different db handles are returned with getHandle()
 * 
 * @author p.hoefer@gentics.com
 */
public class MultipleDatasourceHandleTest {

	private String confPath;

	@Before
	public void setUp() throws Exception {
		confPath = new File(this.getClass().getResource("nodelog.properties")
				.toURI()).getParentFile().getAbsolutePath();
		System.setProperty(CRUtil.PORTALNODE_CONFPATH, confPath);
		EnvironmentConfiguration.setCacheFilePath("${"
				+ CRUtil.PORTALNODE_CONFPATH + "}/cache.ccf");
		EnvironmentConfiguration.loadLoggerProperties();
		EnvironmentConfiguration.loadCacheProperties();
	}

	@Test
	public void testMultipleHandles() throws CRException, IOException {

		GenericConfiguration config = new GenericConfiguration();

		// load multipleDatasourceHandle config with defined hsqldb
		GenericConfigurationFileLoader.load(config, "${"
				+ CRUtil.PORTALNODE_CONFPATH
				+ "}/multipleDatasourceHandle.properties");

		CRConfigUtil crConfig = new CRConfigUtil(config, "crConfig");

		assertNotNull("crConfig is null", crConfig);

		crConfig.initDS();

		assertNotNull("first request processor is null",
				crConfig.getRequestProcessorConfig(1));
		assertNotNull("datasource is null",
				crConfig.getRequestProcessorConfig(1).getDatasource());
		Datasource ds = crConfig.getRequestProcessorConfig(1).getDatasource();
		assertNotNull("datasource handlePool is null", ds.getHandlePool());

		// check if fetched handle is not the same like next fetched Handle
		// (Round robin, first and second handle should be used)
		assertNotEquals("both fetched datasource handles are the same", ds
				.getHandlePool().getHandle(), ds.getHandlePool().getHandle());

	}

}
