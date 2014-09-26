package com.gentics.cr.lucene.search.highlight;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;

import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.search.query.mocks.SimpleLucene;

/**
 * Test highlighting with the PhraseBolder
 * @author christopher
 *
 */
public class CharacterCountPhraseBolderTest extends AbstractBolderTest {

	public CharacterCountPhraseBolderTest(String name) {
		super(name);
	}
	@Override
	public String getBolderClass() {
		return "com.gentics.cr.lucene.search.highlight.CharacterCountPhraseBolder";
	}
	@Override
	public void overwriteConfig(GenericConfiguration config) {
		config.set("fragments", "1");
		config.set("fragmentsize", "24");
	}
	
	/**
	 * We test here if the length of the returned and highlighted fragment meets the configured max fragment size.
	 * @throws ParseException in case of a parse exception
	 * @throws CorruptIndexException in case of a currupted index
	 * @throws IOException in case of a low level IO error
	 */
	public void testFractionLength() throws ParseException, CorruptIndexException, IOException {
		ContentHighlighter highligther = getHighlighter();

		Query parsedQuery = parser.parse("content:(word kord lord)");
		
		String highlighted = highligther.highlight("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, word sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, word kord sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.", parsedQuery);
		
		assertTrue("Highlighted text too long " + highlighted.length() + " ( "+highlighted+" )", highlighted.replaceAll("<b>", "").replaceAll("</b>", "").length() <= highligther.getFragmentSize());
	}

}
