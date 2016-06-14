package com.gentics.cr;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.BeforeClass;

import com.gentics.cr.exceptions.CRException;

public class PooledSQLRequestProcessorTest extends AbsrtactSQLRequestProcessorTest {

	private static RequestProcessor rp;
	
	@BeforeClass
	public static void setUp() throws CRException, URISyntaxException, IOException {
		CRConfigUtil config = initConfigurationAndTest(PooledSQLRequestProcessorTest.class);
		rp = new PooledSQLRequestProcessor(config.getRequestProcessorConfig(1));
	}
	
	
	@Override
	protected RequestProcessor getRequestProcessor() {
		return rp;
	}

}
