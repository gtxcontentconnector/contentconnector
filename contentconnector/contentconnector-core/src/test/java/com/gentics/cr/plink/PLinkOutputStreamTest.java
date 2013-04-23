package com.gentics.cr.plink;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.junit.Test;

import com.gentics.api.portalnode.connector.PLinkInformation;
import com.gentics.api.portalnode.connector.PLinkReplacer;

public class PLinkOutputStreamTest {

	/*
	 * private static final String TSTRING = "test abc blah <plink id=\"10007.1\"> test"; private String pTest = "";
	 * @Before public void setUp() throws Exception { for (int i = 0; i<1000; i++) { pTest+=TSTRING; } }
	 * @Test public void testPLOSPerf() throws IOException { long startPR = System.currentTimeMillis();
	 * PortalConnectorHelper.replacePLinks(pTest, new PLinkReplacer() { public String replacePLink(PLinkInformation
	 * info) { return info.getContentId(); } }); long durPR = System.currentTimeMillis() - startPR; long startPLOS =
	 * System.currentTimeMillis(); testString(pTest); long durPLOS = System.currentTimeMillis() - startPLOS;
	 * System.out.println("PLOS took: " + durPLOS + ", PR took: " + durPR); assertEquals("PLOS is not faster", true,
	 * durPR > durPLOS); }
	 */

	@Test
	public void testPLOS() throws IOException {
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
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		PLinkOutputStream plOs = new PLinkOutputStream(os, new PLinkReplacer() {

			public String replacePLink(PLinkInformation info) {
				return info.getContentId();
			}
		});

		OutputStreamWriter w = new OutputStreamWriter(plOs);
		w.write(input);
		w.close();

		plOs.close();

		return os.toString();
	}
}
