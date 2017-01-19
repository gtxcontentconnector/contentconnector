package com.gentics.cr.lucene.search.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryparser.classic.ParseException;

import com.gentics.cr.CRRequest;
import com.gentics.cr.lucene.AbstractLuceneTest;
import com.gentics.cr.lucene.LuceneVersion;
import com.gentics.cr.lucene.search.query.mocks.ComparableDocument;
import com.gentics.cr.lucene.search.query.mocks.SimpleLucene;

public class CRQueryParserTest extends AbstractLuceneTest {

	public CRQueryParserTest(String name) {
		super(name);
	}

	private static final StandardAnalyzer STANDARD_ANALYZER = new StandardAnalyzer(LuceneVersion.getVersion(),
			CharArraySet.EMPTY_SET);
	private static final String[] SEARCHED_ATTRIBUTES = new String[] { SimpleLucene.CONTENT_ATTRIBUTE, "binarycontent" };
	private CRQueryParser parser;
	private CRRequest crRequest;
	private SimpleLucene lucene;
	private ArrayList<ComparableDocument> documents;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		parser = new CRQueryParser(LuceneVersion.getVersion(), SEARCHED_ATTRIBUTES, STANDARD_ANALYZER);
		crRequest = new CRRequest();
		lucene = new SimpleLucene();

		documents = new ArrayList<ComparableDocument>();
		/* 0 */documents.add(new ComparableDocument(lucene.add(
			SimpleLucene.CONTENT_ATTRIBUTE + ":word9 word1",
			"node_id:1")));
		/* 1 */documents.add(new ComparableDocument(lucene.add(
			SimpleLucene.CONTENT_ATTRIBUTE + ":word2 word9",
			"node_id:1")));
		/* 2 */documents.add(new ComparableDocument(lucene.add(
			SimpleLucene.CONTENT_ATTRIBUTE + ":word3",
			"binarycontent:word9",
			"node_id:2")));
		/* 3 */documents.add(new ComparableDocument(lucene.add(SimpleLucene.CONTENT_ATTRIBUTE
				+ ":wörd4 with-minusinit with-minus-in-it", "node_id:2")));
		/* 4 */documents.add(new ComparableDocument(lucene.add(
			SimpleLucene.CONTENT_ATTRIBUTE + ":word5 minusinit with",
			"updatetimestamp:1311604678",
			"edittimestamp:1311604678",
			"node_id:3"))); //25.07.2011 16:37:58
		/* 5 */documents.add(new ComparableDocument(lucene.add(
			SimpleLucene.CONTENT_ATTRIBUTE + ":word6",
			"updatetimestamp:1313160620",
			"edittimestamp:1313160620",
			"node_id:3"))); //12.08.2011 16:50:20
		/* 6 */documents.add(new ComparableDocument(lucene.add(
			SimpleLucene.CONTENT_ATTRIBUTE + ":word7",
			"updatetimestamp:1314627329",
			"edittimestamp:1314627329",
			"node_id:3"))); //29.08.2011 16:15:29
		/* 7 */documents.add(new ComparableDocument(lucene.add(
			SimpleLucene.CONTENT_ATTRIBUTE + ":word8 01",
			"updatetimestamp:1304510397",
			"edittimestamp:1304510397",
			"node_id:3"))); //04.05.2011 13:59:57
		/* 8 */documents.add(new ComparableDocument(lucene.add(
			SimpleLucene.CONTENT_ATTRIBUTE + ":newword 01/23456789",
			"node_id:11")));

	}

	public void testReplaceBooleanMnoGoSearchQuery() throws ParseException, CorruptIndexException, IOException {
		String[] escapedQueries = new String[]{
			"word1 \\& word9", "word1\\&word9", "word1\\& \\&word9",
			"word1 \\| word9", "word1\\|word9", "word1\\| \\|word9",
			" \\~Text"
		};
		for (String escapedQuery : escapedQueries) {
			assertEquals("Escaped special chars '&', '|' and '~ should never be replaced", escapedQuery, parser.replaceBooleanMnoGoSearchQuery(escapedQuery));
		}

		Map<String, String> unescapedQueries = new HashMap<>();
		unescapedQueries.put("word1 & word9", "word1 AND word9");
		unescapedQueries.put("word1&word9", "word1 AND word9");
		unescapedQueries.put("word1 | word9", "word1 OR word9");
		unescapedQueries.put("word1|word9", "word1 OR word9");
		unescapedQueries.put(" ~word0123456789üöäÜÖÄß", " NOT word0123456789üöäÜÖÄß");
		for (String unescapedQuery : unescapedQueries.keySet()) {
			assertEquals(
				"Special chars '&', '|' and '~' should be replaced with ' AND ', ' OR ' or ' NOT ' for query: " + unescapedQuery,
				unescapedQueries.get(unescapedQuery),
				parser.replaceBooleanMnoGoSearchQuery(unescapedQuery)
			);
		}

		Collection<ComparableDocument> matchedDocuments = wrapComparable(lucene.find(parser.parse("word1 | word2")));
		containsAll(matchedDocuments, new ComparableDocument[] { documents.get(0), documents.get(1) });

		matchedDocuments = wrapComparable(lucene.find(parser.parse("word1 & word9")));
		containsOnly(matchedDocuments, documents.get(0));

		matchedDocuments = wrapComparable(lucene.find(parser.parse("word1&word9")));
		containsOnly(matchedDocuments, documents.get(0));
	}

	public void testSearchAttributes() throws ParseException, CorruptIndexException, IOException {
		Collection<ComparableDocument> matchedDocuments = wrapComparable(lucene.find(parser.parse("word1")));
		containsOnly(matchedDocuments, documents.get(0));
	}

	public void testWordMatchSub() throws ParseException, CorruptIndexException, IOException {
		crRequest.set(CRRequest.WORDMATCH_KEY, "sub");
		parser = new CRQueryParser(LuceneVersion.getVersion(), SEARCHED_ATTRIBUTES, STANDARD_ANALYZER, crRequest);
		Collection<ComparableDocument> matchedDocuments = wrapComparable(lucene.find(parser.parse("word")));
		containsAll(matchedDocuments, new ComparableDocument[] { documents.get(0), documents.get(1), documents.get(2),
				documents.get(4), documents.get(5), documents.get(6), documents.get(7), documents.get(8) });
	}

	public void testWordMatchBeg() throws ParseException, CorruptIndexException, IOException {
		crRequest.set(CRRequest.WORDMATCH_KEY, "beg");
		parser = new CRQueryParser(LuceneVersion.getVersion(), SEARCHED_ATTRIBUTES, STANDARD_ANALYZER, crRequest);
		Collection<ComparableDocument> matchedDocuments = wrapComparable(lucene.find(parser.parse("word")));
		containsAll(matchedDocuments, new ComparableDocument[] { documents.get(0), documents.get(1), documents.get(2),
				documents.get(4), documents.get(5), documents.get(6), documents.get(7) });
	}

	public void testWordMatchEnd() throws ParseException, CorruptIndexException, IOException {
		crRequest.set(CRRequest.WORDMATCH_KEY, "end");
		parser = new CRQueryParser(LuceneVersion.getVersion(), SEARCHED_ATTRIBUTES, STANDARD_ANALYZER, crRequest);
		Collection<ComparableDocument> matchedDocuments = wrapComparable(lucene.find(parser.parse("word")));
		containsOnly(matchedDocuments, documents.get(8));
	}

	public void testWordMatchWrd() throws ParseException, CorruptIndexException, IOException {
		crRequest.set(CRRequest.WORDMATCH_KEY, "wrd");
		parser = new CRQueryParser(LuceneVersion.getVersion(), SEARCHED_ATTRIBUTES, STANDARD_ANALYZER, crRequest);
		Collection<Document> matchedDocuments = lucene.find(parser.parse("word"));
		assertTrue(matchedDocuments.size() == 0);
	}

	public void testWordMatchComplexSub() throws ParseException, CorruptIndexException, IOException {
		crRequest.set(CRRequest.WORDMATCH_KEY, "sub");
		parser = new CRQueryParser(LuceneVersion.getVersion(), SEARCHED_ATTRIBUTES, STANDARD_ANALYZER, crRequest);
		Collection<ComparableDocument> matchedDocuments = wrapComparable(lucene.find(parser.parse("word AND node_id:1")));
		containsAll(matchedDocuments, new ComparableDocument[] { documents.get(0), documents.get(1) });
	}

	public void testWordMatchComplexSubMultipleTerms() throws ParseException, CorruptIndexException, IOException {
		crRequest.set(CRRequest.WORDMATCH_KEY, "sub");
		parser = new CRQueryParser(LuceneVersion.getVersion(), SEARCHED_ATTRIBUTES, STANDARD_ANALYZER, crRequest);
		Collection<ComparableDocument> matchedDocuments = wrapComparable(lucene.find(parser.parse("content:(word1 word9) AND node_id:1")));
		containsAll(matchedDocuments, new ComparableDocument[] { documents.get(0), documents.get(1) });
	}

	public void testWordMatchComplexSubGroupMultipleTerms() throws ParseException, CorruptIndexException, IOException {
		crRequest.set(CRRequest.WORDMATCH_KEY, "sub");
		parser = new CRQueryParser(LuceneVersion.getVersion(), SEARCHED_ATTRIBUTES, STANDARD_ANALYZER, crRequest);
		Collection<ComparableDocument> matchedDocuments = wrapComparable(lucene.find(parser.parse("content:\"word1 word9\" AND node_id:1")));
		assertTrue(matchedDocuments.size() == 0);
		matchedDocuments = wrapComparable(lucene.find(parser.parse("content:\"word9 word1\" AND node_id:1")));
		containsOnly(matchedDocuments, documents.get(0));
	}

	public void testWordMatchComplexSubRangeOperator() throws ParseException, CorruptIndexException, IOException {
		crRequest.set(CRRequest.WORDMATCH_KEY, "sub");
		parser = new CRQueryParser(LuceneVersion.getVersion(), SEARCHED_ATTRIBUTES, STANDARD_ANALYZER, crRequest);
		Collection<ComparableDocument> matchedDocuments = wrapComparable(lucene.find(parser
				.parse("content:word AND edittimestamp:[1304510397 TO 1314627329]")));
		containsAll(matchedDocuments, new ComparableDocument[] { documents.get(4), documents.get(5), documents.get(6),
				documents.get(7) });

		crRequest.set(CRRequest.WORDMATCH_KEY, "wrd");
		parser = new CRQueryParser(LuceneVersion.getVersion(), new String[] { "edittimestamp" }, STANDARD_ANALYZER,
				crRequest);
		matchedDocuments = wrapComparable(lucene.find(parser.parse("[1304510397 TO 1314627329]")));
		containsAll(matchedDocuments, new ComparableDocument[] { documents.get(4), documents.get(5), documents.get(6),
				documents.get(7) });

		crRequest.set(CRRequest.WORDMATCH_KEY, "sub");
		parser = new CRQueryParser(LuceneVersion.getVersion(), new String[] { "edittimestamp" }, STANDARD_ANALYZER,
				crRequest);
		matchedDocuments = wrapComparable(lucene.find(parser.parse("[1304510397 TO 1314627329]")));
		containsAll(matchedDocuments, new ComparableDocument[] { documents.get(4), documents.get(5), documents.get(6),
				documents.get(7) });
	}

	public void testWordMatchComplexSubGroupAddSign() throws CorruptIndexException, IOException, ParseException {
		crRequest.set(CRRequest.WORDMATCH_KEY, "sub");
		parser = new CRQueryParser(LuceneVersion.getVersion(), SEARCHED_ATTRIBUTES, STANDARD_ANALYZER, crRequest);
		Collection<ComparableDocument> matchedDocuments = wrapComparable(lucene.find(parser.parse("+(word1 word9)")));
		containsAll(matchedDocuments, new ComparableDocument[] { documents.get(0), documents.get(1), documents.get(2) });
	}

	public void testMinus() throws CorruptIndexException, IOException, ParseException {
		Collection<ComparableDocument> matchedDocuments = wrapComparable(lucene.find(parser.parse("with\\-minusinit")));
		containsAll(matchedDocuments, new ComparableDocument[] { documents.get(3), documents.get(4) });
		matchedDocuments = wrapComparable(lucene.find(parser.parse("with-minusinit")));
		containsAll(matchedDocuments, new ComparableDocument[] { documents.get(3), documents.get(4) });
	}

	public void testMultipleEscapedMinus() throws CorruptIndexException, IOException, ParseException {
		Collection<ComparableDocument> matchedDocuments = wrapComparable(lucene.find(parser.parse("with-minus-in-it")));
		containsOnly(matchedDocuments, documents.get(3));
	}

	public void testEscapedMinusWordMatch() throws CorruptIndexException, IOException, ParseException {
		crRequest.set(CRRequest.WORDMATCH_KEY, "sub");
		parser = new CRQueryParser(LuceneVersion.getVersion(), SEARCHED_ATTRIBUTES, STANDARD_ANALYZER, crRequest);
		Collection<ComparableDocument> matchedDocuments = wrapComparable(lucene.find(parser.parse("with\\-minusinit")));
		containsAll(matchedDocuments, new ComparableDocument[] { documents.get(3), documents.get(4) });
	}

	public void testNumberWithSlashes() throws CorruptIndexException, IOException, ParseException {
		Collection<ComparableDocument> matchedDocuments = wrapComparable(lucene.find(parser.parse("01/23456789")));
		containsAll(matchedDocuments, new ComparableDocument[] { documents.get(8) });
	}

	public void testNumberWithSlashesAndWildcard() throws CorruptIndexException, IOException, ParseException {
		Collection<ComparableDocument> matchedDocuments = wrapComparable(lucene.find(parser.parse("01/2*")));
		containsAll(matchedDocuments, new ComparableDocument[] { documents.get(8) });
	}

	public void testNumberWithSlashesAndWildcards() throws CorruptIndexException, IOException, ParseException {
		crRequest.set(CRRequest.WORDMATCH_KEY, "sub");
		parser = new CRQueryParser(LuceneVersion.getVersion(), SEARCHED_ATTRIBUTES, STANDARD_ANALYZER, crRequest);
		Collection<ComparableDocument> matchedDocuments = wrapComparable(lucene.find(parser.parse("01/23456789")));
		containsAll(matchedDocuments, new ComparableDocument[] { documents.get(8) });
		matchedDocuments = wrapComparable(lucene.find(parser.parse("01/23")));
		containsAll(matchedDocuments, new ComparableDocument[] { documents.get(8) });
	}
}
