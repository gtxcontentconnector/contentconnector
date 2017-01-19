package com.gentics.cr.rest.xml;

import com.gentics.cr.CRResolvableBean;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import static  org.junit.Assert.assertEquals;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;


public class XmlContentRepositoryTest {
    private static final String FACETS_KEY_NUMERIC = "0";
    private static final String FACETS_VALUE = "something";
    /**
     * Test if the XMLContentRepository is able to handle facets lists in resolvables. Facets lists need special treatment
     * because the keys of the list are numeric values which cannot be converted to XML-elements. Therefore currently
     * the facets list is converted to a JSONObject and stored in the the CDATA section of the element.
     * @throws Exception
     */
    @Test
    public void facetsListOutputTest() throws Exception {
        // Initialize an empty xml cr
        XmlContentRepository xmlCr = new XmlContentRepository(new String[]{});
        // add the facets test resolvable to the cr
        xmlCr.addObject(new TestFacetsResolvable());
        OutputStream out = new ByteArrayOutputStream();
        xmlCr.toStream(out);
        // parse the result as xml using jsoup
        Document doc = Jsoup.parse(out.toString());
        Elements facetElements = doc.getElementsByTag(XmlContentRepository.FACETS_LIST_KEY);
        assertEquals("The result should contain only one facetsList element", facetElements.size(), 1);
        JSONObject json = new JSONObject(facetElements.first().text());
        assertEquals("After parsing the contents of the element to JSON the result should be the same as in the original resolvable", json.get(FACETS_KEY_NUMERIC), FACETS_VALUE);
    }

    class TestFacetsResolvable extends  CRResolvableBean {
        public TestFacetsResolvable() {
            super();
            // Add a map to the resolvable which resembles an basic facets list
            // e.g. numeric strings as keys
            Map<String, Object> facetsResultNode = new HashMap<>();
            facetsResultNode.put(FACETS_KEY_NUMERIC, FACETS_VALUE);
            set(XmlContentRepository.FACETS_LIST_KEY, facetsResultNode);
        }
    }
}
