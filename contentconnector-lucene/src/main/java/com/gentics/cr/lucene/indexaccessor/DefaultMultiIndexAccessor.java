package com.gentics.cr.lucene.indexaccessor;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;

/**
 * Default MultiIndexAccessor implementation.
 * 
 * Last changed: $Date: 2009-09-02 17:57:48 +0200 (Mi, 02 Sep 2009) $
 * @version $Revision: 180 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class DefaultMultiIndexAccessor implements IndexAccessor {
	/**
	 * Log4j logger for error and debug messages.
	 */
	private static final Logger LOGGER = Logger.getLogger(DefaultMultiIndexAccessor.class);
	private final Map<IndexSearcher, IndexAccessor> multiSearcherAccessors = new ConcurrentHashMap<IndexSearcher, IndexAccessor>();
	private final Map<IndexReader, IndexAccessor> multiReaderAccessors = new ConcurrentHashMap<IndexReader, IndexAccessor>();
	private final Map<MultiReader, IndexReader[]> subReaderList = new ConcurrentHashMap<MultiReader, IndexReader[]>();
	private Similarity similarity;

	private Directory[] dirs;

	/**
	 * Create new Instance.
	* @param dirs 
	 */
	public DefaultMultiIndexAccessor(Directory[] dirs) {
		this.similarity = IndexSearcher.getDefaultSimilarity();
		this.dirs = dirs;
	}

	/**
	 * Create new instance.
	* @param dirs 
	 * @param similarity
	 */
	public DefaultMultiIndexAccessor(Directory[] dirs, Similarity similarity) {
		this.similarity = similarity;
		this.dirs = dirs;
	}

	/*
	 * (non-Javadoc)
	 * @see com.mhs.indexaccessor.MultiIndexAccessor#release(org.apache.lucene.search.Searcher)
	 */
	public synchronized void release(IndexSearcher multiSearcher) {
		IndexReader reader = multiSearcher.getIndexReader();
		release(reader);
	}

	/**
	 * Closes all index accessors contained in the multi accessor.
	 */
	public void close() {
		for (Entry<IndexSearcher, IndexAccessor> iae : this.multiSearcherAccessors.entrySet()) {
			IndexAccessor ia = iae.getValue();
			if (ia.isOpen()) {
				ia.close();
			}
		}
		for (Entry<IndexReader, IndexAccessor> iae : this.multiReaderAccessors.entrySet()) {
			IndexAccessor ia = iae.getValue();
			if (ia.isOpen()) {
				ia.close();
			}
		}
	}

	public IndexSearcher getPrioritizedSearcher() throws IOException {

		return getSearcher();
	}

	public IndexReader getReader() throws IOException {
		

		IndexReader[] readers = new IndexReader[this.dirs.length];

		IndexAccessorFactory factory = IndexAccessorFactory.getInstance();
		int i = 0;
		for (Directory index : this.dirs) {
			IndexAccessor indexAccessor = factory.getAccessor(index);
			readers[i] = indexAccessor.getReader();
			multiReaderAccessors.put(readers[i], indexAccessor);
			i++;
		}

		MultiReader multiReader = new MultiReader(readers, true);
		subReaderList.put(multiReader, readers);
		return multiReader;
	}
	
	/**
	 * Get directories.
	 * @return
	 */
	public Directory[] getDirectories() {
		return dirs;
	}

	public IndexSearcher getSearcher() throws IOException {
		return getSearcher(this.similarity, null);
	}

	public IndexSearcher getSearcher(IndexReader indexReader) throws IOException {
		return getSearcher(this.similarity, indexReader);
	}
	
	public IndexSearcher getSearcher(Similarity similarity) throws IOException {
		return getSearcher(similarity, null);
	}

	public IndexSearcher getSearcher(final Similarity similarity, final IndexReader indexReader) throws IOException {
		IndexReader ir = indexReader;
		if (ir == null) {
			ir = getReader();
		}
		IndexSearcher multiSearcher = new IndexSearcher(ir);
		multiSearcher.setSimilarity(similarity);
		
		return multiSearcher;
	}

	public IndexWriter getWriter() throws IOException {
		throw new UnsupportedOperationException();
	}

	public boolean isOpen() {
		boolean open = true;
		IndexAccessorFactory factory = IndexAccessorFactory.getInstance();
		for (Directory index : this.dirs) {
			IndexAccessor indexAccessor = factory.getAccessor(index);
			if (!indexAccessor.isOpen()) {
				open = false;
			}
		}
		return open;
	}

	public boolean isLocked() {
		boolean locked = false;
		for (Directory d : this.dirs) {
			try {
				locked = IndexWriter.isLocked(d);
			} catch (IOException e) {
				LOGGER.error(e);
			}
			if (locked) {
				break;
			}
		}
		return locked;
	}

	public void open() {
		IndexAccessorFactory factory = IndexAccessorFactory.getInstance();
		for (Directory index : this.dirs) {
			IndexAccessor indexAccessor = factory.getAccessor(index);
			indexAccessor.open();
		}
	}

	public int readingReadersOut() {
		int usecount = 0;
		IndexAccessorFactory factory = IndexAccessorFactory.getInstance();
		for (Directory index : this.dirs) {
			IndexAccessor indexAccessor = factory.getAccessor(index);
			usecount += indexAccessor.readingReadersOut();
		}
		return usecount;
	}

	public void release(IndexReader reader) {
		
		IndexReader[] subReaders = subReaderList.get(reader);
		if (subReaders != null) {
			for (IndexReader r : subReaders) {
				IndexAccessor accessor = multiReaderAccessors.get(r);
				if (accessor != null) {
					accessor.release(r);
					multiReaderAccessors.remove(r);
				}
			}
		}
		subReaderList.remove(reader);
	}

	public void release(IndexWriter writer) {
		throw new UnsupportedOperationException();
	}

	public int searcherUseCount() {
		int usecount = 0;
		IndexAccessorFactory factory = IndexAccessorFactory.getInstance();
		for (Directory index : this.dirs) {
			IndexAccessor indexAccessor = factory.getAccessor(index);
			usecount += indexAccessor.searcherUseCount();
		}
		return usecount;
	}

	public int writerUseCount() {
		int usecount = 0;
		IndexAccessorFactory factory = IndexAccessorFactory.getInstance();
		for (Directory index : this.dirs) {
			IndexAccessor indexAccessor = factory.getAccessor(index);
			usecount += indexAccessor.writerUseCount();
		}
		return usecount;
	}

	public int writingReadersUseCount() {
		int usecount = 0;
		IndexAccessorFactory factory = IndexAccessorFactory.getInstance();
		for (Directory index : this.dirs) {
			IndexAccessor indexAccessor = factory.getAccessor(index);
			usecount += indexAccessor.writingReadersUseCount();
		}
		return usecount;
	}

	public void reopen() throws IOException {
		IndexAccessorFactory factory = IndexAccessorFactory.getInstance();
		for (Directory index : this.dirs) {
			IndexAccessor indexAccessor = factory.getAccessor(index);
			indexAccessor.reopen();
		}
	}

}
