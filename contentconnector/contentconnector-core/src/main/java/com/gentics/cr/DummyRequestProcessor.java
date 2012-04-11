package com.gentics.cr;

import java.util.ArrayList;
import java.util.Collection;

import com.gentics.cr.exceptions.CRException;

/**
 * Dummy Request Processor is a simple Template 
 * for new RequestProcessor classes.
 * @author Christopher
 *
 */
public class DummyRequestProcessor extends RequestProcessor {
	/**
	 * Key to load from the configuration.
	 */
	private static final String PREFIX_KEY = "prefix";
	/**
	 * Custom prefix that will be added to all values.
	 */
	private String customPrefix = "";

	/**
	 * Constructor.
	 * @param config configuration passed by the system.
	 * @throws CRException exception thrown on error
	 */
	public DummyRequestProcessor(final CRConfig config) throws CRException {
		super(config);
		//SETUP THE REQUEST PROCESSOR AND LOAD THE CONFIGURATION
		//HERE YOU COULD LOAD DB-HANDLE INFORMATION OR PATHS TO FILES,...
		customPrefix = (String) config.get(PREFIX_KEY);
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
		//The Request filter could be a SQL statement, a XPATH Query
		//or anything the like.
		String requestFilter = request.getRequestFilter();
		//DO FANCY STUFF TO RETRIEVE AND/OR CREATE OBJECTS
		for (int i = 0; i < 10; i++) {
			CRResolvableBean bean = new CRResolvableBean();
			String[] requestedAttributes = request.getAttributeArray();

			for (String attribute : requestedAttributes) {
				bean.set(attribute, customPrefix + "VALUE" + i + "_FOR_" + attribute);
			}
			//CHECK IF BEAN MATCHES THE CONFIGURED RULE
			// IN THIS CASE IT ALWAYS DOES IF THE FILTER IS NOT NULL
			if (requestFilter != null) {
				response.add(bean);
			}
		}

		return response;
	}

}
