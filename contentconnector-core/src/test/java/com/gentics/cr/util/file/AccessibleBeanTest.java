package com.gentics.cr.util.file;

import java.util.HashMap;

import com.gentics.cr.util.AccessibleBean;

import junit.framework.TestCase;

public class AccessibleBeanTest extends TestCase {

	private static HashMap<String, Object> attributes = new HashMap<String, Object>();
	static {
		attributes.put("byteArray", "abc".getBytes());
		attributes.put("string", "abc");
	}
	
	private AccessibleBean bean;
	
	@Override
	protected void setUp() throws Exception {
		bean = new MyAccessibleBean();
	}
	
	public void testGetString() {
		assertEquals("Cannot output byte array as string", "abc", bean.getString("byteArray"));
		assertEquals("Cannot output string as string", "abc", bean.getString("string"));
	}
	
	class MyAccessibleBean extends AccessibleBean {
		
		@Override
		public Object get(final String key) {
			return attributes.get(key);
		}
		
	}
}
