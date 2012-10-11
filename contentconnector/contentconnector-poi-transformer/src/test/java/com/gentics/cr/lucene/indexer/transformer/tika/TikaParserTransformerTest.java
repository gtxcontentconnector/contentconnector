package com.gentics.cr.lucene.indexer.transformer.tika;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.poi.util.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;
import com.gentics.cr.lucene.indexer.transformer.test.TUtil;

public class TikaParserTransformerTest extends TestCase {
	private static final String BINARY_ATTRIBUTE = "binarycontent";
	GenericConfiguration config;

	ContentTransformer t;

	@Before
	public void setUp() throws Exception {
		config = new GenericConfiguration();
		config.set("attribute", BINARY_ATTRIBUTE);

		t = new TikaParserTransformer(config);
	}

	/**
	 * Retrieve the contents of the given file.
	 * @param filename - name of the fiel to get
	 * @return contents of the file
	 * @throws IOException in case the file cannot be opened
	 */
	private byte[] getContentFromFile(final String filename)
			throws IOException {
		InputStream stream1 = 
				TUtil.class.getResourceAsStream(filename);
		return IOUtils.toByteArray(stream1);
	}

	@Test
	public void testTransformerDOCM() throws Exception {
		testDocument("testdoc.docm");
	}
	
	@Test
	public void testTransformerDOCX() throws Exception {
		testDocument("testdoc.docx");
	}
	
	@Test
	public void testTransformerDOTM() throws Exception {
		testDocument("testdoc.dotm");
	}
	
	@Test
	public void testTransformerDOTX() throws Exception {
		testDocument("testdoc.dotx");
	}
	
	private void testDocument(String filename) throws IOException, CRException {
		CRResolvableBean bean = new CRResolvableBean();
		bean.set(BINARY_ATTRIBUTE, getContentFromFile(filename));
		t.processBean(bean);
		assertEquals("Testtext ÄÖÜäüöß€\n", bean.get(BINARY_ATTRIBUTE));
	}
	
	@Test
	public void testTransformerMSPPTX() throws Exception {
		testPresentation("testdoc.mspowerpoint2010.pptx");
	}
	
	@Test
	public void testTransformerPOTM() throws Exception {
		testPresentation("testdoc.potm");
	}
	
	@Test
	public void testTransformerPOTX() throws Exception {
		testPresentation("testdoc.potx");
	}
	
	@Test
	public void testTransformerPPSM() throws Exception {
		testPresentation("testdoc.ppsm");
	}
	
	@Test
	public void testTransformerPPSX() throws Exception {
		testPresentation("testdoc.ppsx");
	}
	
	@Test
	public void testTransformerPPTM() throws Exception {
		testPresentation("testdoc.pptm");
	}
	
	@Test
	public void testTransformerPPTX() throws Exception {
		testPresentation("testdoc.pptx");
	}
	
	
	private void testPresentation(String filename) throws IOException, CRException {
		CRResolvableBean bean = new CRResolvableBean();
		bean.set(BINARY_ATTRIBUTE, getContentFromFile(filename));
		t.processBean(bean);
		assertEquals("Testtext ÄÖÜäüöß€\n", bean.get(BINARY_ATTRIBUTE));
	}

	@Test
	public void testTransformerXLAM() throws Exception {
		testSpreadsheet("testdoc.xlam");
	}
	
	@Test
	public void testTransformerXLSM() throws Exception {
		testSpreadsheet("testdoc.xlsm");
	}
	
	@Test
	public void testTransformerXLSX() throws Exception {
		testSpreadsheet("testdoc.xlsx");
	}
	
	@Test
	public void testTransformerXLTM() throws Exception {
		testSpreadsheet("testdoc.xltm");
	}
	
	@Test
	public void testTransformerXLTX() throws Exception {
		testSpreadsheet("testdoc.xltx");
	}
	
	private void testSpreadsheet(String filename) throws IOException, CRException {
		CRResolvableBean bean = new CRResolvableBean();
		bean.set(BINARY_ATTRIBUTE, getContentFromFile(filename));
		t.processBean(bean);
		assertEquals("Tabelle1\n\ttesttext ÄÖÜäüöß€\n\n\nTabelle2\n\n\nTabelle3\n\n\n",
				bean.get(BINARY_ATTRIBUTE));
	}

}
