package com.gentics.cr.lucene.indexaccessor;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Before;
import org.junit.Test;

public class IndexAccessorFactoryTest {

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

	@Test(expected = IllegalStateException.class)
	public void testCreateAccessor() throws IOException {
		factory.getAccessor(ramdir);
	}

}
