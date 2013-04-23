package com.gentics.cr.lucene.autocomplete;

import java.io.IOException;
import java.util.Collection;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.exceptions.CRException;

public class AutocompleteRequestProcessor extends RequestProcessor {

	private Autocompleter autocompleter;

	public AutocompleteRequestProcessor(CRConfig config) throws CRException {
		super(config);
		autocompleter = new Autocompleter(config);
	}

	@Override
	public void finalize() {
		if (this.autocompleter != null)
			autocompleter.finalize();
	}

	@Override
	public Collection<CRResolvableBean> getObjects(CRRequest request, boolean doNavigation) throws CRException {
		Collection<CRResolvableBean> result = null;
		try {
			result = autocompleter.suggestWords(request);
		} catch (IOException ex) {
			throw new CRException(ex);
		}
		return result;
	}

}
