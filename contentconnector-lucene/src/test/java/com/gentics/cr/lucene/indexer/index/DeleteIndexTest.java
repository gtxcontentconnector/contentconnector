package com.gentics.cr.lucene.indexer.index;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gentics.cr.CRConfigFileLoader;
import com.gentics.cr.lucene.indexaccessor.IndexAccessor;
import com.gentics.cr.util.CRUtil;
import com.gentics.cr.util.indexing.AbstractUpdateCheckerJob;
import com.gentics.cr.util.indexing.IndexController;
/**
 * Test deleting the index with a CRDeleteIndexJob
 * @author christopher
 *
 */
public class DeleteIndexTest {

	private static File temp;

	@BeforeClass
	public static void setUp() throws Exception {
		Path tempDir = Files.createTempDirectory("lucene");
		temp = tempDir.toFile();
		temp.mkdirs();
		File restDir = new File(temp, "rest");
		restDir.mkdirs();
		System.out.println(restDir.getAbsolutePath());
		copyFilesToTemp(restDir, new String[]{"indexer.properties", "analyzer.properties", "search.properties", "stopwords.list"});
		copyFilesToTemp(temp, new String[]{"cache.ccf"});
		System.setProperty(CRUtil.PORTALNODE_CONFPATH, temp.getAbsolutePath());
	}
	
	@AfterClass
	public static void tearDown() throws IOException {
		FileUtils.deleteDirectory(temp);
	}
	
	private static void copyFilesToTemp(File restDir, String[] filenames) throws IOException, URISyntaxException {
		for (String name : filenames) {
			URL uri = DeleteIndexTest.class.getResource(name);
			
			Path target = new File(restDir, name).toPath();
			Path source = new File(uri.toURI()).toPath();
			System.out.println("From" + source + " to "+target);
			Files.copy(source, target , StandardCopyOption.REPLACE_EXISTING);
		}
	}
	
	private static LuceneIndexLocation getIndexerLocation() {
		CRConfigFileLoader config = new CRConfigFileLoader("indexer",null);
		IndexController ic = new IndexController(config);
		return (LuceneIndexLocation) ic.getIndexes().get("DEFAULT");
	}
	
	private LuceneIndexLocation index() {
		LuceneIndexLocation indexLoc = getIndexerLocation();
		indexLoc.createAllCRIndexJobs();
		AbstractUpdateCheckerJob job = indexLoc.getQueue().getAndRemoveNextJob();
		job.run();
		return indexLoc;
	}
	
	private void clearIndex(LuceneIndexLocation indexLoc) {
		indexLoc.createClearJob();
		AbstractUpdateCheckerJob job = indexLoc.getQueue().getAndRemoveNextJob();
		job.run();
	}
	
	/**
	 * First create an index, check the contents, delete it and check the contents again.
	 * @throws IOException in case of low level IO error
	 */
	@Test
	public void testDelete() throws IOException {
		LuceneIndexLocation indexLoc = index();
		assertEquals("Doc count did not match.", DummyIndextestRequestProcessor.INDEX_ELEMENT_COUNT, indexLoc.getDocCount());
		clearIndex(indexLoc);
		assertEquals("Doc count did not match.", 0, indexLoc.getDocCount());
		indexLoc.finalize();
	}

}
