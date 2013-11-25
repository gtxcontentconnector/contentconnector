package com.gentics.cr;

import java.net.URISyntaxException;

import org.junit.BeforeClass;

import com.gentics.cr.exceptions.CRException;

public class SQLRequestProcessorTest extends AbsrtactSQLRequestProcessorTest {

	private static RequestProcessor rp;
	
	@BeforeClass
	public static void setUp() throws CRException, URISyntaxException {
		CRConfigUtil config = initConfigurationAndTest();
		rp = new SQLRequestProcessor(config.getRequestProcessorConfig(1));
	}
	
	
	@Override
	protected RequestProcessor getRequestProcessor() {
		return rp;
	}

}
