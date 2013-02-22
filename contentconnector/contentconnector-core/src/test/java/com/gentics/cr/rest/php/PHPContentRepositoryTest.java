package com.gentics.cr.rest.php;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.exceptions.CRException;

public class PHPContentRepositoryTest {

	Collection<CRResolvableBean> beanCollection = new Vector<CRResolvableBean>();
	private static final String UPDATE_TIMESTAMP_KEY = "updatetimestamp";

	@Before
	public void setUp() throws CRException {
		
		CRResolvableBean mr = new CRResolvableBean("10001.1");
		mr.set(PHPContentRepositoryTest.UPDATE_TIMESTAMP_KEY, new Long(111111111L));
		Map<String, String[]> suggestions = new HashMap<String, String[]>();
		suggestions.put("tst", new String[]{"test", "tess"});
		mr.set("suggestions", suggestions);
		beanCollection.add(mr);
		
		CRResolvableBean b1 = new CRResolvableBean("10002.1");
		b1.set(PHPContentRepositoryTest.UPDATE_TIMESTAMP_KEY, new Long(111111111L));
		beanCollection.add(b1);

		CRResolvableBean b2 = new CRResolvableBean("10002.2");
		b2.set(PHPContentRepositoryTest.UPDATE_TIMESTAMP_KEY, new Long(111211111L));
		beanCollection.add(b2);

		CRResolvableBean b3 = new CRResolvableBean("10002.3");
		b3.set(PHPContentRepositoryTest.UPDATE_TIMESTAMP_KEY, new Long(131111011L));
		beanCollection.add(b3);

		CRResolvableBean b4 = new CRResolvableBean("10002.4");
		b4.set(PHPContentRepositoryTest.UPDATE_TIMESTAMP_KEY, new Long(111111011L));
		beanCollection.add(b4);

		Collection<CRResolvableBean> children = new Vector<CRResolvableBean>();
		CRResolvableBean c1 = new CRResolvableBean("10002.5");
		c1.set(PHPContentRepositoryTest.UPDATE_TIMESTAMP_KEY, new Long(141111011L));
		children.add(c1);

		CRResolvableBean c2 = new CRResolvableBean("10002.6");
		c2.set(PHPContentRepositoryTest.UPDATE_TIMESTAMP_KEY, new Long(151111011L));
		c2.set("permissions", new String[] { "mar, sal, soc" });
		children.add(c2);
		b3.setChildRepository(children);
	}
	@Test
	public void phpTransformerTest() throws CRException, UnsupportedEncodingException, BadFormatException {
		PHPContentRepository cr = new PHPContentRepository(new String[] { "updatetimestamp" });
		cr.addObjects(beanCollection);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		cr.toStream(stream);
		String s = stream.toString("utf-8");
		
		PHPSerializer serializer = new PHPSerializer("utf-8");
		Map<Object, Object> map = (Map<Object, Object>) serializer.unserialize(s);
		assertEquals(resolvePath(map, "status"), "ok");
		
		assertEquals(2, ((Map<Object, Object>) resolvePath(map, "'10001.1'.attributes.suggestions.tst")).size());
		
		assertEquals("10002.1", resolvePath(map, "'10002.1'.contentid"));
		assertEquals("1", resolvePath(map, "'10002.1'.obj_id"));
		assertEquals("10002", resolvePath(map, "'10002.1'.obj_type"));
		assertEquals("", resolvePath(map, "'10002.1'.mother_id"));
		assertEquals("", resolvePath(map, "'10002.1'.mother_type"));
		assertEquals( "111111111", resolvePath(map, "'10002.1'.attributes.updatetimestamp"));
		assertEquals(1, ((Map<Object, Object>) resolvePath(map, "'10002.1'.attributes")).size());
		assertEquals(6, ((Map<Object, Object>) resolvePath(map, "'10002.1'")).size());
		
		assertEquals("10002.2", resolvePath(map, "'10002.2'.contentid"));
		assertEquals("2", resolvePath(map, "'10002.2'.obj_id"));
		assertEquals("10002", resolvePath(map, "'10002.2'.obj_type"));
		assertEquals("", resolvePath(map, "'10002.2'.mother_id"));
		assertEquals("", resolvePath(map, "'10002.2'.mother_type"));
		assertEquals( "111211111", resolvePath(map, "'10002.2'.attributes.updatetimestamp"));
		assertEquals(1, ((Map<Object, Object>) resolvePath(map, "'10002.2'.attributes")).size());
		assertEquals(6, ((Map<Object, Object>) resolvePath(map, "'10002.2'")).size());
		
		assertEquals("10002.3", resolvePath(map, "'10002.3'.contentid"));
		assertEquals("3", resolvePath(map, "'10002.3'.obj_id"));
		assertEquals("10002", resolvePath(map, "'10002.3'.obj_type"));
		assertEquals("", resolvePath(map, "'10002.3'.mother_id"));
		assertEquals("", resolvePath(map, "'10002.3'.mother_type"));
		assertEquals("131111011", resolvePath(map, "'10002.3'.attributes.updatetimestamp"));
		assertEquals(1, ((Map<Object, Object>) resolvePath(map, "'10002.3'.attributes")).size());
		
		
		
		assertEquals("10002.5", resolvePath(map, "'10002.3'.children.'10002.5'.contentid"));
		assertEquals("5", resolvePath(map, "'10002.3'.children.'10002.5'.obj_id"));
		assertEquals("10002", resolvePath(map, "'10002.3'.children.'10002.5'.obj_type"));
		assertEquals("", resolvePath(map, "'10002.3'.children.'10002.5'.mother_id"));
		assertEquals("", resolvePath(map, "'10002.3'.children.'10002.5'.mother_type"));
		assertEquals("141111011", resolvePath(map, "'10002.3'.children.'10002.5'.attributes.updatetimestamp"));
		assertEquals(1, ((Map<Object, Object>) resolvePath(map, "'10002.3'.children.'10002.5'.attributes")).size());
		assertEquals(6, ((Map<Object, Object>) resolvePath(map, "'10002.3'.children.'10002.5'")).size());

		assertEquals("10002.6", resolvePath(map, "'10002.3'.children.'10002.6'.contentid"));
		assertEquals("6", resolvePath(map, "'10002.3'.children.'10002.6'.obj_id"));
		assertEquals("10002", resolvePath(map, "'10002.3'.children.'10002.6'.obj_type"));
		assertEquals("", resolvePath(map, "'10002.3'.children.'10002.6'.mother_id"));
		assertEquals("", resolvePath(map, "'10002.3'.children.'10002.6'.mother_type"));
		assertEquals("151111011", resolvePath(map, "'10002.3'.children.'10002.6'.attributes.updatetimestamp"));
		assertEquals("mar, sal, soc", resolvePath(map, "'10002.3'.children.'10002.6'.attributes.permissions.0"));
		assertEquals(1, ((Map<Object, Object>) resolvePath(map, "'10002.3'.children.'10002.6'.attributes.permissions")).size());
		assertEquals(2, ((Map<Object, Object>) resolvePath(map, "'10002.3'.children.'10002.6'.attributes")).size());
		assertEquals(6, ((Map<Object, Object>) resolvePath(map, "'10002.3'.children.'10002.6'")).size());
		assertEquals(2, ((Map<Object, Object>) resolvePath(map, "'10002.3'.children")).size());
		assertEquals(7, ((Map<Object, Object>) resolvePath(map, "'10002.3'")).size());
		
		assertEquals(resolvePath(map, "'10002.4'.contentid"), "10002.4");
		assertEquals("4", resolvePath(map, "'10002.4'.obj_id"));
		assertEquals("10002", resolvePath(map, "'10002.4'.obj_type"));
		assertEquals("", resolvePath(map, "'10002.4'.mother_id"));
		assertEquals("", resolvePath(map, "'10002.4'.mother_type"));
		assertEquals("111111011", resolvePath(map, "'10002.4'.attributes.updatetimestamp"));
		assertEquals(1, ((Map<Object, Object>) resolvePath(map, "'10002.4'.attributes")).size());
		assertEquals(6, ((Map<Object, Object>) resolvePath(map, "'10002.4'")).size());
		
		assertEquals(map.size(), 6);
	}
	private Object resolvePath(Map<Object, Object> map, String path) {
		int pos = -1;
		if(path.indexOf("'") == 0 && (pos = path.indexOf("'", 2)) != -1) {
			String elementPath = path.substring(1, pos);
			if (path.length() > pos + 1 && path.charAt(pos + 1) == '.') {
				String remainingPath = path.substring(pos + 2);
				Object element = map.get(elementPath);
				if (element instanceof Map) {
					return resolvePath((Map<Object, Object>) element, remainingPath);
				}
			} else if(path.length() == pos + 1) {
				return map.get(elementPath);
			}
			throw new AssertionError("cannot resolve " + path + " on " + map);
		} else if ((pos = path.indexOf(".")) != -1) {
			String elementPath = path.substring(0, pos);
			String remainingPath = path.substring(pos + 1);
			Object element = map.get(elementPath);
			if (element instanceof Map) {
				return resolvePath((Map<Object, Object>) element, remainingPath);
			}
			throw new AssertionError("cannot resolve " + path + " on " + map);
		}
		return map.get(path);
	}

}
