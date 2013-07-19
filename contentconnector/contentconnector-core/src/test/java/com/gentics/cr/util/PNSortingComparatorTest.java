package com.gentics.cr.util;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import com.gentics.api.lib.datasource.Datasource.Sorting;
import com.gentics.api.lib.resolving.Resolvable;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.exceptions.CRException;

public class PNSortingComparatorTest {
	
	List<CRResolvableBean> collection = null;
	
	@Before
	public void init() {
		collection = new Vector<CRResolvableBean>();
		
		
		CRResolvableBean bean1 = new CRResolvableBean("10007.1");
		bean1.set("filename", "A");
		collection.add(bean1);
		
		CRResolvableBean bean2 = new CRResolvableBean("10007.2");
		bean2.set("filename", "Z");
		collection.add(bean2);
		
		CRResolvableBean bean3 = new CRResolvableBean("10007.3");
		bean3.set("filename", "C");
		collection.add(bean3);
		
		
	}
	
	
	@Test
	public void testWithStringConstructor() throws CRException {
		CRRequest req = new CRRequest();
		req.setSortArray(new String[]{"filename:desc"});
		Sorting[] sorting = req.getSorting();
		Collections.sort(collection, new PNSortingComparator(sorting[0].getColumnName(), sorting[0].getSortOrder()));
		assertEquals("Collection is not sorted (filename:desc)", true, isSorted(collection, new String[]{"10007.2", "10007.3", "10007.1"}));
		
	}
	
	@Test
	public void testWithSortingConstructor() throws CRException {
		CRRequest req = new CRRequest();
		
		req.setSortArray(new String[]{"filename:asc"});
		Sorting[] sorting = req.getSorting();
		Collections.sort(collection, new PNSortingComparator(sorting[0]));
		assertEquals("Collection is not sorted (filename:desc)", true, isSorted(collection, new String[]{"10007.1", "10007.3", "10007.2"}));
		
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
	
	
}
