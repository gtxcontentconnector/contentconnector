package com.gentics.cr;

import com.gentics.cr.util.MockHttpServletRequest;
import com.gentics.cr.util.RequestWrapper;
import java.util.HashMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

/**
 *
 * @author Sebastian Vogel <s.vogel at gentics.com>
 * @date Oct 28, 2014
 */
public class CRRequestTest {

    private CRRequest createTestRequest(HashMap<String, String> requestParameters) {
        CRRequest req = new CRRequest("dummyfilter");
        if(requestParameters != null) {
            req.setRequest(new RequestWrapper(new MockHttpServletRequest(requestParameters)));
        }
        return req;
    }

    /**
     * Test the getter for request parameters without a request wrapper in the CRRequest. 
     */
    @Test
    public void testGetParameter() {
        String key = "foo";
        // test without parameters set
        CRRequest req = createTestRequest(null);
        assertNull(
                "When no parameters and no request wrapper is set, a call to get with fallback=true should not throw an exception", 
                req.get(key, Boolean.TRUE)
        );
    }
    /**
     * Test the getter for request parameters with an empty request wrapper in the CRRequest. 
     */
    @Test
    public void testGetParameterWithEmptyServletRequest() {
        String key = "foo";
        // test request with empty request paramters
        CRRequest req = createTestRequest(new HashMap<String, String>());
        assertNull("Null should be returned if no parameter is set", req.get(key));
        assertNull("Null should be returned if no parameter is set and fallback=false", req.get(key, Boolean.FALSE));
        req.set(key, "bar");
        assertEquals("If a parameter is set in the CRRequest, the value should be returned", "bar", req.get(key));
    }
    /**
     * Test the getter for request parameters with an filled request wrapper in the CRRequest. 
     */
    @Test
    public void testGetParameterWithFilledServletRequest() { 
        String key = "foo";
        // test request with parameters in the servlet request
        HashMap<String, String> servletParams = new HashMap<>();
        servletParams.put(key, "servletBar");
        CRRequest req = createTestRequest(servletParams);
        assertNull(
                "Null should be returned when get is called with a single argument and the parameter is only set in the servlet request", 
                req.get(key)
        );
        assertNull(
                "Null should be returned when get is called with fallback=false and the parameter is only set in the servlet request", 
                req.get(key)
        );
        assertEquals(
                "If the parameter is only set in the servlet request and get is called with fallback=true, the value should be retrieved from the servlet request.",
                "servletBar",
                req.get(key, Boolean.TRUE)
        );
        req.set(key, "bar");
        assertNotEquals(
                "If the parameter is set in both requests and get is called with a single argument, the value should be different from the one stored in the servlet request", 
                "servletBar",
                req.get(key)
        );
        assertNotEquals(
                "If the parameter is set in both requests and get is called with fallback=false, the value should be different from the one stored in the servlet request.", 
                "servletBar",
                req.get(key, Boolean.FALSE)
        );
        assertEquals(
                "If the parameter is set in both requests and calls to get with fallback=false and fallback=true shoudl return the same value.", 
                req.get(key, Boolean.TRUE),
                req.get(key, Boolean.FALSE)
        );
    }

}
