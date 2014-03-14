package com.gentics.cr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.gentics.cr.exceptions.CRException;

public class ComparableBeanTest{
		
	@Test
	public void testSame() throws CRException {
		ComparableBean bean = new ComparableBean();
		bean.setContentid("10007.123");
				
		ComparableBean bean2 = new ComparableBean();
		bean2.setContentid("10007.123");
		
		assertEquals("Beans are not the same", bean, bean2);
	}
	
	@Test
	public void testDifferent() throws CRException {
		ComparableBean bean = new ComparableBean();
		bean.setContentid("10007.123");
				
		ComparableBean bean2 = new ComparableBean();
		bean2.setContentid("10007.1");
		
		assertTrue("Beans are the same", !bean.equals(bean2));
	}
	
}
