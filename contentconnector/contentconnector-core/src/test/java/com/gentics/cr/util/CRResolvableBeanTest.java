package com.gentics.cr.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.gentics.cr.CRResolvableBean;

public class CRResolvableBeanTest {
	
	@Test
	public void testContentidConstructor() {
		CRResolvableBean bean = new CRResolvableBean("3.4");
		assertEquals("Contentid not correct.", "3.4", bean.getContentid());
		assertEquals("obj_id not correct.", "4", bean.getObj_id());
		assertEquals("obj_type not correct.", "3", bean.getObj_type());
	}
}
