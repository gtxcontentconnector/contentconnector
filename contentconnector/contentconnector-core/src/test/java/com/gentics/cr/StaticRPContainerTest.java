package com.gentics.cr;

import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.gentics.cr.exceptions.CRException;

public class StaticRPContainerTest{
	
	private static CRConfigUtil config;

	@BeforeClass
	public static void setUp() throws CRException, URISyntaxException {
		config = HSQLTestConfigFactory.getDefaultHSQLConfiguration(StaticRPContainerTest.class.getName(), true);
		
	}
	
	@Test
	public void testGetSame() throws CRException {
		RequestProcessor rp = StaticRPContainer.getRP(config, 1);
		RequestProcessor rpCompare = StaticRPContainer.getRP(config, 1);
		assertTrue("RP instances are not the same.", rp == rpCompare);
	}
	
	@Test
	public void testGetOther() throws CRException {
		RequestProcessor rp = StaticRPContainer.getRP(config, 1);
		RequestProcessor rpCompare = StaticRPContainer.getRP(config, 2);
		assertTrue("RP instances are the same.", rp != rpCompare);
	}
	
	

}
