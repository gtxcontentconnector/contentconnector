package com.gentics.cr.cache.test;


import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRRequestProcessor;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.HSQLCRTestHandler;
import com.gentics.cr.HSQLTestConfigFactory;
import com.gentics.cr.OptimisticNavigationRequestProcessor;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.RequestProcessorTest;
import com.gentics.cr.cache.NavigationCache;
import com.gentics.cr.exceptions.CRException;

/**
 * This test checks the functionality of building of navigation objects in the.
 * 
 * {@link OptimisticNavigationRequestProcessor} Request Processor.
 * 
 * @author l.osang@gentics.com
 */

public class NavigationCacheTest extends RequestProcessorTest {
	/** The request processor. */
	private static CRRequestProcessor requestProcessor;

	/** The navigation request processor. */
	private static OptimisticNavigationRequestProcessor navigationRequestProcessor;

	/** The original navigation object. */
	private static Collection<CRResolvableBean> originalNavigationObject;



	/**
	 * Setup the request processors and the hsql database handler
	 * 
	 * @throws CRException
	 *             the cR exception
	 * @throws URISyntaxException
	 *             the uRI syntax exception
	 */
	@BeforeClass
	public static void setUp() throws CRException, URISyntaxException, IOException {
		CRConfigUtil config = HSQLTestConfigFactory.getDefaultHSQLConfiguration(NavigationCacheTest.class
				.getName());
		
		// enable node id feature
		config.set("RP.1.usenodeidinchildrule", "true");

		requestProcessor = new CRRequestProcessor(config.getRequestProcessorConfig(1));
		navigationRequestProcessor = new OptimisticNavigationRequestProcessor(config.getRequestProcessorConfig(1));

		testHandler = new HSQLCRTestHandler(config.getRequestProcessorConfig(1));

		// a folder structure
		createTestNavigationData(2, 2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gentics.cr.RequestProcessorTest#getRequestProcessor()
	 */
	@Override
	protected RequestProcessor getRequestProcessor() {
		return requestProcessor;
	}

	/**
	 * Tear down and clean up.
	 * 
	 * @throws CRException
	 *             the cR exception
	 */
	@AfterClass
	public static void tearDown() throws CRException {
		requestProcessor.finalize();
		navigationRequestProcessor.finalize();
		testHandler.cleanUp();
	}

	/**
	 * Test normal navigation building.
	 * 
	 * @throws CRException
	 *             the cR exception
	 */
	@Test
	public void testNavigationCache() throws CRException {

		// Check that the original navigation object is not empty
		Assert.assertFalse(CollectionUtils.isEmpty(originalNavigationObject));

		CRRequest crRequest = getNavigationRequest();

		CRResolvableBean crBean = null;

		crBean = NavigationCache.get().getCachedNavigationObject(
				"root.html", "object.obj_type==10002 AND object.navhidden != 1", requestProcessor);

		Assert.assertNull("There should not be something in the cache", crBean);

		NavigationCache.get().fetchAndCacheNavigationObject(
				"root.html", "object.obj_type==10002 AND object.navhidden != 1", requestProcessor, crRequest);

		crBean = NavigationCache.get().getCachedNavigationObject(
				"root.html", "object.obj_type==10002 AND object.navhidden != 1", requestProcessor);

		Assert.assertNotNull("There should be something in the cache", crBean);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gentics.cr.RequestProcessorTest#getExpectedCollectionSize()
	 */
	@Override
	protected int getExpectedCollectionSize() {
		return expectedCollectionSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gentics.cr.RequestProcessorTest#getRequest()
	 */
	@Override
	protected CRRequest getRequest() {
		CRRequest req = new CRRequest();
		req.setRequestFilter("object.obj_type == " + FOLDER_TYPE);
		return req;
	}

	/**
	 * Creates the test navigation data.
	 * 
	 * @param vertical
	 *            specifies how many nodes should be appended to every node
	 * @param depth
	 *            specifies how deep the tree should be
	 * @throws CRException
	 *             the cR exception
	 */
	private static void createTestNavigationData(Integer vertical, Integer depth) throws CRException {
		// check if all necessary object are present
		Assert.assertNotNull(requestProcessor);
		Assert.assertNotNull(testHandler);
		Assert.assertNotNull(vertical);
		Assert.assertNotNull(depth);

		// the root folder
		CRResolvableBean rootFolder = createTestFolder(null, "root.html");

		// save the root element contentid
		rootFolderContentId = rootFolder.getContentid();

		// create tree
		recursiveTreeCreation(rootFolder, vertical, depth);

		// save the original navigation object
		originalNavigationObject = rootFolder.getChildRepository();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("tree created with " + vertical + " vertical child nodes and depth of " + depth);
			LOGGER.debug("Inserted folders amount: " + expectedCollectionSize);
			LOGGER.debug(toStringWithChildren(rootFolder, 0));
		}
	}
}
