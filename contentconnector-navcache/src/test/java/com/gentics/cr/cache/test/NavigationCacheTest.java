package com.gentics.cr.cache.test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.HSQLCRTestHandler;
import com.gentics.cr.HSQLTestConfigFactory;
import com.gentics.cr.OptimisticNavigationRequestProcessor;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.RequestProcessorTest;
import com.gentics.cr.cache.NavigationCache;
import com.gentics.cr.cache.NavigationCacheRequestProcessor;
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
	private static NavigationCacheRequestProcessor requestProcessor;

	/**
	 * Configuration for the navigation cache request processor
	 */
	private static CRConfig navConfig;

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
		CRConfigUtil wrappedConfig = HSQLTestConfigFactory.getDefaultHSQLConfiguration(NavigationCacheTest.class
				.getName());

		// enable node id feature
		wrappedConfig.set("RP.1.usenodeidinchildrule", "true");

		CRConfigUtil config = new CRConfigUtil();
		CRConfigUtil intermediate = new CRConfigUtil();
		config.setSubConfig("rp", intermediate);
		intermediate.setSubConfig("1", wrappedConfig);
		config.set("rp.1.threads", "2");
		config.set("rp.1.secondsbeforecache", "3599");
		config.set("rp.1.minscheduletime", "1");

		navConfig = config.getRequestProcessorConfig(1);

		requestProcessor = new NavigationCacheRequestProcessor(navConfig);

		testHandler = new HSQLCRTestHandler(wrappedConfig.getRequestProcessorConfig(1));

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

		NavigationCache navCache = requestProcessor.getNavigationCache();

		assertNull("There should not be something in the cache", navCache.getCachedNavigationObject(crRequest));

		Collection<CRResolvableBean> navigationObject = navCache.fetchAndCacheNavigationObject(crRequest);
		assertNotNull("Fetching the navigation must return something", navigationObject);

		Collection<CRResolvableBean> cachedNavigationObject = navCache.getCachedNavigationObject(crRequest);
		assertNotNull("There should be something in the cache", cachedNavigationObject);
		assertTrue("The cached object must be identical to the fetched object", navigationObject == cachedNavigationObject);

		assertNull("Cache for other navigation request should be empty", navCache.getCachedNavigationObject(getOtherNavigationRequest()));

		// wait until the cache is automatically refreshed, then get the cached object again (must be another object)
		Thread.sleep(2000);
		Collection<CRResolvableBean> refreshedNavigationObject = navCache.getCachedNavigationObject(crRequest);
		assertNotNull("There should be something in the cache", refreshedNavigationObject);
		assertFalse("The cached object must not be identical to the fetched object", navigationObject == refreshedNavigationObject);
	}

	/**
	 * Test navigation cache keys
	 */
	@Test
	public void testNavigationCacheKey() {
		NavigationCache navCache = requestProcessor.getNavigationCache();
		assertEquals("Check cache keys for equal CRRequests", navCache.getCacheKey(getNavigationRequest()), navCache.getCacheKey(getNavigationRequest()));
		assertFalse("Check cache keys for different CRRequests",
				navCache.getCacheKey(getNavigationRequest()).equals(navCache.getCacheKey(getOtherNavigationRequest())));
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
