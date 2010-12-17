package com.gentics.cr.lucene.analysis;

import org.apache.lucene.analysis.snowball.SnowballAnalyzer;

import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.LuceneVersion;
/**
 * 
 * 
 * Last changed: $Date: 2010-01-12 19:10:48 +0100 (Di, 12 Jän 2010) $
 * @version $Revision: 390 $
 * @author $Author: bigbear.ap $
 *
 */
public class WrappedSnowballAnalyzer extends SnowballAnalyzer {
	private static final String STEMMER_NAME_KEY = "stemmername";
	/**
	 * Creates a SnowballAnalyzer with the configured stemmer name
	 * @param config 
	 */
	public WrappedSnowballAnalyzer(GenericConfiguration config) {
		super(LuceneVersion.getVersion(), config.getString(STEMMER_NAME_KEY));
	}

}
