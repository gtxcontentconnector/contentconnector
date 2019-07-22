package com.gentics.cr.http;

import com.gentics.cr.CRRequest;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Sebastian Vogel <s.vogel at gentics.com>
 */
public class HTTPClientAllParamsRequestProcessorTest {
    
    public HTTPClientAllParamsRequestProcessorTest() {
    }

    /**
     * Test of enumContains static method, of class HTTPClientAllParamsRequestProcessor.
     */
    @Test
    public void testEnumContains() {
        // Test existing value with the AbstractHTTPClientRequestProcessor enum
        String existingValue = AbstractHTTPClientRequestProcessor.DEFAULT_URL_PARAMETERS.ATTRIBUTES.toString();
        assertTrue(
                HTTPClientAllParamsRequestProcessor.enumContains(
                        AbstractHTTPClientRequestProcessor.DEFAULT_URL_PARAMETERS.class, 
                        existingValue
                )
        );
        // Test existing value with the CRRequests enum
        existingValue = CRRequest.DEFAULT_ATTRIBUTES.REQUEST_WRAPPER.toString();
        assertTrue(
                HTTPClientAllParamsRequestProcessor.enumContains(
                        CRRequest.DEFAULT_ATTRIBUTES.class, 
                        existingValue
                )
        );
        // Test nonexisting values in both enums
        String nonExistingValue = "foobar";
        assertFalse(
                HTTPClientAllParamsRequestProcessor.enumContains(
                        AbstractHTTPClientRequestProcessor.DEFAULT_URL_PARAMETERS.class, 
                        nonExistingValue
                )
        );
        assertFalse(
                HTTPClientAllParamsRequestProcessor.enumContains(
                        CRRequest.DEFAULT_ATTRIBUTES.class, 
                        nonExistingValue
                )
        );
    }
    
}
