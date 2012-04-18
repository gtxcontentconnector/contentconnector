package com.gentics.cr.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gentics.cr.util.CRUtil;

public class GenericConfigurationTest {

	private URL confPath;

	@Before
	public void setUp() throws Exception {
		confPath = new File(this.getClass().getResource("nodelog.properties").toURI()).getParentFile().toURI().toURL();
		System.setProperty(CRUtil.PORTALNODE_CONFPATH, confPath.getPath());
		EnvironmentConfiguration.setCacheFilePath("${" + CRUtil.PORTALNODE_CONFPATH + "}/cache.ccf");
		EnvironmentConfiguration.loadLoggerProperties();
		EnvironmentConfiguration.loadCacheProperties();
	}

	@After
	public void cleanup() throws Exception {

	}

	@Test
	public void testReadSimpleEntry() throws IOException {
		GenericConfiguration config = new GenericConfiguration();
		GenericConfigurationFileLoader.load(config, "${" + CRUtil.PORTALNODE_CONFPATH + "}/baseconfig.properties");

		assertNotNull("existing property with subproperties does not exist", config.get("a"));
		assertNull("non existing property exists", config.get("nonexisting"));

		assertEquals("wrong output", "ROOTZWIKI", config.get("a.test"));

		assertEquals("wrong output", "XDA", config.get("a.b"));

		assertNotNull("Must return a GenericConfiguration", config.get("a.b.c"));
	}

	@Test
	public void testReadComplexEntryTree() throws IOException {
		GenericConfiguration config = new GenericConfiguration();
		GenericConfigurationFileLoader.load(config, "${" + CRUtil.PORTALNODE_CONFPATH + "}/baseconfig.properties");

		assertEquals("wrong output", "DDDD", config.get("a.b.c.d"));
		assertEquals("wrong output", "EEEE", config.get("a.b.c.e"));
		assertEquals("wrong output", "FFFF", config.get("a.b.c.f"));
		assertEquals("wrong output", "HHHH", config.get("a.b.g.h"));

		assertEquals(
			"There seems to be a problem with dots",
			"com.gentics.cr.lucene.search.query.CRQueryParser",
			config.get("rp.1.queryparser.class"));
		assertEquals("Sonderzeichen []", "[Module:Suche]", config.get("cr.velocity.frameplaceholder"));
		assertEquals(
			"html tag",
			"<html><body><img src=\"test.png\"></img></body></html>",
			config.get("rp.1.highlighter.2.highlightpostfix"));
		assertNotNull("template problem", config.get("index.test.CR.FILES.transformer.63.template"));
	}

	@Test
	public void testResolveSystemProperty() throws IOException {
		GenericConfiguration config = new GenericConfiguration();
		GenericConfigurationFileLoader.load(config, "${" + CRUtil.PORTALNODE_CONFPATH + "}/baseconfig.properties");

		assertNotNull("system property could not be resolved", config.get("systempath"));
	}

	/*
	 * @Test public void testResolvePropertyFromDifferentFile() throws IOException { GenericConfiguration config = new
	 * GenericConfiguration(); GenericConfigurationFileLoader.load(config, "${" + CRUtil.PORTALNODE_CONFPATH + "}/subconfig.properties"); }
	 * @Test public void testResolvePropertyTreeFromDifferentFile() throws IOException { GenericConfiguration config = new
	 * GenericConfiguration(); GenericConfigurationFileLoader.load(config, "${" + CRUtil.PORTALNODE_CONFPATH + "}/subconfig.properties"); }
	 */

}
