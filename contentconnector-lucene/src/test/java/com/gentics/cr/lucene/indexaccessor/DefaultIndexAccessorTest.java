package com.gentics.cr.lucene.indexaccessor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.gentics.cr.lucene.LuceneVersion;

public class DefaultIndexAccessorTest {

	private IndexAccessorFactory factory = null;

	private Analyzer analyzer = null;

	private RAMDirectory ramdir = new RAMDirectory();

	private Query query = null;
	
	@Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

	@Before
	public void setUp() throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
		factory = IndexAccessorFactory.getInstance();
		analyzer = (Analyzer) Class.forName("org.apache.lucene.analysis.core.WhitespaceAnalyzer").getConstructor(Version.class).newInstance(LuceneVersion.getVersion());
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

		IndexReader writingReader1 = accessor.getReader();
		assertEquals(1, accessor.readingReadersOut());
		writingReader1.close();
		assertEquals(1, accessor.readingReadersOut());
		accessor.release(writingReader1);
		assertEquals(0, accessor.readingReadersOut());
		accessor.close();
		assertEquals(0, accessor.readingReadersOut());
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

		IndexReader reader = accessor.getReader();
		IndexSearcher searcher = accessor.getSearcher(reader);

		accessor.release(searcher);
		accessor.release(reader);
		assertEquals("Reader has not returned",0,accessor.readingReadersOut());
	}

	@Test
	public void testGetSearcher() throws IOException {
		factory.createAccessor(ramdir, analyzer);

		IndexAccessor accessor = factory.getAccessor(ramdir);
		assertNotNull(accessor);

		IndexReader reader = accessor.getReader();
		assertNotNull(reader);
		IndexSearcher searcher = accessor.getSearcher();
		accessor.release(reader);
		assertEquals("Reader has not returned",0,accessor.readingReadersOut());
		accessor.release(searcher);
	}

	@Test
	public void testGetCachedSearcher() throws IOException {
		factory.createAccessor(ramdir, analyzer);

		IndexAccessor accessor = factory.getAccessor(ramdir);
		assertNotNull(accessor);

		IndexReader reader = accessor.getReader();
		assertNotNull(reader);
		IndexSearcher searcher = accessor.getSearcher();

		IndexSearcher searcher2 = accessor.getSearcher();

		assertEquals(searcher, searcher2);
		accessor.release(reader);
		assertEquals("Reader has not returned",0,accessor.readingReadersOut());
		accessor.release(searcher);
		accessor.release(searcher2);
	}
	@Test
	public void testReopen() throws IOException, URISyntaxException {
		File reopenIndexLocation = testFolder.newFolder("reopenIndexLocation");
		File originalIndex = new File(this.getClass().getResource("orignalIndex").toURI());
		File changedIndex = new File(this.getClass().getResource("changedIndex").toURI());
		
		FileUtils.copyDirectory(originalIndex, reopenIndexLocation);
		FSDirectory fsDir = FSDirectory.open(reopenIndexLocation);
		factory.createAccessor(fsDir, analyzer);
		IndexAccessor accessor = factory.getAccessor(fsDir);
		
		IndexReader reader = accessor.getReader();
		accessor.reopen();
		IndexReader newReader = accessor.getReader();
		// before the index is overwritten every call to "getReader" should return the same reader
		assertEquals(reader, newReader);
		// the reading reader use count should be 2
		assertEquals(accessor.readingReadersOut(), 2);
		FileUtils.copyDirectory(changedIndex, reopenIndexLocation);
		accessor.reopen();
		IndexReader changedReader = accessor.getReader();
		// after the index is overwritten the call to "getReader" should return a new reader
		assertEquals(reader.equals(changedReader), false);
		// the reading reader use count should be 1 because only the new Reader is counted
		assertEquals(accessor.readingReadersOut(), 1);
		accessor.release(changedReader);
		assertEquals(accessor.readingReadersOut(), 0);
		// releasing those readers should throw no illegal argument exception
		accessor.release(reader);
		accessor.release(newReader);
		accessor.release(changedReader);
		// releasing an already released reader should not change the use count
		assertEquals(accessor.readingReadersOut(), 0);
	}

}
