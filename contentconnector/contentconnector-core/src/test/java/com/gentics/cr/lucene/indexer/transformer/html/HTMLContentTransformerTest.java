package com.gentics.cr.lucene.indexer.transformer.html;

import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;

import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.exceptions.CRException;

public class HTMLContentTransformerTest {

	File file = null;
	static CRConfigUtil config = new CRConfigUtil();
	CRResolvableBean bean = new CRResolvableBean();

	@Before
	public void prepare() throws URISyntaxException, IOException {
		file = new File(this.getClass().getResource("test.html").toURI());
		BufferedReader br = new BufferedReader(new FileReader(file));

		StringBuilder fileContent = new StringBuilder();
		String sCurrentLine;
		while ((sCurrentLine = br.readLine()) != null) {
			fileContent.append(sCurrentLine);
		}
		br.close();

		bean.set("contentid", "10007.1");
		bean.set("content", fileContent.toString());

		config.set(HTMLContentTransformer.TRANSFORMER_ATTRIBUTE_KEY, "content");
	}

	@Test
	public void test() throws CRException {
		HTMLContentTransformer transformer = new HTMLContentTransformer(config);
		transformer.processBean(bean);
		assertNotNull("The result should never be null", bean.get("content"));
	}
}
