package com.gentics.cr;

import java.net.URISyntaxException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gentics.cr.exceptions.CRException;

public class CRRequestProcessorNavigationTest extends RequestProcessorTest{
	
	private final static Logger LOGGER = Logger.getLogger(CRRequestProcessorNavigationTest.class);

	private static CRRequestProcessor requestProcessor;
	
	private static HSQLTestHandler testHandler;

	@BeforeClass
	public static void setUp() throws CRException, URISyntaxException {
		CRConfigUtil config = HSQLTestConfigFactory.getDefaultHSQLConfiguration(CRRequestProcessorTest.class.getName());
		requestProcessor = new CRRequestProcessor(config.getRequestProcessorConfig(1));
		testHandler = new HSQLTestHandler(config.getRequestProcessorConfig(1));
		
		createTestNavigationData(5, 3);
	}

	@Override
	protected RequestProcessor getRequestProcessor() {
		return requestProcessor;
	}

	@AfterClass
	public static void tearDown() throws CRException {
		requestProcessor.finalize();
		testHandler.cleanUp();
	}

	@Override
	protected int getExpectedCollectionSize() {
		return 2;
	}

	@Override
	protected CRRequest getRequest() {
		CRRequest req = new CRRequest();
		req.setRequestFilter("object.obj_type == 10008");
		return req;
	}

	@Test
	public void testNavigationObject() throws CRException {
		// create some test data
		createTestNavigationData(5, 2);
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

		// create tree
		recursiveTreeCreation(rootFolder, vertical, depth);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("tree created with " + vertical + "vertical child nodes and depth of " + depth);
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
		if (depth == 0) {
			return;
		}

		// create vertical nodes for the given active node as mother
		for (int i = 0; i <= vertical; i++) {
			// create node and start the same method with depth -1 and new node
			// with mother
			recursiveTreeCreation(createTestFolder(active, "node_vertical_" + i + "_depth_" + depth + ".html"), i,
					depth - 1);
		}

	}

	/**
	 * 
	 * creates and persists a folder with specified parent folder and name
	 * 
	 * @param parentFolder
	 * @param name
	 * @return
	 * @throws CRException
	 */
	private static CRResolvableBean createTestFolder(CRResolvableBean parentFolder, String name) throws CRException {

		if (StringUtils.isEmpty(name)) {
			LOGGER.error("no name specified for bean creation");
			throw new IllegalArgumentException("no name specified for bean creation");
		}

		CRResolvableBean folder = new CRResolvableBean();
		folder.setObj_type("10008");
		folder.set("filename", name);

		if (parentFolder != null) {
			folder.setMother_id(parentFolder.getObj_id());
			folder.setMother_type(parentFolder.getObj_type());
		}

		testHandler.createBean(folder);

		return folder;
	}
}
