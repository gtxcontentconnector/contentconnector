package com.gentics.cr.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.gentics.cr.CRConfigUtil;
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

	@Test
	public void testReadSimpleEntry() throws IOException {
		GenericConfiguration config = new GenericConfiguration();
		GenericConfigurationFileLoader.load(config, "${" + CRUtil.PORTALNODE_CONFPATH + "}/baseconfig.properties");

		assertNotNull("existing property with subproperties does not exist", config.get("a"));
		assertNull("non existing property exists", config.get("nonexisting"));

		assertEquals("wrong output", "ROOTZWIKI", config.get("a.test"));

		assertEquals("wrong output", "ROOTZWIKI", config.get("A.TEST"));

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

	@Test
	@Ignore
	public void testResolveFromDifferentFile() throws IOException {
		GenericConfiguration config = new GenericConfiguration();
		GenericConfigurationFileLoader.load(config, "${" + CRUtil.PORTALNODE_CONFPATH + "}/subconfig.properties");

		assertNull("must be null", config.get("f"));
		assertNull("must be null", config.get("e"));

		/**
		 * Test uppercase and lowercase resolving.
		 */
		assertEquals("not of class genericconfiguration", GenericConfiguration.class, config.get("PARENTA").getClass());
		assertEquals("not equals", "DDDD", config.get("PARENTA.d"));
		assertEquals("not equals", "EEEE", config.get("PARENTA.e"));
		assertEquals("not equals", "DDDD", config.get("parentA.d"));
		assertNotNull("must not be null", config.get("REFA"));
		assertEquals("must be set", "blub", config.get("REFA"));
		assertEquals("not equals", "DDDD", config.get("PARENTAVAR"));
		assertEquals("not of class genericconfiguration", GenericConfiguration.class, config.get("parenta").getClass());
		assertEquals("not equals", "DDDD", config.get("parenta.d"));
		assertEquals("not equals", "EEEE", config.get("parenta.e"));
		assertEquals("not equals", "DDDD", config.get("parenta.d"));
		assertNotNull("must not be null", config.get("refa"));
		assertEquals("must be set", "blub", config.get("refa"));
		assertEquals("not equals", "DDDD", config.get("parentavar"));

		/**
		 * test other case in the config file.
		 */
		assertEquals("not of class genericconfiguration", GenericConfiguration.class, config.get("parentb").getClass());
		assertEquals("not equals", "DDDD", config.get("parentb.d"));
		assertEquals("not equals", "EEEE", config.get("parentb.e"));
		assertEquals("not equals", "DDDD", config.get("parentb.d"));
		assertNotNull("must not be null", config.get("refb"));
		assertEquals("must be set", "blub", config.get("refb"));
		assertEquals("not equals", "DDDD", config.get("parentbvar"));

		assertEquals("not of class genericconfiguration", GenericConfiguration.class, config.get("c").getClass());
		assertEquals("not equals", "DDDD", config.get("d"));

		assertEquals("multiline string concatenated to one line", String.class, config.get("template").getClass());

		assertNotNull("requestprocessor must be set", config.get("rp"));
		assertNotNull("referenced request processor config", config.get("rp.1.highlighter.2.highlightpostfix"));
	}

	@Test
	public void testJsonRequestProcessorConfig() throws IOException {
		GenericConfiguration config = new GenericConfiguration();
		GenericConfigurationFileLoader.load(config, "${" + CRUtil.PORTALNODE_CONFPATH + "}/baseconfig.properties");

		Object obj = config.get(CRConfigUtil.REQUEST_PROCESSOR_KEY + "." + 1);

		if (obj != null && obj instanceof GenericConfiguration) {
			System.out.println("success");
		}
	}

}
