package com.gentics.cr.lucene.synonyms;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.configuration.EnvironmentConfiguration;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.configuration.GenericConfigurationFileLoader;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.lucene.indexer.index.LuceneIndexLocation;
import com.gentics.cr.util.CRUtil;
import com.gentics.cr.util.indexing.IndexLocation;

/**
 * JUnit Test for the SynonymDeleteJob
 * 
 * @author patrickhoefer
 */
public class SynonymIndexDeleteJobTest {

	private SynonymIndexExtension indexExtension;
	private IndexLocation singleLoc1;
	private CRConfig config2;

	@BeforeClass
	public static void setup() throws URISyntaxException {
		String confPath = new File(SynonymIndexDeleteJobTest.class.getResource("indexer.properties").toURI()).getParentFile().getAbsolutePath();
		EnvironmentConfiguration.setConfigPath(confPath);
		EnvironmentConfiguration.loadEnvironmentProperties();
	}

	@Before
	public void create() throws CRException, FileNotFoundException, URISyntaxException {
		GenericConfiguration genericConf = new GenericConfiguration();
		try {
			String confPath2 = new File(this.getClass().getResource("indexer.properties").toURI()).getParentFile().getAbsolutePath();
			GenericConfigurationFileLoader.load(genericConf, confPath2 + "/indexer.properties");
		} catch (IOException e) {
			e.printStackTrace();
		}
		CRConfigUtil config = new CRConfigUtil(genericConf, "DEFAULT");

		GenericConfiguration sc = new GenericConfiguration();
		sc.set("indexLocations.1.path", "RAM_1");
		sc.set("indexLocationClass", "com.gentics.cr.lucene.indexer.index.LuceneSingleIndexLocation");

		CRConfig singleConfig1 = new CRConfigUtil(sc, "sc1");
		singleLoc1 = LuceneIndexLocation.getIndexLocation(singleConfig1);

		config2 = new CRConfigUtil(config.getSubConfig("index").getSubConfig("DEFAULT").getSubConfig("extensions").getSubConfig("SYN"),
				"SYN");

		indexExtension = new SynonymIndexExtension(config2, singleLoc1);

		SynonymIndexJob job = new SynonymIndexJob(config2, singleLoc1, indexExtension);
		job.run();

	}

	@Test
	public void testSynonymIndexDeleteJob() {
		SynonymIndexDeleteJob job2 = new SynonymIndexDeleteJob(config2, singleLoc1, indexExtension);
		job2.run();
	}
}
