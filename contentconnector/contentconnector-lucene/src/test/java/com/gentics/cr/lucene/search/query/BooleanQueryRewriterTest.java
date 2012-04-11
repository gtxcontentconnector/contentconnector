package com.gentics.cr.lucene.search.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;

import com.gentics.cr.CRRequest;
import com.gentics.cr.lucene.AbstractLuceneTest;
import com.gentics.cr.lucene.LuceneVersion;
import com.gentics.cr.lucene.search.query.mocks.ComparableDocument;
import com.gentics.cr.lucene.search.query.mocks.SimpleLucene;

public class BooleanQueryRewriterTest extends AbstractLuceneTest {

	public BooleanQueryRewriterTest(String name) {
		super(name);
	}

	private static final StandardAnalyzer STANDARD_ANALYZER = new StandardAnalyzer(LuceneVersion.getVersion());
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
		/* 3 */documents
				.add(new ComparableDocument(lucene.add(SimpleLucene.CONTENT_ATTRIBUTE + ":wörd4", "node_id:2")));
		/* 4 */documents.add(new ComparableDocument(lucene.add(
			SimpleLucene.CONTENT_ATTRIBUTE + ":word5",
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
			SimpleLucene.CONTENT_ATTRIBUTE + ":word8",
			"updatetimestamp:1304510397",
			"edittimestamp:1304510397",
			"node_id:3"))); //04.05.2011 13:59:57
		/* 8 */documents.add(new ComparableDocument(lucene.add(
			SimpleLucene.CONTENT_ATTRIBUTE + ":newword",
			"node_id:11")));

	}

	public void testReplaceTerm() throws ParseException, CorruptIndexException, IOException {
		Query orginalQuery = parser.parse("word1 | word2");
		Query newQuery = BooleanQueryRewriter.replaceTerm(orginalQuery, new Term(SimpleLucene.CONTENT_ATTRIBUTE,
				"word1"), new Term(SimpleLucene.CONTENT_ATTRIBUTE, "word3"));

		Collection<Document> matchedDocuments = lucene.find(newQuery);
		containsAll(matchedDocuments, new ComparableDocument[] { documents.get(1), documents.get(2) });
	}

	public void testReplaceTerms() throws ParseException, CorruptIndexException, IOException {
		Query orginalQuery = parser.parse("word1 & word2");
		HashMap<Term, Term> replacements = new HashMap<Term, Term>();
		replacements.put(new Term(SimpleLucene.CONTENT_ATTRIBUTE, "word1"), new Term(SimpleLucene.CONTENT_ATTRIBUTE,
				"word3"));
		replacements.put(new Term("binarycontent", "word2"), new Term("binarycontent", "word9"));

		Query newQuery = BooleanQueryRewriter.replaceTerms(orginalQuery, replacements);

		Collection<Document> matchedDocuments = lucene.find(newQuery);
		containsOnly(matchedDocuments, documents.get(2));
	}
}
