package com.gentics.cr.cache.test;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
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
import com.gentics.cr.configuration.EnvironmentConfiguration;
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
		NavigationCache.MIN_SCHEDULE_TIME = 1;
		URL restUrl = NavigationCacheTest.class.getResource("rest/navigationcache.properties");
		assertNotNull("Could not find resource 'rest'", restUrl);
		CRConfigUtil config = HSQLTestConfigFactory.getDefaultHSQLConfiguration(NavigationCacheTest.class
				.getName());

		File restFolder = new File(EnvironmentConfiguration.getConfigPath(), "rest");
		restFolder.mkdirs();
		try (InputStream in = NavigationCacheTest.class.getResourceAsStream("rest/navigationcache.properties")) {
			Files.copy(in, new File(restFolder, "navigationcache.properties").toPath());
		}

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
	 * @throws InterruptedException 
	 */
	@Test
	public void testNavigationCache() throws CRException, InterruptedException {

		// Check that the original navigation object is not empty
		Assert.assertFalse(CollectionUtils.isEmpty(originalNavigationObject));

		CRRequest crRequest = getNavigationRequest();

		assertNull("There should not be something in the cache", NavigationCache.get().getCachedNavigationObject(requestProcessor, crRequest));

		Collection<CRResolvableBean> navigationObject = NavigationCache.get().fetchAndCacheNavigationObject(requestProcessor, crRequest);
		assertNotNull("Fetching the navigation must return something", navigationObject);

		Collection<CRResolvableBean> cachedNavigationObject = NavigationCache.get().getCachedNavigationObject(requestProcessor, crRequest);
		assertNotNull("There should be something in the cache", cachedNavigationObject);
		assertTrue("The cached object must be identical to the fetched object", navigationObject == cachedNavigationObject);

		assertNull("Cache for other navigation request should be empty", NavigationCache.get().getCachedNavigationObject(requestProcessor, getOtherNavigationRequest()));

		// wait until the cache is automatically refreshed, then get the cached object again (must be another object)
		Thread.sleep(1100);
		Collection<CRResolvableBean> refreshedNavigationObject = NavigationCache.get().getCachedNavigationObject(requestProcessor, crRequest);
		assertNotNull("There should be something in the cache", refreshedNavigationObject);
		assertFalse("The cached object must be identical to the fetched object", navigationObject == refreshedNavigationObject);
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
