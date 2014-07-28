package com.gentics.cr.lucene.analysis;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

import com.gentics.cr.lucene.LuceneVersion;

/**
 * Analyzer that does all the standard tokenizing and filtering and converts
 * umlauts to their two letter representation.
 * @author christopher
 *
 */
public final class BasicUmlautAnalyzer extends Analyzer {

	/**
	 * Stop word set.
	 */
	private final CharArraySet stopWords;

	/**
	 * Lucene version to use.
	 */
	private final Version matchVersion;

	/**
	 * Creates a new instance of the BasicUmlautAnalyzer with the default lucene version and an extended english stop word set
	 */
	public BasicUmlautAnalyzer() {
		this(LuceneVersion.getVersion(), StopWords.EXTENDED_ENGLISH_STOP_WORDS);
	}
	
	/**
	 * Creates a new instance of the BasicUmlautAnalyzer with the given lucene version and the given stop word set.
	 * @param matchVersion
	 * @param stopWords
	 */
	public BasicUmlautAnalyzer(Version matchVersion, CharArraySet stopWords) {
		
		if (stopWords != null && stopWords.size() == 0) {
			stopWords = null;
		}

		this.stopWords = stopWords;
		this.matchVersion = matchVersion;
	}


	@Override
	protected TokenStreamComponents createComponents(String fieldName,
			Reader reader) {
		final Tokenizer source = new StandardTokenizer(matchVersion, reader);
		TokenStream result = new StandardFilter(matchVersion, source);
		result = new LowerCaseFilter(matchVersion, result);
		result = new StopFilter(matchVersion, result, stopWords);
		result = new UmlautFilter(matchVersion, result);
		return new TokenStreamComponents(source, result);
	}
	
	private class UmlautFilter extends TokenFilter {
		Version luceneVersion;
		private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

		/**
		 * Create a new instance of UmlautFilter
		 * @param matchVersion
		 * @param input
		 */
		protected UmlautFilter(Version matchVersion, TokenStream input) {
			super(input);
			luceneVersion = matchVersion;
		}

		@Override
		public boolean incrementToken() throws IOException {
			if (input.incrementToken()) {
				
				char[] buffer = termAtt.buffer();
				int length = termAtt.length();
				for (int i = 0; i < length;i++) {
					final char c = buffer[i];
					switch(c) {
						case 'ß':
							i = replace(new char[]{'s','z'},i,buffer,termAtt,length);
							length = termAtt.length();
							break;
						case 'ä':
							i = replace(new char[]{'a','e'},i,buffer,termAtt,length);
							length = termAtt.length();
							break;
						case 'ö':
							i = replace(new char[]{'o','e'},i,buffer,termAtt,length);
							length = termAtt.length();
							break;
						case 'ü':
							i = replace(new char[]{'u','e'},i,buffer,termAtt,length);
							length = termAtt.length();
							break;
					}
				}
				return true;
			} else {
				return false;
			}
		}
		
		/**
		 * Do the actual replacing in the buffer
		 * @param replace
		 * @param i
		 * @param buffer
		 * @param termAtt
		 * @param length
		 */
		private int replace(char[] replace, int i, char[] buffer, CharTermAttribute termAtt, int length) {
			buffer = termAtt.resizeBuffer((replace.length - 1)+length);
			if (i < length) {
				System.arraycopy(buffer, i+1, buffer, i+replace.length, (length-i));
			}
			int counter = i;
			for (char c : replace) {
				buffer[counter++] = c;
			}
			length += (replace.length - 1);
			termAtt.setLength(length);
			return i + replace.length - 1;
		}
		
	}

}
