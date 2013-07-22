package com.gentics.cr.lucene.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.junit.Before;
import org.junit.Test;

import com.gentics.cr.lucene.search.query.mocks.SimpleLucene;

public class CRLuceneUtilTest {
	private SimpleLucene lucene;
	
	@Before
	public void setUp() throws CorruptIndexException, IOException {
		lucene = new SimpleLucene();
		Map<String,String> fields = new HashMap<String, String>();
		
		fields.put("content", "test");
		fields.put("testfield1", "test");
		fields.put("testfield2", "test");
		fields.put("someotherfield", "field");
		
		lucene.add(fields);
		
		fields = new HashMap<String, String>();
		
		fields.put("content2", "test");
		fields.put("testfield1", "test");
		fields.put("testfield3", "test");
		fields.put("someotherfield2", "field");
		
		lucene.add(fields);
	}
	
	@Test
	public void testGetFieldNames() throws CorruptIndexException, IOException {
		IndexReader reader = lucene.getReader();
		List<String> nameList = CRLuceneUtil.getFieldNames(reader);
		String[] expectedFields = new String[]{"content", "testfield1", "testfield2", "someotherfield", "content2", "testfield3", "someotherfield2"};
		for(String s : expectedFields) {
			Assert.assertTrue("Expected field " + s + " could not be found in name list.", nameList.contains(s));
		}
	}
}
