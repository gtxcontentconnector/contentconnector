package com.gentics.cr.configuration;

import java.io.File;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;

import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.exceptions.CRException;

/**
 * Test the simple cr config as it's used in implementing projects for testing.
 * If you have to change something in here it is strong hint for a manual change.
 * @author bigbear3001
 *
 */
public class SimpleCRConfigTest {


	@BeforeClass
	public static void setUp() throws Exception {
		String confPath = new File(SimpleCRConfigTest.class.getResource("cache.ccf").toURI()).getParentFile().getAbsolutePath();
		EnvironmentConfiguration.setConfigPath(confPath);
		EnvironmentConfiguration.loadEnvironmentProperties();
	}
	
	@Test
	public void testInitRequestProcessorWithSimpleConfig() throws CRException {
		SimpleCRConfig config = new SimpleCRConfig();
		TestRequestProcessor rp = new TestRequestProcessor(config);
	}
	
	private class TestRequestProcessor extends RequestProcessor {

		public TestRequestProcessor(SimpleCRConfig config) throws CRException {
			super(config);
		}

		@Override
		public Collection<CRResolvableBean> getObjects(CRRequest request, boolean doNavigation) throws CRException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void finalize() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
}
