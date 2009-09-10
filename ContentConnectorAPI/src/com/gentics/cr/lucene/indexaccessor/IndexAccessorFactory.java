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
import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.LogManager;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.store.Directory;

/**
 * An IndexAccessorFactory allows the sharing of IndexAccessors and
 * MultiIndexAccessors across threads.
 * 
 * 
 * Last changed: $Date: 2009-09-02 17:57:48 +0200 (Mi, 02 Sep 2009) $
 * @version $Revision: 180 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class IndexAccessorFactory {
  private static final IndexAccessorFactory indexAccessorFactory = new IndexAccessorFactory();
  private ConcurrentHashMap<Directory, IndexAccessor> indexAccessors = new ConcurrentHashMap<Directory, IndexAccessor>();
  private MultiIndexAccessor multiIndexAccessor = new DefaultMultiIndexAccessor();

  static {
    LogManager manager = LogManager.getLogManager();
    InputStream is = ClassLoader.getSystemResourceAsStream(
        "logger.properties");

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

  /**
   * @return
   */
  public static IndexAccessorFactory getInstance() {
    return indexAccessorFactory;
  }

  private IndexAccessorFactory() {
    // prevent instantiation.
  }

  /**
   * Closes all of the open IndexAccessors and releases any open resources.
   */
  public void close() {
    synchronized (indexAccessors) {
      for (IndexAccessor accessor : indexAccessors.values()) {
        accessor.close();
      }
      indexAccessors.clear();
    }
  }
  /**
   * 
   * @param dir
   * @param analyzer
   * @throws IOException
   */
  public void createAccessor(Directory dir, Analyzer analyzer) throws IOException {
    createAccessor(dir, analyzer, null, null);
  }
  /**
   * 
   * @param dir
   * @param analyzer
   * @param query
   * @throws IOException
   */
  public void createAccessor(Directory dir, Analyzer analyzer, Query query) throws IOException {
    createAccessor(dir, analyzer, query, null);
  };

  private void createAccessor(Directory dir, Analyzer analyzer, Query query, Set<Sort> sortFields)
      throws IOException {
    IndexAccessor accessor = null;
    if(query != null) {
    	accessor = new WarmingIndexAccessor(dir, analyzer, query);
    }else {
    	accessor = new DefaultIndexAccessor(dir, analyzer);
    }
    accessor.open();

    if (dir.list().length == 0) {
      IndexWriter indexWriter = new IndexWriter(dir, null, true, IndexWriter.MaxFieldLength.UNLIMITED);
      indexWriter.close();
    }

    IndexAccessor existingAccessor = indexAccessors.putIfAbsent(dir, accessor);
    if (existingAccessor != null) {
      throw new IllegalStateException("IndexAccessor already exists: " + dir);
    }

  }

  /**
   * @param indexDir
   * @return
   */
  public IndexAccessor getAccessor(Directory indexDir) {

    IndexAccessor indexAccessor = indexAccessors.get(indexDir);
    if (indexAccessor == null) {
      throw new IllegalStateException("Requested Accessor does not exist");
    }
    return indexAccessor;

  }

  /**
   * @return
   */
  public MultiIndexAccessor getMultiIndexAccessor() {
    return multiIndexAccessor;
  }
}
