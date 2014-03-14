package com.gentics.cr.lucene.synonyms;

import java.util.ArrayList;
import java.util.Collection;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.exceptions.CRException;

/**
 * Dummy Request Processor is a simple Template 
 * for new RequestProcessor classes.
 * @author Christopher
 *
 */
public class DummySynonymRequestProcessor extends RequestProcessor {
	/**
	 * Constructor.
	 * @param config configuration passed by the system.
	 * @throws CRException exception thrown on error
	 */
	public DummySynonymRequestProcessor(CRConfig config) throws CRException {
		super(config);
		//SETUP THE REQUEST PROCESSOR AND LOAD THE CONFIGURATION
		//HERE YOU COULD LOAD DB-HANDLE INFORMATION OR PATHS TO FILES,...
	}

	@Override
	public void finalize() {
		//CLOSE ALL DATABASE CONNECTIONS HERE
	}

	/**
	 * Method that gets called for retrieving objects.
	 * @param request request object, containig the url, count, size,...
	 * @param doNavigation can be ignored.
	 * @throws CRException exception that gets thrown on error.
	 * @return complete list of objects requested.
	 */
	public final Collection<CRResolvableBean> getObjects(final CRRequest request, final boolean doNavigation)
			throws CRException {
		Collection<CRResolvableBean> response = new ArrayList<CRResolvableBean>();
		//CREATE SOME DUMMY SYNONYMS
		for (int i = 0; i < 10; i++) {
			CRResolvableBean bean = new CRResolvableBean();	
			String descriptorName = "Deskriptor";
			String synonymName = "Synonym";
			bean.set(descriptorName, "d" + i);
			bean.set(synonymName, "s" + i);
			response.add(bean);
		}

		return response;
	}

}
