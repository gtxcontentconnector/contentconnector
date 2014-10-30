package com.gentics.cr.http;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRRequest;
import com.gentics.cr.exceptions.CRException;
import java.util.Arrays;
import java.util.List;

/**
 * Adds all parameters which are not in the list of default parameters in the
 * {@link CRRequest} ({@link CRRequest#DEFAULT_ATTRIBUTES}) or in the list of default
 * URL parameters in the {@link AbstractHTTPClientRequestProcessor} ({@link AbstractHTTPClientRequestProcessor#DEFAULT_URL_ATTRIBUTES})
 * 
 * @author Sebastian Vogel <s.vogel at gentics.com>
 * @date Oct 30, 2014
 */
public class HTTPClientAllParamsRequestProcessor  extends AbstractHTTPClientRequestProcessor {
    
        private static final List<String> DEFAULT_CRREQUEST_ATTR = Arrays.asList(CRRequest.DEFAULT_ATTRIBUTES);

        public HTTPClientAllParamsRequestProcessor(CRConfig config) throws CRException {
		super(config);
	}

	@Override
	protected void appendCustomGetParam(GetUrlBuilder urlBuilder, CRRequest request) {
            for(String key : request.getParameterKeys()) {
                if(!DEFAULT_URL_ATTRIBUTES.contains(key) && !DEFAULT_CRREQUEST_ATTR.contains(key)) {
                    urlBuilder.appendSkipEmpty(request, key);
                }
            }
	}
}
