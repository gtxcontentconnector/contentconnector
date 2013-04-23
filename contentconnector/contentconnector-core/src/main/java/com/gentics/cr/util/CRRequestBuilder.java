package com.gentics.cr.util;

import java.util.ArrayList;

import javax.portlet.Portlet;
import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import com.gentics.cr.CRRequest;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.configuration.GenericConfiguration;

/**
 * Creates an expressionparser query from the provided query attributes.
 * 
 * This class is mostly used for querying a contentrepository.
 * 
 * Last changed: $Date: 2010-04-01 15:24:41 +0200 (Do, 01 Apr 2010) $
 * 
 * @version $Revision: 543 $
 * @author $Author: supnig@constantinopel.at $
 */
public class CRRequestBuilder {

	private boolean addPermissionsToRule = true;
	protected boolean isDebug = false;
	protected boolean metaresolvable = false;
	protected String highlightquery;
	protected String filter;
	protected String query_and;
	protected String query_or;
	protected String query_not;
	protected String query_group;
	protected String start;
	protected String count;
	protected String contentid;
	protected String wordmatch;
	protected String[] node_id;
	protected String[] sorting;
	protected String[] plinkattributes;
	protected String[] permissions;
	protected String[] options;
	protected RequestWrapper request;
	protected Object response;
	private ContentRepositoryConfig contentRepository = null;

	protected GenericConfiguration config;

	/**
	 * name of the configuration attribute where the defaultparameters are
	 * stored in.
	 */
	public static final String DEFAULPARAMETERS_KEY = "defaultparameters";

	/**
	 * Configuration key for setting if the permissions should be added to the
	 * rule. (This is to disable this feature because it breaks lucene)
	 */
	private static final String ADD_PERMISSIONS_TO_RULE_KEY = "addPermissionsToRule";

	/**
	 * Initializes the CRRequestBuilder from a {@link javax.portlet.Portlet}.
	 * 
	 * @param portletRequest request from the {@link javax.portlet.Portlet}
	 */
	public CRRequestBuilder(final PortletRequest portletRequest) {
		this(portletRequest, null);
	}

	/**
	 * Initializes the CRRequestBuilder from a {@link javax.portlet.Portlet}.
	 * 
	 * @param portletRequest request from the {@link javax.portlet.Portlet}
	 * @param requestBuilderConfiguration configuration for the request builder
	 */
	public CRRequestBuilder(final PortletRequest portletRequest, final GenericConfiguration requestBuilderConfiguration) {
		this(new RequestWrapper(portletRequest), requestBuilderConfiguration);
	}

	/**
	 * Initializes the CRRequestBuilder from a {@link javax.servlet.http.HttpServletRequest}.
	 * 
	 * @param servletRequest request from the {@link javax.servlet.http.HttpServletRequest}
	 */
	public CRRequestBuilder(final HttpServletRequest servletRequest) {
		this(servletRequest, null);
	}

	/**
	 * Initializes the CRRequestBuilder from a {@link javax.servlet.http.HttpServletRequest}.
	 * 
	 * @param servletRequest request from the {@link javax.servlet.http.HttpServletRequest}
	 * @param conf configuration for the request builder where we get the default parameters from.
	 */
	public CRRequestBuilder(final HttpServletRequest servletRequest, final GenericConfiguration conf) {
		this(new RequestWrapper(servletRequest), conf);
	}

	/**
	 * Initializes the CRRequestBuilder in a general manner that is compatible
	 * with {@link Portlet}s and {@link javax.servlet.http.HttpServletRequest}s.
	 * 
	 * @param requestWrapper wrapped request from a {@link javax.servlet.http.HttpServletRequest} or a {@link Portlet}
	 * @param requestBuilderConfiguration configuration for the request builder
	 */
	public CRRequestBuilder(final RequestWrapper requestWrapper, final GenericConfiguration requestBuilderConfiguration) {

		this.config = requestBuilderConfiguration;
		this.contentRepository = new ContentRepositoryConfig(config);

		this.request = requestWrapper;
		this.filter = requestWrapper.getParameter("filter");
		this.contentid = requestWrapper.getParameter("contentid");
		this.count = requestWrapper.getParameter("count");
		this.start = requestWrapper.getParameter("start");
		this.sorting = requestWrapper.getParameterValues("sorting");
		this.contentRepository.setAttributeArray(prepareAttributesArray(requestWrapper.getParameterValues("attributes")));
		this.plinkattributes = requestWrapper.getParameterValues("plinkattributes");
		this.permissions = requestWrapper.getParameterValues("permissions");
		this.options = requestWrapper.getParameterValues("options");
		this.contentRepository.setRepositoryType(requestWrapper.getParameter("type"));
		this.isDebug = (requestWrapper.getParameter("debug") != null && requestWrapper.getParameter("debug").equals("true"));
		this.metaresolvable = Boolean.parseBoolean(requestWrapper.getParameter(RequestProcessor.META_RESOLVABLE_KEY));
		this.highlightquery = requestWrapper.getParameter(RequestProcessor.HIGHLIGHT_QUERY_KEY);
		this.node_id = requestWrapper.getParameterValues("node");
		this.query_and = requestWrapper.getParameter("q_and");
		this.query_or = requestWrapper.getParameter("q_or");
		this.query_not = requestWrapper.getParameter("q_not");
		this.query_group = requestWrapper.getParameter("q_group");
		this.wordmatch = requestWrapper.getParameter("wm");
		if (config != null) {
			String addPermissionsToRuleConfig = config.getString(ADD_PERMISSIONS_TO_RULE_KEY);
			if (addPermissionsToRuleConfig != null) {
				this.addPermissionsToRule = Boolean.parseBoolean(addPermissionsToRuleConfig);
			}
		}

		// Parameters used in mnoGoSearch for easier migration (Users should use
		// type=MNOGOSEARCHXML)
		if (this.filter == null) {
			this.filter = requestWrapper.getParameter("q");
		}
		if (this.filter == null) {
			this.filter = requestWrapper.getParameter("query");
		}
		if (this.count == null) {
			this.count = requestWrapper.getParameter("ps");
		}
		if (this.start == null && this.count != null) {
			String numberOfPageStr = requestWrapper.getParameter("np");
			calcStartFromCount(numberOfPageStr);
		}

		// if filter is not set and contentid is => use contentid instead
		if (("".equals(filter) || filter == null) && contentid != null && !contentid.equals("")) {
			filter = "object.contentid == '" + contentid + "'";
		}

		addAdvancedSearchParameters();

		// SET PERMISSIONS-RULE
		if (addPermissionsToRule) {
			filter = this.createPermissionsRule(filter, permissions);
		}

		contentRepository.getDefaultParameters();

		getDefaultParameters();
	}

	/**
	 * Calculates the start value from the number of pages * count per page.
	 * @param numberOfPageStr
	 */
	private void calcStartFromCount(final String numberOfPageStr) {
		int numberOfPage;
		if (numberOfPageStr != null) {
			numberOfPage = Integer.parseInt(numberOfPageStr);
		} else {
			numberOfPage = 0;
		}
		int intCount = Integer.parseInt(this.count);
		this.start = (numberOfPage * intCount) + "";
	}

	/**
	 * done in the same way as the parameter initialisation in the constructor
	 * to avoid repeated code.
	 */
	private void getDefaultParameters() {
		GenericConfiguration defaultparameters = null;
		if (this.config != null) {
			defaultparameters = (GenericConfiguration) this.config.get(DEFAULPARAMETERS_KEY);
		}
		if (defaultparameters != null) {
			if (this.node_id == null) {
				String defaultNode = defaultparameters.getString("node");
				if (defaultNode != null) {
					this.node_id = defaultNode.split("^");
				}
			}
			if (contentid == null) {
				contentid = defaultparameters.getString("contentid");
			}
			if (filter == null) {
				filter = defaultparameters.getString("filter");
			}
			if (wordmatch == null) {
				wordmatch = defaultparameters.getString("wm");
			}
			if (count == null) {
				count = defaultparameters.getString("ps");
			}
			if (start == null && count != null) {
				String numberOfPageStr = defaultparameters.getString("np");
				calcStartFromCount(numberOfPageStr);
			}
			addAdvancedSearchParameters();
		}
	}

	/**
	 * Get array of options.
	 * 
	 * @return array with options
	 */
	public final String[] getOptionArray() {
		return this.options;
	}

	/**
	 * Returns true if this is a debug request.
	 * 
	 * @return true if debug is enabled
	 */
	public final boolean isDebug() {
		return this.isDebug;
	}

	/**
	 * Parameters for more advanced searches.
	 * (and, or, not, group, filter)
	 */
	private void addAdvancedSearchParameters() {
		if (filter == null || "".equals(filter)) {
			if (query_and != null && !"".equals(query_and)) {
				StringBuilder filterAnd = new StringBuilder();
				for (String query : query_and.split(" ")) {
					if (!"".equals(filterAnd.toString())) {
						filterAnd.append(" AND ");
					}
					filterAnd.append(query.toLowerCase());
				}
				if (!"".equals(filterAnd.toString())) {
					filter = "(" + filterAnd.toString() + ")";
				}
				query_and = "";
			}
			if (query_or != null && !"".equals(query_or)) {
				StringBuilder filterOr = new StringBuilder();
				for (String query : query_or.split(" ")) {
					if (!"".equals(filterOr.toString())) {
						filterOr.append(" OR ");
					}
					filterOr.append(query.toLowerCase());
				}
				if (!"".equals(filterOr.toString())) {
					if (!"".equals(filter)) {
						filter += " AND ";
					}
					filter += "(" + filterOr + ")";
				}
				query_or = "";
			}
			if (query_not != null && !"".equals(query_not)) {
				StringBuilder filterNot = new StringBuilder();
				for (String query : query_not.split(" ")) {
					if (!"".equals(filterNot.toString())) {
						filterNot.append(" OR ");
					}
					filterNot.append(query.toLowerCase());
				}
				if (!"".equals(filterNot.toString())) {
					if (!"".equals(filter)) {
						filter += " AND ";
					}
					filter += "NOT (" + filterNot + ")";
				}
				query_not = "";
			}
			if (query_group != null && !"".equals(query_group)) {
				if (!"".equals(filter)) {
					filter += " AND ";
				}
				filter += " \"" + query_group.toLowerCase() + "\"";
				query_group = "";
			}

		}
		if ((filter != null && !"".equals(filter)) && this.node_id != null && this.node_id.length != 0
				&& !filter.matches("(.+[ (])?node_id\\:[0-9]+.*")) {
			StringBuilder nodeFilter = new StringBuilder();
			for (int i = 0; i < node_id.length; i++) {
				if (!nodeFilter.toString().equals("")) {
					nodeFilter.append(" OR ");
				}
				nodeFilter.append("node_id:" + node_id[i]);
			}
			node_id = new String[] {};
			filter = "(" + filter + ") AND (" + nodeFilter.toString() + ")";
		}
	}

	/**
	 * Creates a CRRequest from the configuration.
	 * 
	 * @return created CRRequest
	 */
	public final CRRequest getCRRequest() {
		CRRequest req = new CRRequest(filter, start, count, sorting, contentRepository.getAttributeArray(), plinkattributes);
		req.setContentid(this.contentid);
		req.setRequest(this.request);
		req.setResponse(this.response);
		req.set(RequestProcessor.META_RESOLVABLE_KEY, this.metaresolvable);
		req.set(CRRequest.WORDMATCH_KEY, this.wordmatch);
		req.set(CRRequest.PERMISSIONS_KEY, this.permissions);
		if (this.highlightquery != null) {
			req.set(RequestProcessor.HIGHLIGHT_QUERY_KEY, this.highlightquery);
		}
		return req;
	}

	/**
	 * returns the request object.
	 * 
	 * @return the request
	 */
	public final Object getRequest() {
		return this.request;
	}

	public final ContentRepositoryConfig getContentRepositoryConfig() {
		return this.contentRepository;
	}

	/**
	 * Wrapps filter rule with the given set of permissions.
	 * 
	 * @param objectFilter
	 * @param userPermissions
	 */
	protected final String createPermissionsRule(final String objectFilter, final String[] userPermissions) {
		String ret = objectFilter;
		if ((userPermissions != null) && (userPermissions.length > 0)) {
			if ((objectFilter != null) && (!objectFilter.equals(""))) {
				ret = "(" + objectFilter + ") AND object.permissions CONTAINSONEOF " + CRUtil.prepareParameterArrayForRule(userPermissions);
			} else {
				ret = "object.permissions CONTAINSONEOF " + CRUtil.prepareParameterArrayForRule(userPermissions);
			}
		}
		return ret;
	}

	protected final String[] prepareAttributesArray(final String[] attributes) {
		ArrayList<String> ret = new ArrayList<String>();
		if (attributes != null) {
			for (String item : attributes) {
				if (item.contains(",")) {
					String[] items = item.split(",");
					for (String subatt : items) {
						ret.add(subatt);
					}
				} else {
					ret.add(item);
				}
			}
		}
		return ret.toArray(new String[ret.size()]);
	}

}
