package com.gentics.cr.lucene.indexer.index;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gentics.cr.CRConfigFileLoader;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.util.CRUtil;
import com.gentics.cr.util.indexing.AbstractUpdateCheckerJob;
import com.gentics.cr.util.indexing.IndexController;

/**
 * Test creating an index with facets
 * @author christopher
 *
 */
public class IndexingAndFacetTest {

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
			URL uri = IndexingAndFacetTest.class.getResource(name);
			
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
	
	private static RequestProcessor getSearchRequestProcessor() throws CRException {
		CRConfigFileLoader config = new CRConfigFileLoader("search",null);
		return config.getNewRequestProcessorInstance(1);
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
	 * Test creating an index.
	 */
	@Test
	public void testIndexing() {
		LuceneIndexLocation indexLoc = index();
		
		assertEquals("Doc count did not match.", 10, indexLoc.getDocCount());
		indexLoc.finalize();
	}
	
	/**
	 * Test creating an index and searching with facets
	 * @throws CRException in case of an error
	 */
	@Test
	public void testSearchWithFacets() throws CRException {
		LuceneIndexLocation indexLoc = index();
		RequestProcessor rp = getSearchRequestProcessor();
		CRRequest request = new CRRequest();
		request.set("metaresolvable", true);
		request.setRequestFilter("content:content");
		
		Collection<CRResolvableBean> ret = rp.getObjects(request);
		assertEquals("Result size did not match", 11, ret.size());
		
		CRResolvableBean metaresolvable = ret.iterator().next();
		
		assertEquals("Hits do not match", 10, metaresolvable.get("hits"));
		
		Map<String,Object> facets = (Map<String,Object>) metaresolvable.get("facetsList");
		assertNotNull("Facets was null", facets);
		Map<String,Object> facet0 = (Map<String,Object>) facets.get("0");
		assertEquals("Resultcount did not match", "10", facet0.get("count"));
		Map<String,Object> subnodes = (Map<String,Object>) facet0.get("subnodes");
		assertEquals("Expected count did not match",5,subnodes.get("cat1"));
		assertEquals("Expected count did not match",5,subnodes.get("cat2"));
		rp.finalize();
		indexLoc.finalize();
	}
	
	

}
