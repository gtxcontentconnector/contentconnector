package com.gentics.cr.test.lucene.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
/**
 * 
 * 
 * Last changed: $Date: 2010-01-12 19:10:48 +0100 (Di, 12 Jän 2010) $
 * @version $Revision: 390 $
 * @author $Author: bigbear.ap $
 *
 */
public final class TestFilter extends TokenFilter {
	  
	/**
	 * create new Test Filter
	 * @param in
	 */
	  public TestFilter(TokenStream in) {
	    super(in);
	    termAtt = addAttribute(TermAttribute.class);
	  }

	  private TermAttribute termAtt;
	  
	  @Override
	  public final boolean incrementToken() throws IOException {
	    if (input.incrementToken()) {

	      final char[] buffer = termAtt.termBuffer();
	      final int length = termAtt.termLength();
	      for(int i=0;i<length;i++)
	        buffer[i] = '-';

	      return true;
	    } else
	      return false;
	  }
	}
