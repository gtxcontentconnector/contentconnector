package com.gentics.cr.lucene.autocomplete;

import java.io.Reader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.ASCIIFoldingFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter.Side;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

import com.gentics.cr.lucene.LuceneVersion;

public class AutocompleteAnalyzer extends Analyzer{

	private static final String[] ENGLISH_STOP_WORDS = { 
	    "a", "an", "and", "are", "as", "at", "be", "but", "by", 
	    "for", "i", "if", "in", "into", "is", 
	    "no", "not", "of", "on", "or", "s", "such", 
	    "t", "that", "the", "their", "then", "there", "these", 
	    "they", "this", "to", "was", "will", "with" 
	    };
	
	
	@Override
	public TokenStream tokenStream(String fieldName,Reader reader) { 
        TokenStream result = new StandardTokenizer(LuceneVersion.getVersion(),reader); 
        result = new StandardFilter(result); 
        result = new LowerCaseFilter(result); 
        result = new ASCIIFoldingFilter(result); 
        List<String> list = Arrays.asList(ENGLISH_STOP_WORDS);
        Set<String> set = new HashSet<String>(list);
        result = new StopFilter(false,result,set,true); 
        result = new EdgeNGramTokenFilter(result, Side.FRONT,1, 20); 
        return result; 
	} 

}
