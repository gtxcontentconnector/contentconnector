package com.gentics.cr.lucene.indexaccessor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Before;
import org.junit.Test;

public class DefaultIndexAccessorTest {

	private IndexAccessorFactory factory = null;

	private Analyzer analyzer = null;

	private RAMDirectory ramdir = new RAMDirectory();

	private Query query = null;

	@Before
	public void setUp() throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
		factory = IndexAccessorFactory.getInstance();
		analyzer = (Analyzer) Class.forName("org.apache.lucene.analysis.WhitespaceAnalyzer").getConstructor().newInstance();
		query = new BooleanQuery();
	}

	@Test
	public void createAccessor() throws IOException {
		factory.createAccessor(ramdir, analyzer);

		IndexAccessor accessor = factory.getAccessor(ramdir);
		assertNotNull(accessor);
	}

	@Test
	public void createAccessorWithQuery() throws IOException {
		factory.createAccessor(ramdir, analyzer, query);

		IndexAccessor accessor = factory.getAccessor(ramdir);
		assertNotNull(accessor);
	}

	@Test
	public void testGetReaderWithClose() throws IOException {
		factory.createAccessor(ramdir, analyzer);

		IndexAccessor accessor = factory.getAccessor(ramdir);
		assertNotNull(accessor);

		IndexReader writingReader1 = accessor.getReader(true);
		assertEquals(1, accessor.writingReadersUseCount());
		writingReader1.close();
		assertEquals(1, accessor.writingReadersUseCount());
		accessor.release(writingReader1, true);
		assertEquals(0, accessor.writingReadersUseCount());
		accessor.close();
		assertEquals(0, accessor.writingReadersUseCount());
	}

	@Test
	public void testGetWriterWithClose() throws IOException {
		factory.createAccessor(ramdir, analyzer);

		IndexAccessor accessor = factory.getAccessor(ramdir);
		assertNotNull(accessor);

		IndexWriter writer = accessor.getWriter();
		assertEquals(1, accessor.writerUseCount());
		writer.close();
		assertEquals(1, accessor.writerUseCount());
		accessor.release(writer);
		assertEquals(0, accessor.writerUseCount());
		accessor.close();
		assertEquals(0, accessor.writerUseCount());
	}

	@Test
	public void testGetSearcherWithIndexReader() throws IOException {
		factory.createAccessor(ramdir, analyzer);

		IndexAccessor accessor = factory.getAccessor(ramdir);
		assertNotNull(accessor);

		IndexReader reader = accessor.getReader(true);
		Searcher searcher = accessor.getSearcher(reader);

		accessor.release(searcher);
	}

	@Test
	public void testGetSearcher() throws IOException {
		factory.createAccessor(ramdir, analyzer);

		IndexAccessor accessor = factory.getAccessor(ramdir);
		assertNotNull(accessor);

		IndexReader reader = accessor.getReader(true);
		Searcher searcher = accessor.getSearcher();

		accessor.release(searcher);
	}

	@Test
	public void testGetCachedSearcher() throws IOException {
		factory.createAccessor(ramdir, analyzer);

		IndexAccessor accessor = factory.getAccessor(ramdir);
		assertNotNull(accessor);

		IndexReader reader = accessor.getReader(true);
		Searcher searcher = accessor.getSearcher();

		Searcher searcher2 = accessor.getSearcher();

		assertEquals(searcher, searcher2);

		accessor.release(searcher);
		accessor.release(searcher2);
	}

}
