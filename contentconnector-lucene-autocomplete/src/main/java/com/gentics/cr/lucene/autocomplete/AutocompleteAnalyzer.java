package com.gentics.cr.lucene.autocomplete;

import java.io.Reader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter.Side;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;

import com.gentics.cr.lucene.LuceneVersion;

public final class AutocompleteAnalyzer extends Analyzer {

	private static final CharArraySet ENGLISH_STOP_WORDS = new CharArraySet(LuceneVersion.getVersion(),50,true);
	
	static {
		ENGLISH_STOP_WORDS.addAll(Arrays.asList(new String[]{ 
			"a", "an", "and", "are", "as", "at", "be", "but", "by", "for",
			"i", "if", "in", "into", "is", "no", "not", "of", "on", "or", "s", "such", "t", "that", "the", "their",
			"then", "there", "these", "they", "this", "to", "was", "will", "with" }));
	}
	
	@Override
	protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
		Tokenizer result = new StandardTokenizer(LuceneVersion.getVersion(), reader);
		TokenFilter filter = new StandardFilter(LuceneVersion.getVersion(), result);
		filter = new LowerCaseFilter(LuceneVersion.getVersion(), filter);
		filter = new ASCIIFoldingFilter(result);
		filter = new StopFilter(LuceneVersion.getVersion(),filter, ENGLISH_STOP_WORDS);
		filter = new EdgeNGramTokenFilter(LuceneVersion.getVersion(), filter, EdgeNGramTokenFilter.Side.FRONT, 1, 20);
		return new TokenStreamComponents(result, filter);
	}

}
