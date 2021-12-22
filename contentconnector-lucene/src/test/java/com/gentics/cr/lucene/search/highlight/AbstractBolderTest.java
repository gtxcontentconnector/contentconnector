package com.gentics.cr.lucene.search.highlight;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;

import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.AbstractLuceneTest;
import com.gentics.cr.lucene.LuceneVersion;
import com.gentics.cr.lucene.search.query.mocks.SimpleLucene;
import com.gentics.lib.log.NodeLogger;
/**
 * Abstract test class for all highlighters.
 * @author christopher
 *
 */
public abstract class AbstractBolderTest extends AbstractLuceneTest {
	
	private final static NodeLogger LOGGER = NodeLogger.getNodeLogger(AbstractBolderTest.class);

	public AbstractBolderTest(String name) {
		super(name);
	}

	SimpleLucene lucene;

	GenericConfiguration config;

	QueryParser parser;
	
	public abstract String getBolderClass();

	protected void setUp() throws Exception {
		super.setUp();

		lucene = new SimpleLucene();
		lucene.add(SimpleLucene.CONTENT_ATTRIBUTE + ":this word9 the word1 tat", "node_id:1");

		config = new GenericConfiguration();
		config.set("class", getBolderClass());
		config.set("attribute", SimpleLucene.CONTENT_ATTRIBUTE);
		config.set("rule", "1==1");
		config.set("fragments", "2");
		config.set("fragmentsize", "24");
		overwriteConfig(config);
		parser = new QueryParser(LuceneVersion.getVersion(), SimpleLucene.CONTENT_ATTRIBUTE, new StandardAnalyzer(
				LuceneVersion.getVersion(), CharArraySet.EMPTY_SET));
	}
	
	/**
	 * Possibility for individual tests to overwrite the config
	 * @param config
	 */
	public abstract void overwriteConfig(GenericConfiguration config);
	
	/**
	 * Create the highlighter
	 * @return
	 */
	public ContentHighlighter getHighlighter() {
		ContentHighlighter t = null;
		try {
			t = (ContentHighlighter) Class.forName(getBolderClass())
					.getConstructor(new Class[] { GenericConfiguration.class }).newInstance(config);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException
				| ClassNotFoundException e) {
			LOGGER.error(e);
		}
		return t;
	}

	/**
	 * Test highlighting with the configured highlighter
	 * @throws ParseException in case of a parse exception
	 * @throws CorruptIndexException in case of a currupted index
	 * @throws IOException in case of a low level IO error
	 */
	public void testHighlighting() throws ParseException, CorruptIndexException, IOException {
		ContentHighlighter highligther = getHighlighter();
		IndexReader reader = lucene.getReader();
		//CONFIGURE MAX CLAUSES
		BooleanQuery.setMaxClauseCount(BooleanQuery.getMaxClauseCount());

		//CONFIGURE LOWER CASE EXPANDED TERMS
		parser.setLowercaseExpandedTerms(true);

		//ADD SUPPORT FOR LEADING WILDCARDS
		parser.setAllowLeadingWildcard(true);
		parser.setMultiTermRewriteMethod(MultiTermQuery.SCORING_BOOLEAN_QUERY_REWRITE);
		Query parsedQuery = parser.parse("content:t*");
		IndexSearcher searcher = lucene.getSearcher();
		parsedQuery = searcher.rewrite(parsedQuery);
		Document d = reader.document(0);
		String highlighted;
		if (highligther instanceof AdvancedContentHighlighter) {
			highlighted = ((AdvancedContentHighlighter)highligther).highlight(parsedQuery, reader, 0, SimpleLucene.CONTENT_ATTRIBUTE);
		} else {
			highlighted = highligther.highlight(d.get(SimpleLucene.CONTENT_ATTRIBUTE), parsedQuery);
		}
		System.out.println(highlighted);
		assertEquals("Could not properly highlight", "<b>this</b> word9 <b>the</b> word1 <b>tat</b>", highlighted);
	}

}
