package com.gentics.cr.rendering.image.test;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.rendering.image.ImageMimetypeMapper;

public class ImageMimetypeMapperTest {

	@Before
	public void setUp() throws Exception {

	}

	/**
	 * Test for filename.
	 * @throws Exception
	 */
	@Test
	public void testTypePerFilename() throws Exception {

		CRResolvableBean bean = new CRResolvableBean();
		bean.set("filename", "my.new.testpic.jpg");
		String type = ImageMimetypeMapper.getTypeFromBean(bean);
		String shouldbe = "jpeg";
		assertEquals("The type (" + type + ") should be (" + shouldbe + ")", type, shouldbe);
	}

	/**
	 * Test for mimetype.
	 * @throws Exception
	 */
	@Test
	public void testTypePerMimetype() throws Exception {
		CRResolvableBean bean = new CRResolvableBean();
		bean.set("mimetype", "image/jpeg");
		String type = ImageMimetypeMapper.getTypeFromBean(bean);
		String shouldbe = "jpeg";
		assertEquals("The type (" + type + ") should be (" + shouldbe + ")", type, shouldbe);
	}
	
	/**
	 * Test for mimetype.
	 * @throws Exception
	 */
	@Test
	public void testTypePerStrangeIEMimetype() throws Exception {
		CRResolvableBean bean = new CRResolvableBean();
		bean.set("mimetype", "image/pjpeg");
		String type = ImageMimetypeMapper.getTypeFromBean(bean);
		String shouldbe = "jpeg";
		assertEquals("The type (" + type + ") should be (" + shouldbe + ")", type, shouldbe);
	}

	/**
	 * Test for default.
	 * @throws Exception
	 */
	@Test
	public void testTypePerDefault() throws Exception {
		CRResolvableBean bean = new CRResolvableBean();
		bean.set("mimetype", "somefrigginwrongthing");
		String type = ImageMimetypeMapper.getTypeFromBean(bean);
		String shouldbe = "png";
		assertEquals("The type (" + type + ") should be (" + shouldbe + ")", type, shouldbe);
	}

	@After
	public void tearDown() throws Exception {

	}
}
