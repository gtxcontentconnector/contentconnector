package com.gentics.cr.lucene.util;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexReader;

public class CRLuceneUtil {

	/**
	 * Utility class.
	 */
	private CRLuceneUtil() {
		
	}
	
	/**
	 * Get the list of field names from an index reader.
	 * @param reader
	 * @return
	 */
	public static Set<String> getFieldNames(IndexReader reader) {
		Set<String> nameSet = new TreeSet<String>();
		for(AtomicReaderContext aRC : reader.leaves()) {
			AtomicReader aReader = aRC.reader();
			for(FieldInfo info : aReader.getFieldInfos()) {
				nameSet.add(info.name);
			}
		}
		
		return nameSet;
	}
}
