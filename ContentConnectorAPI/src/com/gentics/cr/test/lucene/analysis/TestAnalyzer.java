package com.gentics.cr.test.lucene.analysis;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;
/**
 * 
 * 
 * Last changed: $Date: 2010-01-12 19:10:48 +0100 (Di, 12 Jän 2010) $
 * @version $Revision: 390 $
 * @author $Author: bigbear.ap $
 *
 */
public class TestAnalyzer extends Analyzer {

	@Override
	public TokenStream tokenStream(String arg0, Reader reader) {
		Tokenizer tokenizer = new StandardTokenizer(Version.LUCENE_CURRENT,reader);
		TokenStream stream = new StandardFilter(tokenizer);
		TokenStream f_stream = new TestFilter(stream);
		return f_stream;
	}

}
