package com.gentics.cr;

import java.net.URISyntaxException;
import java.util.Collection;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gentics.cr.exceptions.CRException;

/**
 * This test checks the functionality of building of navigation objects in the
 * {@link OptimisticNavigationRequestProcessor} Request Processor.
 * 
 * @author l.osang@gentics.com
 * 
 */

public class CRRequestProcessorNavigationTest extends RequestProcessorTest {

	private final static Logger LOGGER = Logger.getLogger(CRRequestProcessorNavigationTest.class);

	private static final String[] attributes = { "name", "folder_id" };

	private static final String FOLDER_TYPE = CRResolvableBean.DEFAULT_DIR_TYPE;

	private static CRRequestProcessor requestProcessor;

	private static HSQLTestHandler testHandler;

	private static OptimisticNavigationRequestProcessor navigationRequestProcessor;

	private static Integer expectedCollectionSize = 0;

	private static String rootFolderContentId;

	private static Collection<CRResolvableBean> originalNavigationObject;

	@BeforeClass
	public static void setUp() throws CRException, URISyntaxException {
		CRConfigUtil config = HSQLTestConfigFactory.getDefaultHSQLConfiguration(CRRequestProcessorTest.class.getName());

		requestProcessor = new CRRequestProcessor(config.getRequestProcessorConfig(1));
		navigationRequestProcessor = new OptimisticNavigationRequestProcessor(config.getRequestProcessorConfig(1));

		testHandler = new HSQLTestHandler(config.getRequestProcessorConfig(1));

		// a folder structure
		createTestNavigationData(2, 3);
	}

	@Override
	protected RequestProcessor getRequestProcessor() {
		return requestProcessor;
	}

	@AfterClass
	public static void tearDown() throws CRException {
		requestProcessor.finalize();
		navigationRequestProcessor.finalize();
		testHandler.cleanUp();
	}

	@Override
	protected int getExpectedCollectionSize() {
		return expectedCollectionSize;
	}

	@Override
	protected CRRequest getRequest() {
		CRRequest req = new CRRequest();
		req.setRequestFilter("object.obj_type == " + FOLDER_TYPE);
		return req;
	}

	@Test
	public void testOptimisticNavigationBuilding() throws CRException {

		Collection<CRResolvableBean> result = navigationRequestProcessor.getNavigation(getNavigationRequest());

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("size of constructed object: " + result.size());

			for (CRResolvableBean crResolvableBean : result) {
				LOGGER.debug(toStringWithChildren(crResolvableBean, 0));
			}
		}
		
		Assert.assertTrue(originalNavigationObject.equals(result));
	}

	@Test
	public void testNavigationObject() throws CRException {

		Collection<CRResolvableBean> result = requestProcessor.getNavigation(getNavigationRequest());

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("size of constructed object: " + result.size());
			for (CRResolvableBean crResolvableBean : result) {
				LOGGER.debug(toStringWithChildren(crResolvableBean, 0));
			}
		}
		
		Assert.assertTrue(originalNavigationObject.equals(result));
	}

	/**
	 * Gets the prepared Request for a Navigation Object building for any
	 * request processor
	 * 
	 * @return the prepared Navigation Request
	 */
	private CRRequest getNavigationRequest() {

		CRRequest req = new CRRequest();

		req.setRequestFilter("object.obj_type == " + FOLDER_TYPE + " AND object.folder_id == " + rootFolderContentId);
		req.setChildFilter("object.obj_type == " + FOLDER_TYPE);
		req.setAttributeArray(attributes);

		return req;
	}

	/**
	 * 
	 * @param vertical
	 *            specifies how many nodes should be appended to every node
	 * @param depth
	 *            specifies how deep the tree should be
	 * @throws CRException
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
	 * recursive method to create a tree, should only be called by
	 * {@link CRRequestProcessorTest#createTestNavigationData(Integer, Integer)}
	 * 
	 * @param active
	 *            is the active element, initially the root element
	 * @param vertical
	 *            specifies how many nodes should be appended to every node
	 * @param depth
	 *            specifies how deep the tree should be
	 * @throws CRException
	 */
	private static void recursiveTreeCreation(CRResolvableBean active, int vertical, int depth) throws CRException {

		// recursive break condition
		if (depth <= 0) {
			return;
		}

		String newName;

		// create vertical nodes for the given active node as mother
		for (int i = 0; i < vertical; i++) {
			// the new test folder name
			newName = "node_vertical_" + i + "_depth_" + depth + ".html";

			// create node and start the same method with depth -1 and new node
			// with parent tree node
			recursiveTreeCreation(createTestFolder(active, newName), vertical, depth - 1);
		}
	}

	/**
	 * 
	 * creates and persists a folder with specified parent folder and name
	 * 
	 * @param parentFolder
	 *            the mother folder
	 * @param name
	 *            the name for the new folder, should not be null
	 * @return the created and persisted folder
	 * @throws CRException
	 */
	private static CRResolvableBean createTestFolder(CRResolvableBean parentFolder, String name) throws CRException {

		if (StringUtils.isEmpty(name)) {
			throw new IllegalArgumentException("no name specified for bean creation");
		}

		CRResolvableBean folder = new CRResolvableBean();
		folder.setObj_type(FOLDER_TYPE);
		folder.set("name", name);

		if (parentFolder != null) {
			// append the parent folder
			folder.set("folder_id", parentFolder.getContentid());
		}

		// persist the Bean
		folder = testHandler.createBean(folder, attributes);

		// increase size
		expectedCollectionSize++;

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("created folder bean " + folder.getContentid() + " with name: " + folder.get("name"));
		}

		return folder;
	}

	/**
	 * Prints a tree view of the CRResolvable, convenient for navigation objects
	 * 
	 * @return String tree with children
	 */
	private static String toStringWithChildren(CRResolvableBean resolvable, int depth) {

		StringBuilder sb = new StringBuilder();
		sb.append(getTabs(depth));
		sb.append("[" + resolvable.getContentid() + "]" + "\n");
		sb.append(getTabs(depth));

		if (resolvable.getChildRepository().size() > 0) {
			sb.append("children: { ");

			for (CRResolvableBean child : resolvable.getChildRepository()) {
				sb.append("\n");
				sb.append(getTabs(depth));
				sb.append(toStringWithChildren(child, depth + 1));
			}
			sb.append(getTabs(depth));
			sb.append("}\n");
		}

		return sb.toString();
	}
	
	private static String getTabs(int amount) {
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < amount; i++) {
			sb.append("	");
		}
		return sb.toString();
	}
}
