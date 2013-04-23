package com.gentics.cr.rendering;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gentics.api.portalnode.connector.PLinkInformation;
import com.gentics.api.portalnode.connector.PLinkReplacer;
import com.gentics.cr.CRConfig;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.conf.gentics.ConfigDirectory;
import com.gentics.cr.exceptions.CRException;


public class ContentRendererTest {
	
	
	
	
	private CRConfig conf;
	private PLinkReplacer replacer = new PLinkReplacer() {

		public String replacePLink(PLinkInformation info) {
			return "link" + info.getContentId();
		}
	}; 
	
	@Before
	public void setUp() throws Exception {
		ConfigDirectory.useThis();
		conf = new CRConfigUtil();
	}

	private static final String CONTENT_1 = "This is a simple test. <plink id=\"10007.1\"> and more.";
	private static final String EXPECTED_1 = "This is a simple test. link10007.1 and more.";
	private static final String CONTENT_2 = "#set($t = 'more') This is a simple test. $t <plink id=\"10007.1\"> and more.";
	private static final String EXPECTED_2 = " This is a simple test. more link10007.1 and more.";
	
	/**
	 * Test for filename.
	 * @throws Exception
	 */
	@Test
	public void testContentRenderer() throws Exception {
		IContentRenderer renderer = new ContentRenderer(conf);
		String s1 = renderString(CONTENT_1, renderer, false);
		assertEquals("Testing ContentRenderer with PLink", EXPECTED_1, s1);
	}
	
	@Test
	public void testContentRendererVelocity() throws Exception {
		IContentRenderer renderer = new ContentRenderer(conf);
		String s1 = renderString(CONTENT_2, renderer, true);
		assertEquals("Testing ContentRenderer with PLink and velocity", EXPECTED_2 , s1);
	}
	
	@Test
	public void testFastContentRenderer() throws Exception {
		IContentRenderer renderer = new FastContentRenderer(conf);
		String s1 = renderString(CONTENT_1, renderer, false);
		assertEquals("Testing ContentRenderer with PLink", EXPECTED_1, s1);
	}
	
	@Test
	public void testFastContentRendererVelocity() throws Exception {
		IContentRenderer renderer = new FastContentRenderer(conf);
		String s1 = renderString(CONTENT_2, renderer, true);
		assertEquals("Testing ContentRenderer with PLink and velocity", EXPECTED_2 , s1);
	}
	
	private String renderString(String content, IContentRenderer renderer, boolean doVelocity) throws CRException, IOException {
		CRResolvableBean bean = new CRResolvableBean();
		bean.set("content", content);
		return renderer.renderContent(bean, "content", true, replacer, doVelocity, null);
	}

	@After
	public void tearDown() throws Exception {

	}
}
