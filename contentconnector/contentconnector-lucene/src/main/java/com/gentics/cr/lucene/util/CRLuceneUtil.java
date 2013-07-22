package com.gentics.cr.lucene.util;

import java.util.List;
import java.util.Vector;

import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.util.ReaderUtil;

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
	public static List<String> getFieldNames(IndexReader reader) {
		
		List<String> nameList = new Vector<String>();
		for(FieldInfo info : ReaderUtil.getMergedFieldInfos(reader)) {
			nameList.add(info.name);
		}
		return nameList;
	}
}
