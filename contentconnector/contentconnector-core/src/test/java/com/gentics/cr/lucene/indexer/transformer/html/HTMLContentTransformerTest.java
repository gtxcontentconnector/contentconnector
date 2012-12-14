package com.gentics.cr.lucene.indexer.transformer.html;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;

import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.exceptions.CRException;

public class HTMLContentTransformerTest {

	@Test
	public void testContentAttribute() throws URISyntaxException, IOException, CRException {
		CRConfigUtil config = new CRConfigUtil();
		File file = new File(this.getClass().getResource("test.html").toURI());
		BufferedReader br = new BufferedReader(new FileReader(file));

		StringBuilder fileContent = new StringBuilder();
		String sCurrentLine;
		while ((sCurrentLine = br.readLine()) != null) {
			fileContent.append(sCurrentLine);
		}
		br.close();

		CRResolvableBean bean = new CRResolvableBean();
		bean.set("contentid", "10007.1");
		bean.set("content", fileContent.toString());

		config.set(HTMLContentTransformer.TRANSFORMER_ATTRIBUTE_KEY, "content");

		HTMLContentTransformer transformer = new HTMLContentTransformer(config);
		transformer.processBean(bean);
		assertNotNull("The result should never be null", bean.get("content"));
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
}
