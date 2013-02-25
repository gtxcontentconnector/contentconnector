package com.gentics.cr.util;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Test;

public class CRRequestBuilderTest {
	
	
	
	
	@Test
	public void testContentRepositoryTypePHP() {
		testContentRepositoryType("PHP");
	}
	
	@Test
	public void testContentRepositoryTypeJSON() {
		testContentRepositoryType("JSON");
	}
	
	@Test
	public void testContentRepositoryTypeXML() {
		testContentRepositoryType("XML");
	}
	
	@Test
	public void testContentRepositoryTypeJAVABIN() {
		testContentRepositoryType("JAVABIN");
	}
	
	private void testContentRepositoryType(String type) {
		HashMap<String, String> parameters = new HashMap<String, String>();
		parameters.put("type", type);
		CRRequestBuilder builder = new CRRequestBuilder(new MockHttpServletRequest(parameters));
		String actType = builder.getContentRepositoryConfig().getRepositoryType();
		assertEquals("Repository type is not correct.", type, actType);
	}
}
