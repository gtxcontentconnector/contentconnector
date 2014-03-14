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
	
	@Test
	public void testContentRepositoryTypeDEFAULT() {
		HashMap<String, String> parameters = new HashMap<String, String>();
		CRRequestBuilder builder = new CRRequestBuilder(new MockHttpServletRequest(parameters));
		String actType = builder.getContentRepositoryConfig().getRepositoryType();
		assertEquals("Repository type is not correct.", "XML", actType);
	}
	
	private void testContentRepositoryType(String type) {
		HashMap<String, String> parameters = new HashMap<String, String>();
		parameters.put("type", type);
		CRRequestBuilder builder = new CRRequestBuilder(new MockHttpServletRequest(parameters));
		String actType = builder.getContentRepositoryConfig().getRepositoryType();
		assertEquals("Repository type is not correct.", type, actType);
	}
}
