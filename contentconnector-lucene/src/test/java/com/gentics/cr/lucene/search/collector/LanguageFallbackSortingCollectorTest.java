package com.gentics.cr.lucene.search.collector;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import junit.framework.Assert;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gentics.cr.CRConfigStreamLoader;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.configuration.EnvironmentConfiguration;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.lucene.didyoumean.DidyoumeanIndexExtension;
import com.gentics.cr.lucene.indexaccessor.IndexAccessor;
import com.gentics.cr.lucene.indexer.index.LuceneIndexLocation;
import com.gentics.cr.lucene.search.CRSearcher;
import com.gentics.cr.lucene.search.LuceneRequestProcessor;
import com.gentics.cr.lucene.search.LuceneRequestProcessorTest;
import com.gentics.cr.util.indexing.AbstractUpdateCheckerJob;

public class LanguageFallbackSortingCollectorTest {
	private static CRConfigUtil config = null;
	private static RequestProcessor rp=null;
	private static LuceneIndexLocation location=null;
	
	@BeforeClass
	public static void setUp() throws CRException, URISyntaxException, IOException {
		EnvironmentConfiguration.loadEnvironmentProperties();
		config = new CRConfigStreamLoader("languagesortingfallbacksearch", LuceneRequestProcessorTest.class.getResourceAsStream("languagesortingfallbacksearch.properties"));
		rp = config.getNewRequestProcessorInstance(1);
		CRConfigUtil rpConfig = config.getRequestProcessorConfig(1);
		location = LuceneIndexLocation.getIndexLocation(rpConfig);
		IndexAccessor accessor = location.getAccessor();
		
		addDoc(accessor, "content:content1", "category:cars", "contentid:10007.1", "languagesetit:10007.1","languagecode:de");
		addDoc(accessor, "content:content1", "category:cars", "contentid:10007.11", "languagesetit:10007.1","languagecode:en");
		addDoc(accessor, "content:content1", "category:cars", "contentid:10007.12", "languagesetit:10007.1","languagecode:es");
		addDoc(accessor, "content:audi", "category:cars", "contentid:10007.2", "languagesetit:10007.2","languagecode:de");
		addDoc(accessor, "content:audi", "category:cars", "contentid:10007.21", "languagesetit:10007.2","languagecode:en");
		addDoc(accessor, "content:audi", "category:cars", "contentid:10007.22", "languagesetit:10007.2","languagecode:es");
		addDoc(accessor, "content:saab", "category:cars", "contentid:10007.3", "languagesetit:10007.3","languagecode:de");
		addDoc(accessor, "content:saab", "category:cars", "contentid:10007.31", "languagesetit:10007.3","languagecode:en");
		addDoc(accessor, "content:saab saab saab saab", "category:cars", "contentid:10007.32", "languagesetit:10007.3","languagecode:es");
		addDoc(accessor, "content:volvo", "category:cars", "contentid:10007.4", "languagesetit:10007.4","languagecode:de");
		addDoc(accessor, "content:volvo", "category:cars", "contentid:10007.41", "languagesetit:10007.4","languagecode:en");
		addDoc(accessor, "content:volvo", "category:cars", "contentid:10007.42", "languagesetit:10007.4","languagecode:es");
		addDoc(accessor, "content:ford", "category:cars", "contentid:10007.5", "languagesetit:10007.5","languagecode:de");
		addDoc(accessor, "content:ford", "category:cars", "contentid:10007.51", "languagesetit:10007.5","languagecode:en");
		addDoc(accessor, "content:ford", "category:cars", "contentid:10007.52", "languagesetit:10007.5","languagecode:es");
		addDoc(accessor, "content:tree", "category:cars", "contentid:10007.6","languagesetit:10007.6","languagecode:de");
		addDoc(accessor, "content:potatoe", "category:plants", "contentid:10007.7","languagesetit:10007.7","languagecode:en");
		addDoc(accessor, "content:flower", "category:plants", "contentid:10007.8","languagesetit:10007.8","languagecode:en");
		addDoc(accessor, "content:aflower", "category:plants", "contentid:10007.81","languagesetit:10007.8","languagecode:es");
		addDoc(accessor, "content:tree", "category:plants", "contentid:10007.9");
		
		DidyoumeanIndexExtension dymProvider = ((LuceneRequestProcessor) rp).getCRSearcher().getDYMProvider();
		AbstractUpdateCheckerJob dymIndexJob = dymProvider.createDYMIndexJob(location);
		dymIndexJob.run();
	}
	
	@Test
	public void testConfig() {
		Assert.assertNotNull(rp);
	}
	
	@Test
	public void simpleSearchTest() throws CRException {
		CRRequest request = new CRRequest();
		request.setRequestFilter("category:plants");
		Collection<CRResolvableBean> objects = rp.getObjects(request);
		Assert.assertEquals("The Search did not find all items.", 3, objects.size());
		for(CRResolvableBean bean : objects) {
			Assert.assertEquals("Object was not in category plants.", "plants", bean.get("category"));
		}
		
	}
	
	@Test
	public void simpleFallbackTest() throws CRException {
		CRRequest request = new CRRequest();
		request.setRequestFilter("content:saab");
		Collection<CRResolvableBean> objects = rp.getObjects(request);
		Assert.assertEquals("The Search did not find all items.", 1, objects.size());
		for(CRResolvableBean bean : objects) {
			Assert.assertEquals("Object was not in the desired language", "de", bean.get("languagecode"));
		}	
	}
	
	@Test
	public void simpleFallbackFromRequestStringTest() throws CRException {
		testLanguageFallbackRequest("es,en");
	}
	
	@Test
	public void simpleFallbackFromRequestArrayTest() throws CRException {
		testLanguageFallbackRequest(new String[]{"es", "en"});
	}
	
	
	public void testLanguageFallbackRequest(Object fallbackprio) throws CRException {
		CRRequest request = new CRRequest();
		request.set("languagefallbackpriority", fallbackprio);
		request.setRequestFilter("content:saab");
		Collection<CRResolvableBean> objects = rp.getObjects(request);
		Assert.assertEquals("The Search did not find all items.", 1, objects.size());
		for(CRResolvableBean bean : objects) {
			Assert.assertEquals("Object was not in the desired language", "es", bean.get("languagecode"));
		}	
	}
	
	@Test
	public void sortedPageTest() throws CRException {
		CRRequest request = new CRRequest();
		request.setRequestFilter("category:cars");
		request.setSortArray(new String[]{"languagesetit:asc"});
		request.setStartString("3");
		request.setCountString("3");
		Collection<CRResolvableBean> objects = rp.getObjects(request);
		Assert.assertEquals("The Search did not find all items.", 3, objects.size());
		Iterator<CRResolvableBean> iter = objects.iterator();
		Assert.assertEquals("Wrong item.","10007.4",iter.next().get("languagesetit"));
		Assert.assertEquals("Wrong item.","10007.5",iter.next().get("languagesetit"));
		Assert.assertEquals("Wrong item.","10007.6",iter.next().get("languagesetit"));
	}
	
	@Test
	public void sortedASCSearchTest1() throws CRException {
		testSorting("category:cars", "content", false);
	}
	
	@Test
	public void sortedDESCSearchTest1() throws CRException {
		testSorting("category:cars", "content", true);
	}
	
	@Test
	public void sortedASCSearchTest2() throws CRException {
		testSorting("category:plants", "content", false);
	}
	
	@Test
	public void sortedDESCSearchTest2() throws CRException {
		testSorting("category:plants", "content", true);
	}
	
	private void testSorting(String query, String sortfield, boolean reverse) throws CRException {
		CRRequest request = new CRRequest();
		request.setRequestFilter(query);
		String[] sorting;
		if (reverse) {
			sorting = new String[]{sortfield+":desc"};
		} else {
			sorting = new String[]{sortfield+":asc"};
		}
		request.setSortArray(sorting);
		Collection<CRResolvableBean> objects = rp.getObjects(request);
		String last = null;
		Assert.assertEquals("No results found.",  true, objects.size() > 0);
		int count = 0;
		for(CRResolvableBean bean : objects) {
			boolean sorted = false;
			if (last != null) {
				if (reverse) {
					sorted = last.compareTo(bean.getString(sortfield)) >= 0;
				} else {
					sorted = last.compareTo(bean.getString(sortfield)) <= 0;
				}
				Assert.assertEquals("Object was not correct order. ("+last+":"+bean.getString(sortfield)+") on "+count+" element.", true, sorted);
			}
			last = bean.getString(sortfield);
			count ++;
		}
	}
	
	@Test
	public void sortedByScoreSearchTest() throws CRException {
		CRRequest request = new CRRequest();
		request.setRequestFilter("category:plants");
		Collection<CRResolvableBean> objects = rp.getObjects(request);
		Float last = null;
		for(CRResolvableBean bean : objects) {
			if (last != null) {
				Assert.assertEquals("Object was not corrent order.", true, last.compareTo(bean.getFloat("score", Float.POSITIVE_INFINITY)) >= 0);
			}
			last = new Float(bean.getFloat("score", Float.POSITIVE_INFINITY));
		}
	}
	
	
	@Test
	public void simpleFilterSearchTest() throws CRException {
		
		CRRequest request = new CRRequest();
		request.setRequestFilter("content:tree");
		Collection<CRResolvableBean> objects = rp.getObjects(request);
		Assert.assertEquals("The Search did not find all items.", 2, objects.size());
				
		request = new CRRequest();
		request.setRequestFilter("content:tree");
		request.set("filterquery","category:plants");
		objects = rp.getObjects(request);
		Assert.assertEquals("The Search did not find all items.", 1, objects.size());
		for(CRResolvableBean bean : objects) {
			Assert.assertEquals("Object was not in category plants.", "plants", bean.get("category"));
		}
	}
	
	@Test
	public void simpleFilterComparisonSearchTest() throws CRException {
		CRRequest request = new CRRequest();
		request.setRequestFilter("content:tree AND category:plants");
		Collection<CRResolvableBean> objects = rp.getObjects(request);
		Assert.assertEquals("The Search did not find all items.", 1, objects.size());
		for(CRResolvableBean bean : objects) {
			Assert.assertEquals("Object was not in category plants.", "plants", bean.get("category"));
		}
	}
	
	@Test
	public void metaResolvableSearchTest() throws CRException {
		CRRequest request = new CRRequest();
		request.set("metaresolvable", "true");
		request.setRequestFilter("category:plants");
		Collection<CRResolvableBean> objects = rp.getObjects(request);
		CRResolvableBean metabean = objects.iterator().next();
		Assert.assertNotNull(metabean);
		Object collector = metabean.get(CRSearcher.RESULT_COLLECTOR_KEY);
		Assert.assertEquals("Collector was not the expected one (LanguageFallbackTopDocsCollecto!="+collector.getClass().getName()+")", true, collector instanceof LanguageFallbackSortingTopDocsCollector);
		Assert.assertEquals("The Search did not find all items.", 4, objects.size());
		Assert.assertEquals("Hitcount did not match the expected value.", 3, metabean.getInteger("totalhits", 0));
		Assert.assertEquals("We did not find the other languages.",true,((LanguageFallbackSortingTopDocsCollector)collector).getOtherLanguages().contains("es"));
	}
	
	@Test
	public void testDYMMetaResolvableSearch() throws CRException {
		CRRequest request = new CRRequest();
		request.set("metaresolvable", "true");
		request.setRequestFilter("content:frd");
		Collection<CRResolvableBean> objects = rp.getObjects(request);
		
		CRResolvableBean metabean = objects.iterator().next();
		HashMap<String, String[]> suggestions = (HashMap<String, String[]>) metabean.get("suggestions");
		Assert.assertNotNull(suggestions);
		Assert.assertEquals("Suggestion count did not match the expected value.", 1, suggestions.get("frd").length);
		Assert.assertEquals("Suggestion did not match the expected value.", "ford", suggestions.get("frd")[0]);
	}
	
	@AfterClass
	public static void tearDown() throws IOException {
		DidyoumeanIndexExtension dymProvider = ((LuceneRequestProcessor) rp).getCRSearcher().getDYMProvider();
		AbstractUpdateCheckerJob dymIndexJob = dymProvider.createDYMIndexDeleteJob(location);
		dymIndexJob.run();
		Assert.assertEquals("DYM Index should be empty.", 0, dymProvider.getDidyoumeanLocation().getDocCount());
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
		IndexWriter writer = ia.getWriter();
		
		Document document = new Document();
		for (String field : fields) {
			String name = field.replaceAll(":.*", "");
			String value = field.substring(name.length() + 1);
			document.add(new Field(name, value, Field.Store.YES, Field.Index.ANALYZED,
					TermVector.WITH_POSITIONS_OFFSETS));
		}
		
		writer.addDocument(document);
		ia.release(writer);
		return document;
	}
}
