package com.gentics.cr;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;

import com.gentics.cr.exceptions.CRException;

/**
 * Tests concerning CMSPageLanguageFallbackRequestProcessor
 * @author escitalopram
 *
 */
public class CMSPageLanguageFallbackRequestProcessorTest extends
		RequestProcessorTest {

	private static RequestProcessor requestProcessor;

	private static String [] page_attributes = {"name", "content", "lang", "contentid_de", "contentid_en"};

	@BeforeClass
	public static void setUp() throws CRException, URISyntaxException, IOException {

		CRConfigUtil config = HSQLTestConfigFactory.getDefaultHSQLConfiguration(CMSPageLanguageFallbackRequestProcessorTest.class.getName(), true);

		requestProcessor = new CMSPageLanguageFallbackRequestProcessor(config.getRequestProcessorConfig(1));

		// Create and prefill CR
		HSQLCRTestHandler testHandler = new HSQLCRTestHandler(config.getRequestProcessorConfig(1));

		CRResolvableBean testBean = new CRResolvableBean();
		testBean.setObj_type("10007");
		testBean.set("name", "test1");
		testBean.set("lang", "de");
		testBean.set("content", "Dies ist deutscher Content");
		testBean.set("contentid_en","10007.2");
		testHandler.createBean(testBean, page_attributes);

		testBean = new CRResolvableBean();
		testBean.setObj_type("10007");
		testBean.set("name", "test1");
		testBean.set("lang", "en");
		testBean.set("contentid_de","10007.1");
		testBean.set("content", "This is english content");
		testHandler.createBean(testBean, page_attributes);
	}

	@Override
	protected RequestProcessor getRequestProcessor() {
		return requestProcessor;
	}

	@Override
	protected int getExpectedCollectionSize() {
		return 0;
	}

	@Override
	protected CRRequest getRequest() {
		// Insert some unsatisfiable query to make inherited someTest happy
		CRRequest result = new CRRequest("contentid == 666");
		return result;
	}

	/**
	 * Test if fallback delivers a page for every requested language, even if it doesn't exist
	 * @throws CRException
	 */
	@Test
	public void testFallback() throws CRException {
		for (String language : new String[]{"de", "en", "cz"}) {
			CRRequest request = new CRRequest();
			request.set("langs", Arrays.asList(new String[] { language }));
			request.set("contentid", "10007.2");

			Collection<CRResolvableBean> beans = getRequestProcessor().getObjects(request);
			assertEquals("must return page for language " + language, 1, beans.size());
		}
	}

}
