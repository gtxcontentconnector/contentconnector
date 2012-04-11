package com.gentics.cr;

import java.lang.reflect.InvocationTargetException;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;

import com.gentics.cr.configuration.EnvironmentConfiguration;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.plink.PlinkProcessor;
import com.gentics.cr.util.CRUtil;
import com.gentics.portalnode.cnintegration.PLinkProcessor;

public class CRRequestProcessorTest extends RequestProcessorTest {

	CRRequestProcessor requestProcessor;

	@Before
	public void setUp() throws CRException {
		EnvironmentConfiguration.setConfigPath(this.getClass().getResource("conf/gentics").getPath());
		EnvironmentConfiguration.loadEnvironmentProperties();

		CRConfig config = new CRConfigUtil();
		config.set(RequestProcessor.CONTENTCACHE_KEY, "false");
		config.set(PlinkProcessor.PLINK_CACHE_ACTIVATION_KEY, "false");
		config.set("ds-handle.url", "jdbc:hsqldb:" + this.getClass().getResource("hsqldbCRRequestProcessor").getPath()
				+ "/demoportal_ccr?user=SA");
		config.set("ds-handle.driverClass", "org.hsqldb.jdbcDriver");
		config.set("ds-handle.type", "jdbc");
		config.set("ds.sanitycheck", "false");
		requestProcessor = new CRRequestProcessor(config);
	}

	@Override
	protected RequestProcessor getRequestProcessor() {
		return requestProcessor;
	}

}
