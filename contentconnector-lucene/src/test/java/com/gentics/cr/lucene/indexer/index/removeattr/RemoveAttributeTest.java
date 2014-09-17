package com.gentics.cr.lucene.indexer.index.removeattr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gentics.cr.CRConfigFileLoader;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.lucene.indexer.index.LuceneIndexLocation;
import com.gentics.cr.util.CRUtil;
import com.gentics.cr.util.indexing.AbstractUpdateCheckerJob;
import com.gentics.cr.util.indexing.IndexController;

/**
 * Test case for re-indexing objects from a request processor, where attributes
 * that were indexed are removed (Set to value "null")
 */
public class RemoveAttributeTest {
	private static File temp;

	private static IndexController ic;

	private static RequestProcessor rq;

	@BeforeClass
	public static void setUp() throws Exception {
		Path tempDir = Files.createTempDirectory("lucene");
		temp = tempDir.toFile();
		temp.mkdirs();
		File restDir = new File(temp, "rest");
		restDir.mkdirs();
		System.out.println(restDir.getAbsolutePath());
		copyFilesToTemp(restDir, new String[] { "indexer.properties", "analyzer.properties", "search.properties", "stopwords.list" });
		copyFilesToTemp(temp, new String[] { "cache.ccf" });
		System.setProperty(CRUtil.PORTALNODE_CONFPATH, temp.getAbsolutePath());
	}

	@AfterClass
	public static void tearDown() throws IOException {
		if (rq != null) {
			rq.finalize();
			rq = null;
		}
		if (ic != null) {
			ic.stop();
			ic = null;
		}
		FileUtils.deleteDirectory(temp);
	}

	private static void copyFilesToTemp(File restDir, String[] filenames) throws IOException, URISyntaxException {
		for (String name : filenames) {
			URL uri = RemoveAttributeTest.class.getResource(name);

			Path target = new File(restDir, name).toPath();
			Path source = new File(uri.toURI()).toPath();
			System.out.println("From" + source + " to " + target);
			Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private static LuceneIndexLocation getIndexerLocation() {
		if (ic == null) {
			CRConfigFileLoader config = new CRConfigFileLoader("indexer", null);
			ic = new IndexController(config);
		}
		return (LuceneIndexLocation) ic.getIndexes().get("DEFAULT");
	}

	private static RequestProcessor getSearchRequestProcessor() throws CRException {
		if (rq == null) {
			CRConfigFileLoader config = new CRConfigFileLoader("search", null);
			rq = config.getNewRequestProcessorInstance(1);
		}
		return rq;
	}

	private LuceneIndexLocation index() {
		LuceneIndexLocation indexLoc = getIndexerLocation();
		indexLoc.createAllCRIndexJobs();
		AbstractUpdateCheckerJob job = indexLoc.getQueue().getAndRemoveNextJob();
		job.run();
		return indexLoc;
	}

	/**
	 * Test re-indexing with some attributes removed or added
	 * @throws Exception
	 */
	@Test
	public void testRemoveAttribute() throws Exception {
		indexAndQuery(true, "10007.1", "10007.2", "10007.3", "10007.4", "10007.5");
		indexAndQuery(false);
		indexAndQuery(true, "10007.1", "10007.2", "10007.3", "10007.4", "10007.5");
	}

	/**
	 * Let the index job run and make a query afterwards
	 * @param withAttribute true if the attributes shall be indexed, false if not
	 * @param expectedContentIds optional list of expected content id's
	 * @throws Exception
	 */
	protected void indexAndQuery(boolean withAttribute, String... expectedContentIds) throws Exception {
		RemoveAttributeRequestProcessor.withAttribute = withAttribute;
		RemoveAttributeRequestProcessor.timestamp++;
		LuceneIndexLocation indexLoc = index();
		assertEquals("Doc count did not match.", 10, indexLoc.getDocCount());
		indexLoc.finalize();

		// search for category
		RequestProcessor rp = getSearchRequestProcessor();
		CRRequest request = new CRRequest();
		request.setRequestFilter("attr:value1");

		Collection<CRResolvableBean> ret = rp.getObjects(request);
		Set<String> expectedSet = new HashSet<String>();
		for (String expected : expectedContentIds) {
			expectedSet.add(expected);
		}
		for (CRResolvableBean obj : ret) {
			String contentId = obj.getContentid();
			assertTrue("Object " + contentId + " was found, but not expected", expectedSet.contains(contentId));
			expectedSet.remove(contentId);
		}

		assertTrue("Some expected objects were not found: " + expectedSet, expectedSet.isEmpty());
	}
}
