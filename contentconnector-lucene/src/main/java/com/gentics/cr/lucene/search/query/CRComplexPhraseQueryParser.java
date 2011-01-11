package com.gentics.cr.lucene.search.query;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.complexPhrase.ComplexPhraseQueryParser;
import org.apache.lucene.util.Version;

import com.gentics.cr.CRRequest;
/**
 * Wrapper for ComplexPhraseQueryParser.
 * @author Christopher
 */
public class CRComplexPhraseQueryParser extends ComplexPhraseQueryParser {

	
	/**
	   * initialize a CRQeryParser with multiple search attributes.
	   * @param version version of lucene
	   * @param searchedAttributes attributes to search in
	   * @param analyzer analyzer for index
	   */
	  public CRComplexPhraseQueryParser(final Version version,
			  final String[] searchedAttributes, final Analyzer analyzer) {
		  super(version, searchedAttributes[0], analyzer);
	  }
	  
	 
	  /**
	   * initialize a CRQeryParser with multiple search attributes.
	   * @param version version of lucene
	   * @param searchedAttributes attributes to search in
	   * @param analyzer analyzer for index
	   * @param crRequest request to get additional parameters from.
	   */
	  public CRComplexPhraseQueryParser(final Version version,
			final String[] searchedAttributes, final Analyzer analyzer,
			final CRRequest crRequest) {
		  this(version, searchedAttributes, analyzer);
	  }

}
