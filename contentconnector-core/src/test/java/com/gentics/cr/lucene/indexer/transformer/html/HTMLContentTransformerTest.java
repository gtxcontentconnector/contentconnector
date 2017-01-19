package com.gentics.cr.lucene.indexer.transformer.html;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;

import com.gentics.cr.conf.gentics.ConfigDirectory;
import org.junit.Before;
import org.junit.Test;

import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.exceptions.CRException;

public class HTMLContentTransformerTest {
	@Before
	public void init() throws URISyntaxException {
		ConfigDirectory.useThis();
	}

	@Test
	public void testContentAttribute() throws URISyntaxException, IOException, CRException {
		CRConfigUtil config = new CRConfigUtil();
		config.set(HTMLContentTransformer.TRANSFORMER_ATTRIBUTE_KEY, "content");

		CRResolvableBean bean = new CRResolvableBean();
		bean.set("contentid", "10007.1");
		bean.set("content", readFile("test.html").toString());

		HTMLContentTransformer transformer = new HTMLContentTransformer(config);
		transformer.processBean(bean);

		assertEquals(readFile("testresult.html").toString(), bean.get("content"));
	}

	@Test
	public void testNameAttribute() throws CRException {
		CRConfigUtil config = new CRConfigUtil();
		config.set(HTMLContentTransformer.TRANSFORMER_ATTRIBUTE_KEY, "name");
		HTMLContentTransformer transformer = new HTMLContentTransformer(config);

		CRResolvableBean bean1 = new CRResolvableBean();
		bean1.set("contentid", "10007.1");
		bean1.set("name", "Fußball");
		transformer.processBean(bean1);
		assertEquals("Fußball", bean1.get("name"));

		CRResolvableBean bean2 = new CRResolvableBean();
		bean2.set("name", "Über das USP");
		transformer.processBean(bean2);
		assertEquals("Über das USP", bean2.get("name"));

		CRResolvableBean bean3 = new CRResolvableBean();
		bean3.set("name", "<strong>das ist eine test überschrift</strong>");
		transformer.processBean(bean3);
		assertEquals("das ist eine test überschrift", bean3.get("name"));
	}

	@Test
	public void testStrings() throws CRException {
		CRConfigUtil config = new CRConfigUtil();
		config.set(HTMLContentTransformer.TRANSFORMER_ATTRIBUTE_KEY, "name");
		HTMLContentTransformer transformer = new HTMLContentTransformer(config);

		CRResolvableBean bean = new CRResolvableBean();
		bean.set("name", "ASDF-lexikon\n\t\n\t\t\t<br class=\"aloha-end-br\"/>");
		transformer.processBean(bean);
		assertEquals("ASDF-lexikon", bean.get("name"));

		CRResolvableBean bean2 = new CRResolvableBean();
		bean2.set("name", "<abbr title=\"Informations- und Kommunikationstechnologie\">IKT</abbr>-Sicherheitslexikon	");
		transformer.processBean(bean2);
		assertEquals("IKT-Sicherheitslexikon", bean2.get("name"));

		CRResolvableBean bean3 = new CRResolvableBean();
		bean3.set("name", "<abbr title=\"Informations- und Kommunikationstechnologie\">IKT</abbr>- Sicherheitslexikon	");
		transformer.processBean(bean3);
		assertEquals("IKT- Sicherheitslexikon", bean3.get("name"));

		CRResolvableBean bean4 = new CRResolvableBean();
		bean4.set("name", "<br class=\"aloha-end-br\"/>");
		transformer.processBean(bean4);
		assertEquals("", bean4.get("name"));
	}
	/**
	 * Test if spaces after block elements are added 
	 * 
	 * Example 1: (space between block elemets added)
	 * [..] lorem ipsum.&lt;/p&gt;&lt;h2&gt;Dolor sit amet [..] => lorem ipsum. Dolor sit amet
	 *  
	 * Example 2: (no spaces between inline elements)
	 * &lt;strong>ip&lt;/strong>&lt;i>sum&lt;/i> => ipsum
	 *  
	 * Example 3: (no spaces between inline elements)
	 * &lt;strong>ip&lt;/strong>&lt;i>sum&lt;/i> => ipsum
	 *  
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws CRException
	 */
	@Test 
	public void testSpaces() throws URISyntaxException, IOException, CRException {
		CRConfigUtil config = new CRConfigUtil();
		config.set(HTMLContentTransformer.TRANSFORMER_ATTRIBUTE_KEY, "content");

		CRResolvableBean bean = new CRResolvableBean();
		bean.set("contentid", "10007.1");
		bean.set("content", readFile("testHtmlSpaces.html").toString());

		HTMLContentTransformer transformer = new HTMLContentTransformer(config);
		transformer.processBean(bean);
		
		assertEquals(readFile("testHtmlSpacesResult.html").toString().trim(), bean.get("content").toString().trim());
	}

	private StringBuilder readFile(final String fileName) throws URISyntaxException, FileNotFoundException, IOException {
		File file = new File(this.getClass().getResource(fileName).toURI());
		BufferedReader br = new BufferedReader(new FileReader(file));

		StringBuilder fileContent = new StringBuilder();
		String sCurrentLine;
		while ((sCurrentLine = br.readLine()) != null) {
			fileContent.append(sCurrentLine);
		}
		br.close();
		return fileContent;
	}

}
