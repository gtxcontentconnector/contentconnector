package com.gentics.cr.lucene.search.highlight;

import java.io.IOException;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;

import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.AbstractLuceneTest;
import com.gentics.cr.lucene.LuceneVersion;
import com.gentics.cr.lucene.search.query.mocks.SimpleLucene;

public class WhitespaceVectorBolderTest extends AbstractLuceneTest {

	public WhitespaceVectorBolderTest(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	
	SimpleLucene lucene;
	
	GenericConfiguration config;
	
	QueryParser parser;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		lucene = new SimpleLucene();
		lucene.add(SimpleLucene.CONTENT_ATTRIBUTE + ":this word9 the word1 tat", "node_id:1");
		
		config = new GenericConfiguration();
		config.set("class", "com.gentics.cr.lucene.search.highlight.WhitespaceVectorBolder");
		config.set("attribute", SimpleLucene.CONTENT_ATTRIBUTE);
		config.set("rule", "1==1");
		config.set("fragments", "2");
		config.set("fragmentsize", "24");
		
		parser = new QueryParser(LuceneVersion.getVersion(),
				SimpleLucene.CONTENT_ATTRIBUTE, new StandardAnalyzer(LuceneVersion.getVersion(),
						CharArraySet.EMPTY_SET));
	}
	
	
	public void testHighlighting() throws ParseException, CorruptIndexException, IOException {
		AdvancedContentHighlighter advancedHighlighter = new WhitespaceVectorBolder(config);
		IndexReader reader = lucene.getReader();
		//CONFIGURE MAX CLAUSES
		BooleanQuery.setMaxClauseCount(BooleanQuery.getMaxClauseCount());

		//CONFIGURE LOWER CASE EXPANDED TERMS (useful for WhitespaceAnalyzer)
		parser.setLowercaseExpandedTerms(true);


		//ADD SUPPORT FOR LEADING WILDCARDS
		parser.setAllowLeadingWildcard(true);
			parser.setMultiTermRewriteMethod(MultiTermQuery
					.SCORING_BOOLEAN_QUERY_REWRITE);
		Query parsedQuery = parser.parse("content:t*");
		IndexSearcher searcher = lucene.getSearcher();
		parsedQuery = searcher.rewrite(parsedQuery);
		Document d = reader.document(0);
		String highlighted = advancedHighlighter.highlight(parsedQuery,
				reader,	0, SimpleLucene.CONTENT_ATTRIBUTE);
		System.out.println(highlighted);
		assertEquals("Could not properly highlight", "<b>this</b> word9 <b>the</b> word1 <b>tat</b>", highlighted);
	}

}
