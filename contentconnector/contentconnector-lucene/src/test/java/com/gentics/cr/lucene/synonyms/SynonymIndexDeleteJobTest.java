package com.gentics.cr.lucene.synonyms;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Before;
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
	
	@Before
	public void setup() {
		URL confPath = null;
		try {
			confPath = new File(this.getClass().getResource("indexer.properties").toURI()).getParentFile().toURI().toURL();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.setProperty(CRUtil.PORTALNODE_CONFPATH, confPath.getPath());
		EnvironmentConfiguration.setCacheFilePath("${" + CRUtil.PORTALNODE_CONFPATH + "}/cache.ccf");
		EnvironmentConfiguration.loadLoggerProperties();
		EnvironmentConfiguration.loadCacheProperties();
	}
	
	@Before
	public void create() throws CRException, FileNotFoundException, URISyntaxException {
		GenericConfiguration genericConf = new GenericConfiguration();
		try {
			URL confPath2 = new File(this.getClass().getResource("indexer.properties").toURI()).getParentFile().toURI().toURL();
			GenericConfigurationFileLoader.load(genericConf, confPath2.getPath() + "/indexer.properties");
		} catch (IOException e) {
			e.printStackTrace();
		}
		CRConfigUtil config = new CRConfigUtil(genericConf, "DEFAULT");

		GenericConfiguration sc = new GenericConfiguration();
		sc.set("indexLocations.1.path", "RAM_1");
		sc.set("indexLocationClass", "com.gentics.cr.lucene.indexer.index.LuceneSingleIndexLocation");

		CRConfig singleConfig1 = new CRConfigUtil(sc, "sc1");
		singleLoc1 = LuceneIndexLocation.getIndexLocation(singleConfig1);
		
		
		config2 = new CRConfigUtil(config.getSubConfig("index").getSubConfig("DEFAULT").getSubConfig("extensions").getSubConfig("SYN"), "SYN");
		
		indexExtension = new SynonymIndexExtension(config2, singleLoc1);
		
		SynonymIndexJob job = new SynonymIndexJob(config2, singleLoc1, indexExtension);
		job.run();
		
	}
	
	@Test
	public void testSynonymIndexDeleteJob(){
		SynonymIndexDeleteJob job2 = new SynonymIndexDeleteJob(config2, singleLoc1, indexExtension);
		job2.run();
	}
}
