package com.gentics.cr.util.file;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.gentics.cr.util.AccessibleBean;

public class AccessibleBeanTest {

	private static HashMap<String, Object> attributes = new HashMap<String, Object>();
	static {
		attributes.put("byteArray", "abc".getBytes());
		attributes.put("string", "abc");
	}

	private AccessibleBean bean;

	@Before
	public void setUp() throws Exception {
		bean = new MyAccessibleBean();
	}

	@Test
	public void testGetString() {
		assertEquals("Cannot output byte array as string", "abc", bean.getString("byteArray"));
		assertEquals("Cannot output string as string", "abc", bean.getString("string"));
		assertEquals("Cannot output string as string", "abc", bean.getString("notfoundattribute", "abc"));
	}

	class MyAccessibleBean extends AccessibleBean {

		@Override
		public Object get(final String key) {
			return attributes.get(key);
		}

	}
}
