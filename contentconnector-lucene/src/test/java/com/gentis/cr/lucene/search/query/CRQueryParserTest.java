package com.gentis.cr.lucene.search.query;

import junit.framework.TestCase;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.ParseException;

import com.gentics.cr.CRRequest;
import com.gentics.cr.lucene.LuceneVersion;
import com.gentics.cr.lucene.search.query.CRQueryParser;

public class CRQueryParserTest extends TestCase {
	private static final StandardAnalyzer STANDARD_ANALYZER = new StandardAnalyzer(LuceneVersion.getVersion());
	private static final String[] SEARCHED_ATTRIBUTES = new String[]{"content", "binarycontent"};
	private CRQueryParser parser;
	private CRRequest crRequest;
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		parser = new CRQueryParser(
					LuceneVersion.getVersion(),
					SEARCHED_ATTRIBUTES,
					STANDARD_ANALYZER);
		crRequest = new CRRequest();
	}
	
	public void testReplaceBooleanMnoGoSearchQuery() throws ParseException {
		assertEquals("(content:test1 binarycontent:test1) (content:test2 binarycontent:test2)", parser.parse("test1 | test2").toString());
		assertEquals("+(content:test1 binarycontent:test1) +(content:test2 binarycontent:test2)", parser.parse("test1 & test2").toString());
		assertEquals("+(content:test1 binarycontent:test1) +(content:test2 binarycontent:test2)", parser.parse("test1&test2").toString());
	}
	public void testSearchAttributes() throws ParseException {
		assertEquals("content:test1 binarycontent:test1", parser.parse("test1").toString());
	}
	public void testWordMatch() throws ParseException {
		crRequest.set(CRRequest.WORDMATCH_KEY, "sub");
		parser = new CRQueryParser(LuceneVersion.getVersion(), SEARCHED_ATTRIBUTES, STANDARD_ANALYZER, crRequest);
		assertEquals("content:*test1* binarycontent:*test1*", parser.parse("test1").toString());
		crRequest.set(CRRequest.WORDMATCH_KEY, "beg");
		parser = new CRQueryParser(LuceneVersion.getVersion(), SEARCHED_ATTRIBUTES, STANDARD_ANALYZER, crRequest);
		assertEquals("content:test1* binarycontent:test1*", parser.parse("test1").toString());
		crRequest.set(CRRequest.WORDMATCH_KEY, "end");
		parser = new CRQueryParser(LuceneVersion.getVersion(), SEARCHED_ATTRIBUTES, STANDARD_ANALYZER, crRequest);
		assertEquals("content:*test1 binarycontent:*test1", parser.parse("test1").toString());
		crRequest.set(CRRequest.WORDMATCH_KEY, "wrd");
		parser = new CRQueryParser(LuceneVersion.getVersion(), SEARCHED_ATTRIBUTES, STANDARD_ANALYZER, crRequest);
		assertEquals("content:test1 binarycontent:test1", parser.parse("test1").toString());
	}
	
}
