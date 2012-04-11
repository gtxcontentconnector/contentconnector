package com.gentics.cr.lucene.indexer.transformer.test;

public class TUtil {

	public static String normalizeCRLF(String string) {

		String ret = string;
		if (ret != null) {
			ret = ret.replaceAll("\\r\\n", "\n");
			ret = ret.replaceAll("\\r", "\n");
		}
		return ret;
	}

}
