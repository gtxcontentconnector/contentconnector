package com.gentics.cr;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import com.gentics.api.lib.datasource.Datasource.Sorting;
import com.gentics.cr.exceptions.CRException;

public abstract class RequestProcessorTest {
	/** The Constant attributes. */
	protected static final String[] attributes = { "name", "folder_id", "node_id", "test1"};

	/** The Constant LOGGER. */
	protected final static Logger LOGGER = Logger.getLogger(CRRequestProcessorNavigationTest.class);

	/** The Constant FOLDER_TYPE. */
	protected static final String FOLDER_TYPE = CRResolvableBean.DEFAULT_DIR_TYPE;

	protected abstract RequestProcessor getRequestProcessor();
	
	protected abstract int getExpectedCollectionSize();
	
	protected abstract CRRequest getRequest();

	/** The test handler. */
	protected static HSQLCRTestHandler testHandler;

	/** The expected collection size. */
	protected static Integer expectedCollectionSize = 0;

	/** The root folder content id. */
	protected static String rootFolderContentId;



	@Test
	public void someTest() throws CRException {
		CRRequest request = getRequest();
		RequestProcessor processor = getRequestProcessor();
		Collection<CRResolvableBean> beans = processor.getObjects(request);
		int expectedSize = getExpectedCollectionSize();
		assertEquals(expectedSize, beans.size());
	}

	/**
	 * Gets the prepared Request for a Navigation Object building for any
	 * request processor.
	 * 
	 * @return the prepared Navigation Request
	 */
	protected CRRequest getNavigationRequest() {

		CRRequest req = new CRRequest();

		req.setRequestFilter("object.obj_type == " + FOLDER_TYPE + " AND object.folder_id == " + rootFolderContentId);
		req.setChildFilter("object.obj_type == " + FOLDER_TYPE);
		// sort by randomly added values
		req.setSorting(new Sorting[] {new Sorting("test2", 1)});
		req.setAttributeArray(attributes);

		return req;
	}

	/**
	 * creates and persists a folder with specified parent folder and name.
	 * 
	 * @param parentFolder
	 *            the mother folder
	 * @param name
	 *            the name for the new folder, should not be null
	 * @return the created and persisted folder
	 * @throws CRException
	 *             the cR exception
	 */
	protected static CRResolvableBean createTestFolder(
			CRResolvableBean parentFolder, String name) throws CRException {

		if (StringUtils.isEmpty(name)) {
			throw new IllegalArgumentException("no name specified for bean creation");
		}

		CRResolvableBean folder = new CRResolvableBean();
		folder.setObj_type(FOLDER_TYPE);
		folder.set("name", name);

		// randomly add some extra attributes
		if(RandomUtils.nextBoolean()) {
			folder.set("test1", RandomStringUtils.randomAlphabetic(8));
		}

		// randomly add some extra attributes
		if(RandomUtils.nextBoolean()) {
			folder.set("test2", RandomStringUtils.randomAlphabetic(8));
		}

		if (parentFolder != null) {
			// append the parent folder
			folder.set("folder_id", parentFolder.getContentid());
			// set node id same as parent
			folder.set("node_id", parentFolder.getInteger("node_id", 0));
		} else {
			// set random node id
			folder.set("node_id", RandomUtils.nextInt(99));
		}

		// persist the Bean
		folder = testHandler.createBean(folder, attributes);

		if (parentFolder != null) {
			parentFolder.getChildRepository().add(folder);
		}

		// increase size
		expectedCollectionSize++;

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("created folder bean " + folder.getContentid() + " with name: " + folder.get("name"));
		}

		return folder;
	}

	/**
	 * recursive method to create a tree.
	 * 
	 * @param active
	 *            is the active element, initially the root element
	 * @param vertical
	 *            specifies how many nodes should be appended to every node
	 * @param depth
	 *            specifies how deep the tree should be
	 * @throws CRException
	 *             the cR exception
	 *             {@link CRRequestProcessorTest#createTestNavigationData(Integer, Integer)}
	 */
	protected static void recursiveTreeCreation(CRResolvableBean active, int vertical, int depth) throws CRException {

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
	 * Prints a tree view of the CRResolvable, convenient for navigation
	 * objects.
	 * 
	 * @param resolvable
	 *            the resolvable
	 * @param depth
	 *            the depth
	 * @return String tree with children
	 */
	protected static String toStringWithChildren(CRResolvableBean resolvable, int depth) {

		StringBuilder sb = new StringBuilder();
		sb.append(getTabs(depth));
		sb.append("[" + resolvable.getContentid() + "]" + resolvable.getAttrMap() + "\n");
		sb.append(getTabs(depth));

		if (resolvable.getChildRepository() != null && resolvable.getChildRepository().size() > 0) {
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

	/**
	 * Gets tab amount for output.
	 * 
	 * @param amount
	 *            the amount
	 * @return the tabs
	 */
	protected static String getTabs(int amount) {

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < amount; i++) {
			sb.append("	");
		}
		return sb.toString();
	}
}
