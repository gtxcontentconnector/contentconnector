package com.gentics.cr;

import com.gentics.cr.exceptions.CRException;
import org.apache.log4j.Logger;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * The LDAP request processor can be used to request objects from an LDAP server.
 * It was developed to provide means to index LDAP content for the contentconnector search.
 * For available config options please see the public static properties on this class.
 *
 * @author Sebastian Vogel
 */
public class LDAPRequestProcessor extends RequestProcessor {
	private static final Logger logger = Logger.getLogger(LDAPRequestProcessor.class);

	/**
	 * The ldap server host name config key
	 */
	public static final String LDAP_HOST_CONFIG_KEY = "ldaphost";
	/**
	 * The LDAP server port config key
	 */
	public static final String LDAP_PORT_CONFIG_KEY = "ldapport";
	/**
	 * The LDAP user config key
	 */
	public static final String LDAP_USER_CONFIG_KEY = "ldapuser";
	/**
	 * The LDAP password config key
	 */
	public static final String LDAP_PASS_CONFIG_KEY = "ldappassword";
	/**
	 * The LDAP search base dn config key for the optional base dn parameter added to every search
	 */
	public static final String LDAP_SEARCH_BASE_DN_CONFIG_KEY = "ldapsearcbbasedn";
	/**
	 * The LDAP id attribute config key this attribute is returned with every search (defaults to "cn")
	 */
	public static final String LDAP_ID_ATTRIBUTE_CONFIG_KEY = "ldapidattribute";


	private String ldapHost;
	private String ldapPort;
	private String ldapUser;
	private String ldapPassword;
	private String ldapSearchBaseDN;
	private String ldapIdAttribute;

	public LDAPRequestProcessor(CRConfig config) throws CRException {
		super(config);
		CRConfigUtil configUtil = (CRConfigUtil) config;
		ldapHost = configUtil.getString(LDAP_HOST_CONFIG_KEY);
		ldapPort = configUtil.getString(LDAP_PORT_CONFIG_KEY);
		ldapUser = configUtil.getString(LDAP_USER_CONFIG_KEY);
		ldapPassword = configUtil.getString(LDAP_PASS_CONFIG_KEY);
		ldapSearchBaseDN = configUtil.getString(LDAP_SEARCH_BASE_DN_CONFIG_KEY, "");
		ldapIdAttribute = configUtil.getString(LDAP_ID_ATTRIBUTE_CONFIG_KEY, "cn");
	}

	/**
	 * Setup the the context for the LDAP query
	 * @return the ldap context
	 * @throws CRException when the context could not be setup
	 */
	private DirContext getLdapContext () throws CRException {
		Hashtable<String, String> ldapEnv = new Hashtable<>();
		ldapEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		ldapEnv.put(Context.PROVIDER_URL, "ldap://" + ldapHost + ":" + ldapPort);
		ldapEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
		ldapEnv.put(Context.SECURITY_PRINCIPAL, ldapUser);
		ldapEnv.put(Context.SECURITY_CREDENTIALS, ldapPassword);
		// add this config to use the same url resolver in all environments
		ldapEnv.put(Context.URL_PKG_PREFIXES, "com.sun.jndi.url.ldap");
		DirContext ctx;
		try {
			ctx = new InitialDirContext(ldapEnv);
		} catch (NamingException e) {
			throw new CRException("Could not setup ldap context", e);
		}
		return ctx;
	}

	/**
	 * Close a previously opened LDAP context
	 * @param ctx the context to close
	 */
	private void closeContext(DirContext ctx) {
		if (ctx != null) {
			try {
				ctx.close();
			} catch (NamingException e) {
				logger.error("Could not close LDAP context", e);
			}
		}
	}

	/**
	 * Prepare the CR-request attribute for use in the LDAP search.
	 * Add the ldapIdAttribute and remove the "contentid" attribute.
	 * @param requestAttributes the CRRequest attributes to fetch
	 * @return the attributes to use in the ldap search
	 */
	private String[] prepareLdapAttributes(String[] requestAttributes) {
		List<String> ldapAttributes = new ArrayList<>();
		ldapAttributes.add(ldapIdAttribute);
		for (String attr : requestAttributes) {
			if (!"contentid".equals(attr) && !ldapAttributes.contains(attr)) {
				ldapAttributes.add(attr);
			}
		}
		return ldapAttributes.toArray(new String[] {});
	}

	/**
	 * Normalize attribute for comparing the returned attribute from the ldap-search
	 * with the requested attribute.
	 * @param attribute the attribute to normalize
	 * @return the normalized attribute
	 */
	private String normalizeAttribute(String attribute) {
		return attribute.toLowerCase();
	}

	/**
	 * Normalize the attributes in a collection and return a Map where
	 * the key is the normalized attribute and the value is the original attribute name.
	 * @param attributesArray the attributes to normalize
	 * @return a map containing the normalized attributes as keys
	 */
	private Map<String, String> getNormalizedAttributesMap(String[] attributesArray) {
		HashMap<String, String> normalizedAttributes = new HashMap<>();
		for (String attribute : attributesArray) {
			normalizedAttributes.put(normalizeAttribute(attribute), attribute);
		}
		return normalizedAttributes;
	}

	/**
	 * Do the actual ldap query.
	 * @param ctx the ldap context
	 * @param request the request
	 * @return all the found objects as beans
	 * @throws CRException when the ldap search returned errors
	 */
	private Collection<CRResolvableBean> doLdapQuery(DirContext ctx, CRRequest request) throws CRException {
		SearchControls ctrls = new SearchControls();
		ctrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		ctrls.setReturningAttributes(prepareLdapAttributes(request.getAttributeArray()));
		NamingEnumeration<SearchResult> ldapResult;
		List<CRResolvableBean> results = new ArrayList<>();
		try {
			ldapResult = ctx.search(ldapSearchBaseDN, request.getRequestFilter(), ctrls);
			Map<String, String> normalizedRequestAttributes = getNormalizedAttributesMap(request.getAttributeArray());
			while (ldapResult.hasMoreElements()) {
				results.add(convertToResolvable(ldapResult.nextElement(), normalizedRequestAttributes));
			}
		} catch (NamingException e) {
			throw new CRException("Could not search ldap with filter: " + request.getRequestFilter(), e);
		}
		return results;
	}

	@Override
	public Collection<CRResolvableBean> getObjects(CRRequest request, boolean doNavigation) throws CRException {
		DirContext ctx = getLdapContext();
		try {
			return doLdapQuery(ctx, request);
		} finally {
			closeContext(ctx);
		}
	}

	/**
	 * Convert a single LDAP search result to a resolvable bean
	 * @param singleResult the single search result
	 * @return the resolvable created from the ldap search result.
	 * @throws NamingException when the attributes of the search results could not be accessed
	 */
	private CRResolvableBean convertToResolvable(SearchResult singleResult,  Map<String, String> normalizedRequestAttributes) throws NamingException {
		CRResolvableBean bean = new ComparableBean();
		Attributes attributes = singleResult.getAttributes();
		if (null != attributes)  {
			for (NamingEnumeration ae = attributes.getAll(); ae.hasMoreElements();) {
				Attribute attr = (Attribute) ae.next();
				String attributeID = normalizeAttribute(attr.getID());
				Enumeration vals = attr.getAll();
				if (vals.hasMoreElements() && normalizedRequestAttributes.containsKey(attributeID)) {
					bean.set(normalizedRequestAttributes.get(attributeID), vals.nextElement());
				}
			}
		}
		bean.setContentid(bean.getString(ldapIdAttribute));
		return bean;
	}

	@Override
	public void fillAttributes(Collection<CRResolvableBean> col, CRRequest request, String idAttribute) throws CRException {
		// clone the request
		CRRequest fillRequest = request.Clone();
		if (null == col || col.isEmpty()) {
			// do nothing
			return;
		}
		// build a request filter to search for the beans
		StringBuilder sb = new StringBuilder();
		if (col.size() > 1) {
			sb.append("(|");
		}
		for (CRResolvableBean bean : col) {
			sb.append("(").append(ldapIdAttribute).append("=").append(bean.get(ldapIdAttribute)).append(")");
		}
		if (col.size() > 1) {
			sb.append(")");
		}
		fillRequest.setRequestFilter(sb.toString());
		DirContext ctx = getLdapContext();
		Collection<CRResolvableBean> results;
		try {
			results = doLdapQuery(ctx, fillRequest);
		} finally {
			closeContext(ctx);
		}
		for (CRResolvableBean bean : results) {
			for (CRResolvableBean colBean : col) {
				if (colBean.getContentid().equals(bean.getContentid())) {
					mergeBeans(colBean, bean);
					break;
				}
			}
		}
	}

	/**
	 * Merge the attributes of one bean into another
	 * @param target the target bean
	 * @param source the source bean
	 */
	private void mergeBeans(CRResolvableBean target, CRResolvableBean source) {
		for (String key : source.getAttrMap().keySet()) {
			target.set(key, source.get(key));
		}
	}

	@Override
	public void finalize() {
		// do nothing because we have no static resources
	}
}
