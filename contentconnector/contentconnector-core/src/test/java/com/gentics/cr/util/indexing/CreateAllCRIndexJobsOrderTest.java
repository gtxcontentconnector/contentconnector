package com.gentics.cr.util.indexing;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.conf.gentics.ConfigDirectory;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.configuration.GenericConfigurationFileLoader;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.util.indexing.update.filesystem.FileSystemUpdateCheckerTest;


/**
 * Check if createAllCRIndexJobs() creates the CRIndexJobs in correct order (alphabetically)
 * 
 * @author patrickhoefer
 */
public class CreateAllCRIndexJobsOrderTest {

	private static final Logger LOGGER = Logger.getLogger(FileSystemUpdateCheckerTest.class);

	static CRConfigUtil config = new CRConfigUtil();
	
	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private final PrintStream saveOut = System.out;

	DummyIndexLocation2 indexLocation;


	@BeforeClass
	public static void init() throws URISyntaxException {
		ConfigDirectory.useThis();
	}

	public CreateAllCRIndexJobsOrderTest(){

	}
	@Before
	public void setUpStreams() {		
	    System.setOut(new PrintStream(outContent));
	}


	@Test
	public void testUpdateFiles() throws CRException, FileNotFoundException, URISyntaxException {
		GenericConfiguration genericConf = new GenericConfiguration();
		try {
			URL confPath2 = new File(this.getClass().getResource("testOrder.properties").toURI()).getParentFile().toURI().toURL();
			GenericConfigurationFileLoader.load(genericConf, confPath2.getPath()+"/testOrder.properties");
		} catch (IOException e) {
			e.printStackTrace();
		}
		CRConfigUtil config = new CRConfigUtil(genericConf, "Test");
	
		indexLocation = new DummyIndexLocation2(config);
		indexLocation.createAllCRIndexJobs();
		assertEquals(outContent.toString(),"Create Job: Test.AFILESBRANCHENKAERNTEN Create Job: Test.AFILESBRANCHENWIEN Create Job: Test.APAGESBRANCHENKAERNTEN Create Job: Test.APAGESBRANCHENWIEN Create Job: Test.FILES Create Job: Test.PAGES ");
	}
	
	@After
	public void cleanUpStreams() {
	    System.setOut(saveOut);
	}
}
