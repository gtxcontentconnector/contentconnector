package com.gentics.cr.util;

import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.exceptions.CRException;

public class AccessibleBeanTest {
	
	CRResolvableBean bean = new CRResolvableBean();
	
	@Before
	public void init() {
		bean.set("int", new Integer(10));
		bean.set("bigdecimal", new BigDecimal(10));
		bean.set("float", new Float(3.4f));
		bean.set("String", "10");
	}
	
	
	@Test
	public void testInteger() throws CRException {
		Object o1 = bean.getInteger("int", 0);
		testInteger(o1, 10);
		
		Object o2 = bean.getInteger("bigdecimal", 0);
		testInteger(o2, 10);
		
		Object o3 = bean.getInteger("String", 0);
		testInteger(o3, 10);
		
		Object o4 = bean.getInteger("null", 0);
		testInteger(o4, 0);
		
		Object o5 = bean.getInteger("float", 0);
		testInteger(o5, 3);
	}
	
	private void testInteger(Object o, int val) {
		assertTrue("Object is not an Integer.", o instanceof Integer);
		assertEquals("Object does not have the right value", 
				((Integer) o).intValue(), val);
	}
	
	@Test
	public void testLong() throws CRException {
		Object o1 = bean.getLong("int", 0);
		testLong(o1, 10);
		
		Object o2 = bean.getLong("bigdecimal", 0);
		testLong(o2, 10);
		
		Object o3 = bean.getLong("String", 0);
		testLong(o3, 10);
		
		Object o4 = bean.getLong("null", 0);
		testLong(o4, 0);
		
		Object o5 = bean.getLong("float", 0);
		testLong(o5, 3);
	}
	
	private void testLong(Object o, long val) {
		assertTrue("Object is not an Integer.", o instanceof Long);
		assertEquals("Object does not have the right value", 
				((Long) o).longValue(), val);
	}
	
	@Test
	public void testFloat() throws CRException {
		Object o1 = bean.getFloat("int", 0);
		testFloat(o1, 10);
		
		Object o2 = bean.getFloat("bigdecimal", 0);
		testFloat(o2, 10);
		
		Object o3 = bean.getFloat("String", 0);
		testFloat(o3, 10);
		
		Object o4 = bean.getFloat("null", 0);
		testFloat(o4, 0);
		
		Object o5 = bean.getFloat("float", 0);
		testFloat(o5, 3.4f);
	}
	
	private void testFloat(Object o, float val) {
		assertTrue("Object is not an Integer.", o instanceof Float);
		assertEquals("Object does not have the right value", 
				((Float) o).floatValue(), val, 0);
	}

}
