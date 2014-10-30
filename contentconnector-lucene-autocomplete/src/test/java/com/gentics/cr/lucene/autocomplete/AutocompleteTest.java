package com.gentics.cr.lucene.autocomplete;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;

import junit.framework.Assert;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gentics.cr.CRConfigStreamLoader;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.configuration.EnvironmentConfiguration;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.lucene.indexaccessor.IndexAccessor;
import com.gentics.cr.lucene.indexer.index.LuceneIndexLocation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class AutocompleteTest {
	private static CRConfigUtil autoConfig = null;
	private static RequestProcessor rp=null;
	private static LuceneIndexLocation location=null;
	private static Autocompleter autocompleter = null;
        
	@BeforeClass
	public static void setUp() throws CRException, URISyntaxException, IOException {
                String configLocation = AutocompleteTest.class.getResource(".").toURI().getPath();
		EnvironmentConfiguration.setConfigPath(configLocation);
		EnvironmentConfiguration.loadEnvironmentProperties();
		autoConfig = new CRConfigStreamLoader("autocomplete", AutocompleteTest.class.getResourceAsStream("autocomplete.properties"));
		rp = autoConfig.getNewRequestProcessorInstance(1);
		CRConfigUtil rpConfig = autoConfig.getRequestProcessorConfig(1);
		location = LuceneIndexLocation.getIndexLocation(rpConfig);
		IndexAccessor accessor = location.getAccessor();
		
		addDoc(accessor, "content:content1", "category:cars", "contentid:10007.1");
		addDoc(accessor, "content:audi", "category:cars", "contentid:10007.2");
		addDoc(accessor, "content:saab", "category:cars", "contentid:10007.3");
		addDoc(accessor, "content:volvo", "category:cars", "contentid:10007.4");
		addDoc(accessor, "content:ford", "category:cars", "contentid:10007.5");
		addDoc(accessor, "content:pagani", "category:cars", "contentid:10007.6");
		addDoc(accessor, "content:potatoe", "category:plants", "contentid:10007.7");
		addDoc(accessor, "content:flower", "category:plants", "contentid:10007.8");
		addDoc(accessor, "content:tree", "category:plants", "contentid:10007.9");
                addDoc(accessor, "content:schön", "category:words", "contentid:10007.10");
                addDoc(accessor, "content:schon", "category:words", "contentid:10007.11");
                addDoc(accessor, "content:schon", "category:words", "contentid:10007.12");
                addDoc(accessor, "content:ignore", "category:words", "contentid:10007.13");
		
		AutocompleteIndexExtension autoExtension = new AutocompleteIndexExtension(rpConfig, location);
		AutocompleteIndexJob aIJ = new AutocompleteIndexJob(autoConfig, location, autoExtension);
		aIJ.run();
                autocompleter = new Autocompleter(rpConfig);
	}
	
	@Test
	public void testConfig() {
		Assert.assertNotNull(rp);
	}
	
	@Test
	public void testSingleAutocomplete() throws CRException, IOException {
            HashMap<String, Integer> shouldContain = new HashMap<String, Integer>();
            shouldContain.put("audi", 1);

            Collection<CRResolvableBean> objects = retrieveSuggestions("a");
            checkResolveableCollection(objects, 1, shouldContain);
	}
	
	@Test
	public void testMultiAutocomplete() throws CRException, IOException {
            HashMap<String, Integer> shouldContain = new HashMap<String, Integer>();
            shouldContain.put("potatoe", 1);
            shouldContain.put("pagani", 1);

            Collection<CRResolvableBean> objects = retrieveSuggestions("p");
            checkResolveableCollection(objects, 2, shouldContain);
	}
        
        
        /**
         * Tests if the search term is parsed correctly and if words containing
         * special characters (umlaute) can be found.
         */
        @Test
        public void testSearchTermParsing() throws CRException, IOException {
            HashMap<String, Integer> shouldContain = new HashMap<String, Integer>();
            shouldContain.put("schon", 2); // there are 2 documents containing "schon" 
            shouldContain.put("schön", 1); // there is only 1 documents containing "schön" 
            
            // test if words with special characters are found by ASCII equivalent ('o' == 'ö')
            Collection<CRResolvableBean> suggestedWords = retrieveSuggestions("scho");
            checkResolveableCollection(suggestedWords, 2, shouldContain);
            
            // test if words with special chars are found (including words with the ASCII equivalent of the special char)
            suggestedWords = retrieveSuggestions("schö");
            checkResolveableCollection(suggestedWords, 2, shouldContain);
            
            // test if only the last word is used for suggestions (ignore is in the index but should not be in the collection)
            suggestedWords = retrieveSuggestions("ignore scho");
            checkResolveableCollection(suggestedWords, 2, shouldContain);
            // check if "ignore" is really in the index
            suggestedWords = retrieveSuggestions("ignore");
            shouldContain = new HashMap<String, Integer>();
            shouldContain.put("ignore", 1);
            checkResolveableCollection(suggestedWords, 1, shouldContain);
        }
        /**
         * Check a autocomplete resolveable collection for size and the 
         * suggested words it contains. This method will call assertions for
         * the collection size, the expected document count for a certain word
         * and if certain words are contained in the collection
         * 
         * @param collection the collection to check
         * @param size the expected size of the collection
         * @param shouldContain a hash map of the words this collection should contain (keys) and the expected document count (value)
         */
        private void checkResolveableCollection(Collection<CRResolvableBean> collection, int size, HashMap<String, Integer> shouldContain) {
            Assert.assertEquals("Expected size of collection does not match actual size", size, collection.size());
            List<String> foundWords = new ArrayList<String>();
            Iterator<CRResolvableBean> it = collection.iterator();
            while(it.hasNext()) {
                CRResolvableBean bean = it.next();
                String word = (String) bean.get("word");
                foundWords.add(word);
                if(shouldContain.get(word) != null) {
                    StringBuilder sb = new StringBuilder("Autocompleter did not return the expected result count for '");
                    sb.append(word).append("'");
                    Integer count = Integer.parseInt((String) bean.get("count"));
                    Assert.assertEquals(sb.toString(), shouldContain.get(word), count);
                }
            }
            Assert.assertTrue("Not all words the collections should contain where found", foundWords.containsAll(shouldContain.keySet()));
        }
        /**
         * get suggestions for a term
         * @param term the term to search for
         * @return the resolveables the autocompleter found for this term
         * @throws IOException 
         */
	private Collection<CRResolvableBean> retrieveSuggestions(String term) throws IOException {
            CRRequest request = new CRRequest();
            request.setRequestFilter(term);
            return autocompleter.suggestWords(request);
        }
	/**
	 * Adds a Document to the index.
	 * @param ia
	 * @param fields
	 * @return
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	private static Document addDoc(IndexAccessor ia, String... fields) throws CorruptIndexException, IOException {
		Document document = new Document();
		for (String field : fields) {
			String name = field.replaceAll(":.*", "");
			String value = field.substring(name.length() + 1);
			document.add(new Field(name, value, Field.Store.YES, Field.Index.ANALYZED,
					TermVector.WITH_POSITIONS_OFFSETS));
		}
		IndexWriter writer = ia.getWriter();
		writer.addDocument(document);
		ia.release(writer);
		return document;
	}
}
