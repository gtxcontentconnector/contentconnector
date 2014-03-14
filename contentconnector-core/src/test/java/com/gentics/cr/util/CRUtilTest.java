package com.gentics.cr.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.junit.Test;

import com.gentics.api.lib.datasource.Datasource.Sorting;
import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.cr.CRResolvableBean;
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
	
	
	
	@Test
	public void testsortCollection() {
		
		List<CRResolvableBean> beans = new Vector<CRResolvableBean>();
		beans.add(createBean("10001.1", "falcon.jpg", "animals"));
		beans.add(createBean("10001.2", "ford.jpg", "cars"));
		beans.add(createBean("10001.3", "eagle.jpg", "animals"));
		beans.add(createBean("10001.4", "tree.jpg", "plants"));
		beans.add(createBean("10001.5", "bird.jpg", "animals"));
		beans.add(createBean("10001.6", "honda.jpg", "cars"));
		beans.add(createBean("10001.7", "flower.jpg", "plants"));
		beans.add(createBean("10001.8", "saab.jpg", "cars"));
		
		Sorting[] sorting = CRUtil.convertSorting(new String[]{"category:asc", "filename:asc"});
		CRUtil.sortCollection(beans, sorting);
		assertTrue("Collection is not properly sorted.", isSorted(beans, new String[]{"10001.5", "10001.3", "10001.1", "10001.2", "10001.6", "10001.8", "10001.7", "10001.4"}));
		
	}
	
	private boolean isSorted(Collection<? extends Resolvable> coll, String[] expectedcontentids) {
		@SuppressWarnings("unchecked")
		Iterator<Resolvable> collIterator = (Iterator<Resolvable>) coll.iterator();
		for (int i = 0; i < coll.size(); i++) {
			Resolvable reso = collIterator.next();
			String cId = (String) reso.get("contentid");
			if (!cId.equalsIgnoreCase(expectedcontentids[i])) {
				return false;
			}
		}
		return true;
	}
		
	private CRResolvableBean createBean(String contentid, String filename, String catetory) {
		CRResolvableBean bean = new CRResolvableBean(contentid);
		bean.set("filename", filename);
		bean.set("category", catetory);
		return bean;
	}
	
}
