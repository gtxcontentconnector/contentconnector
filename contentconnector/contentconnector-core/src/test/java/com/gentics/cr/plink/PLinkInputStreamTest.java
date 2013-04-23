package com.gentics.cr.plink;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.gentics.api.portalnode.connector.PLinkInformation;
import com.gentics.api.portalnode.connector.PLinkReplacer;
import com.gentics.cr.util.StringUtils;

public class PLinkInputStreamTest {

	/*
	 * private static final String TSTRING = "test abc blah <plink id=\"10007.1\"> test"; private String pTest = "";
	 * @Before public void setUp() throws Exception { for (int i = 0; i<1000; i++) { pTest+=TSTRING; } }
	 * @Test public void testPLOSPerf() throws IOException { long startPR = System.currentTimeMillis();
	 * PortalConnectorHelper.replacePLinks(pTest, new PLinkReplacer() { public String replacePLink(PLinkInformation
	 * info) { return info.getContentId(); } }); long durPR = System.currentTimeMillis() - startPR; long startPLIS =
	 * System.currentTimeMillis(); testString(pTest); long durPLIS = System.currentTimeMillis() - startPLIS;
	 * System.out.println("PLIS took: " + durPLIS + ", PR took: " + durPR); assertEquals("PLIS is not faster", true,
	 * durPR > durPLIS); }
	 */

	@Test
	public void testPLIS() throws IOException {
		assertEquals(
			"Could not replace plink",
			"test abc blah 10007.1 test",
			testString("test abc blah <plink id=\"10007.1\"> test"));
		assertEquals(
			"Could not replace plink",
			"test abc blah  asdfa asdf",
			testString("test abc blah <plink id=\"100> asdfa asdf"));
		assertEquals("Could not replace plink", "test abc blah test", testString("test abc blah test"));
		assertEquals(
			"Could not replace plink",
			"test abc blah 10007.1 test",
			testString("test abc blah <plink    id=\"10007.1\"> test"));
		assertEquals("Could not replace plink", "test abc blah <pli test", testString("test abc blah <pli test"));
		assertEquals("Could not replace plink", "test abc blah <> test", testString("test abc blah <> test"));
		assertEquals(
			"Could not replace plink",
			"test abc blah 10007.1 test",
			testString("test abc blah <plink id='10007.1'> test"));
		assertEquals(
			"Could not replace plink",
			"test abc blah 10007.1 test",
			testString("test abc blah <plink id=\"10007.1\" test lkjasdf sdfjlkj> test"));
	}

	private String testString(String input) throws IOException {
		InputStream is = new ByteArrayInputStream(input.getBytes("UTF-8"));

		PLinkInputStream plis = new PLinkInputStream(is, new PLinkReplacer() {

			public String replacePLink(PLinkInformation info) {
				return info.getContentId();
			}
		});

		return StringUtils.streamToString(plis);
	}
}
