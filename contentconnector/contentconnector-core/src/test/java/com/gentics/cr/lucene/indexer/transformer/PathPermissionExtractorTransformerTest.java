package com.gentics.cr.lucene.indexer.transformer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PathPermissionExtractorTransformerTest {

	@Test
	public void testPubdirPermission() {
		String path = "/ASDF";
		assertEquals("asdf", PathPermissionExtractorTransformer.extractPermission(path));
	}

	@Test
	public void testPubdir1() {
		String path = "/ASDF/";
		assertEquals("asdf", PathPermissionExtractorTransformer.extractPermission(path));
	}

	@Test
	public void testPubdir2() {
		String path = "/ASDF/test/";
		assertEquals("asdf", PathPermissionExtractorTransformer.extractPermission(path));
	}

	@Test
	public void testPubdir3() {
		String path = "/ASDF/test/asdf";
		assertEquals("asdf", PathPermissionExtractorTransformer.extractPermission(path));
	}

	@Test
	public void testLowercaseConversion() {
		String path = "/ASDF/asdf/TEST";
		assertEquals("asdf", PathPermissionExtractorTransformer.extractPermission(path));
	}

	@Test
	public void testroot() {
		String path = "/";
		assertEquals(PathPermissionExtractorTransformer.NORESTRICTIONS, PathPermissionExtractorTransformer.extractPermission(path));
	}

	@Test
	public void testnull() {
		String path = null;
		assertEquals(PathPermissionExtractorTransformer.NORESTRICTIONS, PathPermissionExtractorTransformer.extractPermission(path));
	}

	@Test
	public void testempty() {
		String path = "";
		assertEquals(PathPermissionExtractorTransformer.NORESTRICTIONS, PathPermissionExtractorTransformer.extractPermission(path));
	}

}
