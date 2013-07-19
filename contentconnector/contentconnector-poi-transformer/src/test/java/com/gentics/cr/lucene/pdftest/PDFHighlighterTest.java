package com.gentics.cr.lucene.pdftest;

import java.io.InputStream;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.poi.util.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.LuceneVersion;
import com.gentics.cr.lucene.indexer.transformer.AbstractTransformerTest;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;
import com.gentics.cr.lucene.indexer.transformer.pdf.PDFContentTransformer;
import com.gentics.cr.lucene.search.highlight.ContentHighlighter;
import com.gentics.cr.lucene.search.highlight.PhraseBolder;
import com.gentics.cr.lucene.search.highlight.VectorBolder;
import com.gentics.cr.lucene.search.highlight.WhitespaceVectorBolder;

public class PDFHighlighterTest extends AbstractTransformerTest {

	private static final int HITS = 1;

	CRResolvableBean bean;
	GenericConfiguration config;

	Directory dir;
	IndexSearcher searcher;
	Analyzer analyzer;
	Query query;

	@Before
	public void init() throws Exception {
		bean = new CRResolvableBean();

		InputStream stream = PDFHighlighterTest.class.getResourceAsStream("test.pdf");
		byte[] arr = IOUtils.toByteArray(stream);
		bean.set("binarycontent", arr);

		config = new GenericConfiguration();
		config.set("attribute", "binarycontent");
		analyzer = new StandardAnalyzer(LuceneVersion.getVersion());
		dir = new RAMDirectory();
		prepareIndex();

		searcher = new IndexSearcher(dir);

		QueryParser parser = new QueryParser(LuceneVersion.getVersion(), "binarycontent", analyzer);

		query = parser.parse("binarycontent:(ahst~0.5)");

		query = query.rewrite(searcher.getIndexReader());
	}

	private Document getDocument(CRResolvableBean bean) throws Exception {
		//TRANSFORM BEAN
		ContentTransformer t = new PDFContentTransformer(config);
		t.processBean(bean);

		//CREATE DOCUMENT
		Document doc = new Document();
		Object value = bean.get("binarycontent");
		Field f = new Field("binarycontent", value.toString(), Store.YES, Field.Index.ANALYZED,
				TermVector.WITH_POSITIONS_OFFSETS);
		doc.add(f);
		doc.add(new Field("testid", "pdftest", Field.Store.YES, Field.Index.NOT_ANALYZED));
		return doc;
	}

	private void prepareIndex() throws Exception {
		IndexWriter writer = new IndexWriter(dir, analyzer, IndexWriter.MaxFieldLength.UNLIMITED);
		writer.addDocument(getDocument(bean));
		writer.close();

	}

	@Test
	public void testVectorBolder() throws Exception {
		System.out.println("VECTOR");
		VectorBolder h = new VectorBolder(new GenericConfiguration());
		String ret = h.highlight(query, searcher.getIndexReader(), 0, "binarycontent");
		System.out.println(ret);

		Assert.assertTrue(ret != null && !"".equals(ret));
	}

	@Test
	public void testWhitespaceVectorBolder() throws Exception {
		System.out.println("WHITESPACEVECTOR");
		WhitespaceVectorBolder h = new WhitespaceVectorBolder(new GenericConfiguration());
		String ret = h.highlight(query, searcher.getIndexReader(), 0, "binarycontent");
		System.out.println(ret);

		Assert.assertTrue(ret != null && !"".equals(ret));
	}

	@Test
	public void testPhraseBolder2() throws Exception {
		System.out.println("PHRASE2");
		ContentHighlighter h = new PhraseBolder(new GenericConfiguration());
		CRResolvableBean crBean = new CRResolvableBean();
		crBean.set("binarycontent", "this is a test (AHSt)");
		String ret = h.highlight((String) crBean.get("binarycontent"), query);
		System.out.println(ret);

		Assert.assertTrue(ret != null && !"".equals(ret));
	}

	@After
	public void tearDown() throws Exception {

	}
}
