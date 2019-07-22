package com.gentics.cr.lucene.search.collector;

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

import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.PriorityQueue;

final class LanguageSortingHitQueue extends PriorityQueue<LanguageSortingScoreDoc> {
	private SortField[] sorts;
	private boolean doSortFields = true;
  /**
   * Creates a new instance with <code>size</code> elements. If
   * <code>prePopulate</code> is set to true, the queue will pre-populate itself
   * with sentinel objects and set its {@link #size()} to <code>size</code>. In
   * that case, you should not rely on {@link #size()} to get the number of
   * actual elements that were added to the queue, but keep track yourself.<br>
   * <b>NOTE:</b> in case <code>prePopulate</code> is true, you should pop
   * elements from the queue using the following code example:
   * 
   * <pre>
   * PriorityQueue pq = new HitQueue(10, true); // pre-populate.
   * ScoreDoc top = pq.top();
   * 
   * // Add/Update one element.
   * top.score = 1.0f;
   * top.doc = 0;
   * top = (ScoreDoc) pq.updateTop();
   * int totalHits = 1;
   * 
   * // Now pop only the elements that were *truly* inserted.
   * // First, pop all the sentinel elements (there are pq.size() - totalHits).
   * for (int i = pq.size() - totalHits; i &gt; 0; i--) pq.pop();
   * 
   * // Now pop the truly added elements.
   * ScoreDoc[] results = new ScoreDoc[totalHits];
   * for (int i = totalHits - 1; i &gt;= 0; i--) {
   *   results[i] = (ScoreDoc) pq.pop();
   * }
   * </pre>
   * 
   * <p><b>NOTE</b>: This class pre-allocate a full array of
   * length <code>size</code>.
   * 
   * @param size
   *          the requested size of this queue.
   * @param sort 
   *          Sorting
   * @param prePopulate
   *          specifies whether to pre-populate the queue with sentinel values.
   * @see #getSentinelObject()
   */
  LanguageSortingHitQueue(int size, Sort sort, boolean prePopulate) {
	super(size,prePopulate);
	if (sort == null) {
		this.doSortFields = false;
	} else {
		this.sorts = sort.getSort();
	}
  }

  // Returns null if prePopulate is false.
  @Override
  protected LanguageSortingScoreDoc getSentinelObject() {
    // Always set the doc Id to MAX_VALUE so that it won't be favored by
    // lessThan. This generally should not happen since if score is not NEG_INF,
    // TopScoreDocCollector will always add the object to the queue.
    return new LanguageSortingScoreDoc(Integer.MAX_VALUE, Float.NEGATIVE_INFINITY, null, null , true);
  }
  
  @Override
  protected final boolean lessThan(LanguageSortingScoreDoc hitA, LanguageSortingScoreDoc hitB) {
	if (hitA.sentinel) {
		return true;
	} else if (doSortFields && hitA.sortvalue != null && hitB.sortvalue != null) {
    	return compare(hitA.sortvalue, hitB.sortvalue, 0) > 0;
    } else if (hitA.score == hitB.score) {
      return hitA.doc > hitB.doc; 
  	}
    return hitA.score < hitB.score;
  }
  
  public String toString() {
	StringBuilder sb = new StringBuilder();
	Object[] heap = this.getHeapArray();
	for (Object o : heap) {
		if (o instanceof LanguageSortingScoreDoc) {
			LanguageSortingScoreDoc sd = (LanguageSortingScoreDoc) o;
			if (sd.sortvalue!=null) {
				sb.append("{("+sd.sortvalue.toString()+")");
				for (Entry<String, BytesRef> e : sd.sortvalue.entrySet()) {
					sb.append(e.getValue().utf8ToString());
				}
				sb.append("}");
			}
		}
	}
	  return sb.toString();
  }
  
  private final int compare(HashMap<String,BytesRef> sortvalueA, HashMap<String,BytesRef> sortvalueB, int sortpos) {
	  if (sortpos >= this.sorts.length) {
		  return 0;
	  }
	  SortField sf = this.sorts[sortpos];
	  if (sf == null) {
		  return 0;
	  }
	  BytesRef refA = sortvalueA.get(sf.getField());
	  BytesRef refB = sortvalueB.get(sf.getField());
	  int ret = 0;
	  if (refA == null) {
		  return -1;
	  } else  if (refB == null) {
		  return 1;
	  } else {
		  ret = sf.getBytesComparator().compare(refA, refB);
	  }
	  
	  if (ret == 0) {
		  return compare(sortvalueA, sortvalueB, sortpos + 1);
	  }
	  if (sf.getReverse()) {
		  ret *= -1;
	  }
	  return ret;
  }
}