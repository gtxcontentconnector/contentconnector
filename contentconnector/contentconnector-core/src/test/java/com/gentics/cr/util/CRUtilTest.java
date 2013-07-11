package com.gentics.cr.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import com.gentics.cr.exceptions.CRException;
/**
 * Tests for the CRUtil Class
 * 
 * @author Sebastian Vogel <s.vogel@gentics.com>
 *
 */
public class CRUtilTest {
	
	@Test
	public void testClasspathResolving() throws CRException {
		String classpath = "classpath:com/gentics";
		System.setProperty(CRUtil.PORTALNODE_CONFPATH, classpath);
		CRUtil.resolveSystemProperties(CRUtil.PORTALNODE_CONFPATH);
		String newConfpath = System.getProperty(CRUtil.PORTALNODE_CONFPATH);
		assertNotSame("Resolving should change the system property.", classpath, newConfpath);
		assertTrue("The property should be resolved to an actual path in the filesystem.", (new File(newConfpath)).exists());
		CRUtil.resolveSystemProperties(CRUtil.PORTALNODE_CONFPATH);
		assertEquals("A second call to resolve should not change the confpath.", newConfpath, System.getProperty(CRUtil.PORTALNODE_CONFPATH));
	}
	
}
