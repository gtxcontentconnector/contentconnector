package com.gentics.cr.lucene.indexer.transformer.html;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.w3c.tidy.Tidy;

/**
 * 
 * Last changed: $Date: 2009-06-24 17:10:19 +0200 (Mi, 24 Jun 2009) $
 * @version $Revision: 99 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class TidyHelper {

	/**
	 * Tidy a HTML Stream.
	 * @param is
	 */
	public static Reader tidy(InputStream is) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Tidy tidy = new Tidy();
		tidy.parse(is, out);
		return new InputStreamReader(new ByteArrayInputStream(out.toByteArray()));
	}
}
