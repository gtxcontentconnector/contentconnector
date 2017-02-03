package com.gentics.cr;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gentics.api.lib.datasource.Datasource.Sorting;
import com.gentics.cr.exceptions.CRException;

/**
 * This test checks the functionality of building of navigation objects in the.
 * 
 * {@link OptimisticNavigationRequestProcessor} Request Processor.
 * 
 * @author l.osang@gentics.com
 */

public class CRRequestProcessorNavigationTest extends RequestProcessorTest {
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
		CRConfigUtil config = HSQLTestConfigFactory.getDefaultHSQLConfiguration(CRRequestProcessorNavigationTest.class
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
	 * Test optimistic navigation building.
	 * 
	 * @throws CRException
	 *             the cR exception
	 */
	@Test
	public void testOptimisticNavigationBuilding() throws CRException {

		// check that the original navigation object is not empty
		Assert.assertFalse(CollectionUtils.isEmpty(originalNavigationObject));

		// do a request with the navigation request and the optimistic request
		// processor
		Collection<CRResolvableBean> result = navigationRequestProcessor.getNavigation(getNavigationRequest());

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("size of constructed object: " + result.size());

			for (CRResolvableBean crResolvableBean : result) {
				LOGGER.debug(toStringWithChildren(crResolvableBean, 0));
			}
		}

		Assert.assertTrue(compareResolvableChildren(originalNavigationObject, result));
	}

	/**
	 * Test optimistic navigation building. Without child filter
	 * 
	 * @throws CRException
	 *             the cR exception
	 */
	@Test
	public void testOptimisticNavigationBuildingWithoutChildfilter() throws CRException {

		// check that the original navigation object is not empty
		Assert.assertFalse(CollectionUtils.isEmpty(originalNavigationObject));

		// do a request with the navigation request and the optimistic request
		// processor
		Collection<CRResolvableBean> result = navigationRequestProcessor.getNavigation(getNavigationRequestWithoutChildfilter());

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("size of constructed object: " + result.size());

			for (CRResolvableBean crResolvableBean : result) {
				LOGGER.debug(toStringWithChildren(crResolvableBean, 0));
			}
		}

		Assert.assertTrue(compareResolvableChildren(originalNavigationObject, result));
	}
	
	
	
	/**
	 * Test normal navigation building.
	 * 
	 * @throws CRException
	 *             the cR exception
	 */
	@Test
	public void testNavigationBuilding() throws CRException {

		// check that the original navigation object is not empty
		Assert.assertFalse(CollectionUtils.isEmpty(originalNavigationObject));

		// do a request with the navigation request and the normal request
		// processor
		Collection<CRResolvableBean> result = requestProcessor.getNavigation(getNavigationRequest());

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("size of constructed object: " + result.size());
			for (CRResolvableBean crResolvableBean : result) {
				LOGGER.debug(toStringWithChildren(crResolvableBean, 0));
			}
		}

		Assert.assertTrue(compareResolvableChildren(originalNavigationObject, result));
	}
	
	/**
	 * Gets the prepared Request for a Navigation Object building for any
	 * request processor.
	 * 
	 * @return the prepared Navigation Request
	 */
	private CRRequest getNavigationRequestWithoutChildfilter() {

		CRRequest req = new CRRequest();

		req.setRequestFilter("object.obj_type == " + FOLDER_TYPE + " AND object.folder_id == " + rootFolderContentId);
		// sort by randomly added values
		req.setSorting(new Sorting[] {new Sorting("test2", 1)});
		req.setAttributeArray(attributes);

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

	/**
	 * Compares two collections of CRResolvableBeans against contentid and
	 * childrepositories.
	 * 
	 * ATTENTION: not very fast! Should only be used for testing!
	 * 
	 * @param origTree
	 * @param fetchedTree
	 * @return true if the two collections are equal
	 */
	private static boolean compareResolvableChildren(final Collection<CRResolvableBean> origTree,
			Collection<CRResolvableBean> fetchedTree) {
		Boolean result = true;

		Set<CRResolvableBean> matchesFoundOrig = new HashSet<CRResolvableBean>();
		Set<CRResolvableBean> matchesFoundFetched = new HashSet<CRResolvableBean>();

		for (CRResolvableBean crResolvableBean : origTree) {

			for (CRResolvableBean crResolvableBeanFetched : fetchedTree) {

				// check if we got a match and that match
				if (crResolvableBean.getContentid().equals(crResolvableBeanFetched.getContentid())
						&& !matchesFoundOrig.contains(crResolvableBean)) {

					result = compareResolvableChildren(crResolvableBean.getChildRepository(),
							crResolvableBeanFetched.getChildRepository());

					if (result == false) {
						break;
					}

					matchesFoundFetched.add(crResolvableBeanFetched);
					// store which matches we already found
					matchesFoundOrig.add(crResolvableBean);

				}
			}

			if (result == false) {
				break;
			}
		}

		// return false if we didn't find all original entries
		if (matchesFoundOrig.size() != origTree.size()) {
			result = false;
		}

		// return false if we didn't got all fetched entries in the original
		// tree
		if (matchesFoundFetched.size() != fetchedTree.size()) {
			result = false;
		}

		return result;
	}
}
