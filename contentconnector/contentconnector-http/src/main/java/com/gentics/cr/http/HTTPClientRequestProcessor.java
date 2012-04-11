package com.gentics.cr.http;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRRequest;
import com.gentics.cr.exceptions.CRException;

public class HTTPClientRequestProcessor extends AbstractHTTPClientRequestProcessor {

	public HTTPClientRequestProcessor(CRConfig config) throws CRException {
		super(config);
	}

	@Override
	protected void appendCustomGetParam(GetUrlBuilder urlBuilder, CRRequest request) {
		// IN THE DEFAULT IMPLEMENTATION WE DO NOTHING HERE
	}

}
