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
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class MultiIndexAccessorTest {

	private IndexAccessorFactory factory = null;

	private Analyzer analyzer = null;

	private RAMDirectory ramdir = new RAMDirectory();
	private RAMDirectory ramdir2 = new RAMDirectory();

	private Query query = null;
	
	@Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

	@Before
	public void setUp() throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException, ClassNotFoundException, IOException {
		factory = IndexAccessorFactory.getInstance();
		analyzer = (Analyzer) Class.forName("org.apache.lucene.analysis.WhitespaceAnalyzer").getConstructor().newInstance();
		query = new BooleanQuery();
		factory.createAccessor(ramdir, analyzer);
		factory.createAccessor(ramdir2, analyzer);
	}

	@Test
	public void createAccessor() throws IOException {
		
		IndexAccessor accessor = factory.getMultiIndexAccessor(new Directory[]{ramdir, ramdir2});
		assertNotNull(accessor);
	}

	@Test
	public void createAccessorWithQuery() throws IOException {
		IndexAccessor accessor = factory.getMultiIndexAccessor(new Directory[]{ramdir, ramdir2});
		assertNotNull(accessor);
	}

	@Test
	public void testGetReaderWithClose() throws IOException {
		
		IndexAccessor accessor = factory.getMultiIndexAccessor(new Directory[]{ramdir, ramdir2});
		assertNotNull(accessor);

		IndexReader readingReader1 = accessor.getReader(false);
		readingReader1.close();
		accessor.release(readingReader1, false);
		assertEquals(0, accessor.writingReadersUseCount());
		accessor.close();
		assertEquals(0, accessor.writingReadersUseCount());
	}

	@Test
	public void testGetSearcherWithIndexReader() throws IOException {
		
		IndexAccessor accessor = factory.getMultiIndexAccessor(new Directory[]{ramdir, ramdir2});
		assertNotNull(accessor);

		IndexReader reader = accessor.getReader(false);
		IndexSearcher searcher = accessor.getSearcher(reader);

		accessor.release(searcher);
		accessor.close();
	}

	@Test
	public void testGetSearcher() throws IOException {
		
		IndexAccessor accessor = factory.getMultiIndexAccessor(new Directory[]{ramdir, ramdir2});
		assertNotNull(accessor);

		IndexReader reader = accessor.getReader(false);
		assertNotNull(reader);
		IndexSearcher searcher = accessor.getSearcher();

		accessor.release(searcher);
		accessor.close();
	}

	
	@Test
	public void testReopen() throws IOException, URISyntaxException {
		File reopenIndexLocation = testFolder.newFolder("reopenIndexLocation");
		File originalIndex = new File(this.getClass().getResource("orignalIndex").toURI());
		File changedIndex = new File(this.getClass().getResource("changedIndex").toURI());
		
		FileUtils.copyDirectory(originalIndex, reopenIndexLocation);
		FSDirectory fsDir = FSDirectory.open(reopenIndexLocation);
		factory.createAccessor(fsDir, analyzer);
		IndexAccessor accessor = factory.getMultiIndexAccessor(new Directory[]{fsDir});
		
		IndexReader reader = accessor.getReader(false);
		accessor.reopen();
		IndexReader newReader = accessor.getReader(false);
		
		// the reading reader use count should be 2
		assertEquals(accessor.readingReadersOut(), 2);
		FileUtils.copyDirectory(changedIndex, reopenIndexLocation);
		accessor.reopen();
		IndexReader changedReader = accessor.getReader(false);
		// after the index is overwritten the call to "getReader" should return a new reader
		assertEquals(reader.equals(changedReader), false);
		// the reading reader use count should be 1 because only the new Reader is counted
		assertEquals(accessor.readingReadersOut(), 1);
		accessor.release(changedReader, false);
		assertEquals(accessor.readingReadersOut(), 0);
		// releasing those readers should throw no illegal argument exception
		accessor.release(reader, false);
		accessor.release(newReader, false);
		accessor.release(changedReader, false);
		// releasing an already released reader should not change the use count
		assertEquals(accessor.readingReadersOut(), 0);
	}

}
