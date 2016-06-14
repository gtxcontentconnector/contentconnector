package com.gentics.cr.lucene.indexer.transformer;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.gentics.DefaultTestConfiguration;
import com.gentics.cr.configuration.EnvironmentConfiguration;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.util.CRUtil;

public class AbstractTransformerTest {

	@BeforeClass
	public static void setUp() throws CRException, URISyntaxException, IOException {
		EnvironmentConfiguration.setConfigPath(DefaultTestConfiguration.getTempConfigDirectory().getAbsolutePath());
		EnvironmentConfiguration.loadEnvironmentProperties();
	}
	
	@Test
	public void quickTest() {
		String prop = System.getProperty(CRUtil.PORTALNODE_CONFPATH);
		Assert.assertNotNull(prop);
	}
}