package com.gentics.cr.lucene;

import org.apache.lucene.util.Version;

/**
 * 
 * Last changed: $Date: 2010-03-17 16:56:19 +0100 (Mi, 17 Mär 2010) $
 * @version $Revision: 508 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class LuceneVersion {

	/**
	 * Returns the current Lucene Version used in this implementation.
	 */
	public static Version getVersion() {
		return Version.LUCENE_4_9;
	}
}
