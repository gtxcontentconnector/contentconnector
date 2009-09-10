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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.store.Directory;

/**
 * Default MultiIndexAccessor implementation.
 * 
 * Last changed: $Date: 2009-09-02 17:57:48 +0200 (Mi, 02 Sep 2009) $
 * @version $Revision: 180 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class DefaultMultiIndexAccessor implements MultiIndexAccessor {

  private final static Logger logger = Logger.getLogger(DefaultMultiIndexAccessor.class.getName());

  private final Map<Searcher, IndexAccessor> multiSearcherAccessors = new HashMap<Searcher, IndexAccessor>();

  private Similarity similarity;

  /**
   * Create new Instance
   */
  public DefaultMultiIndexAccessor() {
    this.similarity = Similarity.getDefault();
  }

  /**
   * Create new instance
   * @param similarity
   */
  public DefaultMultiIndexAccessor(Similarity similarity) {
    this.similarity = similarity;
  }


  /*
   * (non-Javadoc)
   * 
   * @see com.mhs.indexaccessor.MultiIndexAccessor#getMultiSearcher(org.apache.lucene.search.Similarity,
   *      java.util.Set, org.apache.lucene.index.IndexReader)
   */
  public synchronized Searcher getMultiSearcher(Set<Directory> indexes) throws IOException {

    if (logger.isLoggable(Level.FINE)) {
      logger.fine("opening new multi searcher");
    }

    Searcher[] searchers = new Searcher[indexes.size()];
    Iterator<Directory> it = indexes.iterator();
    IndexAccessorFactory factory = IndexAccessorFactory.getInstance();

    for (int i = 0; i < searchers.length; i++) {
      Directory index = it.next();
      IndexAccessor indexAccessor = factory.getAccessor(index);
      searchers[i] = indexAccessor.getSearcher(similarity, null);
      multiSearcherAccessors.put(searchers[i], indexAccessor);
    }

    MultiSearcher multiSearcher = new MultiSearcher(searchers);

    return multiSearcher;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.mhs.indexaccessor.MultiIndexAccessor#release(org.apache.lucene.search.Searcher)
   */
  public synchronized void release(Searcher multiSearcher) {
    Searchable[] searchers = ((MultiSearcher) multiSearcher).getSearchables();
    for (Searchable searchable : searchers) {
      multiSearcherAccessors.remove(searchable).release((Searcher) searchable);
    }
  }

}
