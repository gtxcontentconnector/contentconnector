package com.gentics.cr.lucene.indexaccessor;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.	See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.	You may obtain a copy of the License at
 *
 *		 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.LogManager;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

import com.gentics.lib.log.NodeLogger;

/**
 * An IndexAccessorFactory allows the sharing of IndexAccessors and
 * MultiIndexAccessors across threads.
 * 
 * 
 * Last changed: $Date: 2009-09-02 17:57:48 +0200 (Mi, 02 Sep 2009) $
 * @version $Revision: 180 $
 * @author $Author: supnig@constantinopel.at $
 */
public class IndexAccessorFactory {

	/**
	 * Log4j logger for debug and error messages.
	 */
	private static final NodeLogger LOGGER = NodeLogger.getNodeLogger(IndexAccessorFactory.class);

	/**
	 * Holds an single instance of {@link IndexAccessorFactory} to give it to
	 * others who want to read a lucene index.
	 */
	private static final IndexAccessorFactory INDEXACCESSORFACTORY = new IndexAccessorFactory();

	private ConcurrentHashMap<Directory, IndexAccessor> indexAccessors = new ConcurrentHashMap<Directory, IndexAccessor>();

	private Vector<IndexAccessorToken> consumer = new Vector<IndexAccessorToken>();

	/**
	 * boolean mark for indicating {@link IndexAccessorFactory} was closed before.
	 */
	private static boolean wasClosed = false;

	static {
		LogManager manager = LogManager.getLogManager();
		InputStream is = ClassLoader.getSystemResourceAsStream("logger.properties");

		if (is != null) {
			try {
				manager.readConfiguration(is);
			} catch (SecurityException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static IndexAccessorFactory getInstance() {
		return INDEXACCESSORFACTORY;
	}

	private IndexAccessorFactory() {
		// prevent instantiation.
	}

	public synchronized IndexAccessorToken registerConsumer(String name) {
		IndexAccessorToken token = new IndexAccessorToken(name);
		LOGGER.debug("Adding Consumer: " + token);
		this.consumer.add(token);
		return token;
	}

	public synchronized void releaseConsumer(final IndexAccessorToken token) {
		this.consumer.remove(token);
		LOGGER.debug("Releasing Consumer: " + token.getName() + ", Size: " + consumer.size());
		if (this.consumer.size() == 0) {
			close();
		}
	}

	/**
	 * Closes all of the open IndexAccessors and releases any open resources.
	 */
	private synchronized void close() {
		if (!wasClosed) {
			for (IndexAccessor accessor : indexAccessors.values()) {
				if (accessor.isOpen()) {
					accessor.close();
				}
			}
			indexAccessors.clear();
			wasClosed = true;
			if (LOGGER.isDebugEnabled()) {
				try {
					throw new Exception("Closing index accessory factory lucene search is now disabled.");
				} catch (Exception e) {
					LOGGER.debug("IndexAccessorFactory is now closed.", e);
				}
			}
		}
	}

	/**
	 * Allows further index access if new accessors are created.
	 * IndexAccessorFactory would only be useful once but after closing it becomes useless. 
	 */
	public synchronized void reopen() {
		wasClosed = false;
	}

	public void createAccessor(final Directory dir, final Analyzer analyzer) throws IOException {
		createAccessor(dir, analyzer, null, null);
	}

	public void createAccessor(final Directory dir, final Analyzer analyzer, final Query query) throws IOException {
		createAccessor(dir, analyzer, query, null);
	};

	private void createAccessor(final Directory dir, final Analyzer analyzer, final Query query, final Set<Sort> sortFields)
			throws IOException {
		IndexAccessor accessor = null;
		if (query != null) {
			accessor = new WarmingIndexAccessor(dir, analyzer, query);
		} else {
			accessor = new DefaultIndexAccessor(dir, analyzer);
		}
		accessor.open();

		if (dir.listAll().length == 0) {
			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_9, analyzer);
			IndexWriter indexWriter = new IndexWriter(dir, config);
			indexWriter.close();
		}

		IndexAccessor existingAccessor = indexAccessors.putIfAbsent(dir, accessor);
		if (existingAccessor != null) {
			accessor.close();
			throw new IllegalStateException("IndexAccessor already exists: " + dir);
		}

	}

	/**
	 * Get an {@link IndexAccessor} for the specified {@link Directory}.
	 * @param indexDir {@link Directory} to get the {@link IndexAccessor} for.
	 * @return {@link IndexAccessor} for the {@link Directory}.
	 */
	public IndexAccessor getAccessor(final Directory indexDir) throws AlreadyClosedException {
		if (wasClosed) {
			throw new AlreadyClosedException("IndexAccessorFactory was already closed" + ". Maybe there is a shutdown in progress.");
		}
		IndexAccessor indexAccessor = indexAccessors.get(indexDir);
		if (indexAccessor == null) {
			throw new IllegalStateException("Requested Accessor does not exist");
		}
		return indexAccessor;
	}

	/**
	 * Check if an Accessor is already created for that directory.
	 * @param indexDir directory in which contains the index file
	 * @return boolean true if present, false if not
	 */
	public boolean hasAccessor(final Directory indexDir) {
		IndexAccessor indexAccessor = indexAccessors.get(indexDir);
		if (indexAccessor == null) {
			return false;
		}
		return true;
	}

	/**
	 * @param dirs 
	 */
	public IndexAccessor getMultiIndexAccessor(final Directory[] dirs) {
		IndexAccessor multiIndexAccessor = new DefaultMultiIndexAccessor(dirs);

		return multiIndexAccessor;
	}

	public static void destroy() {
		getInstance().close();
	}
}
