package com.gentics.cr.configuration;

import java.util.ArrayList;
import java.util.Properties;

import com.gentics.api.lib.datasource.Datasource;
import com.gentics.cr.CRConfig;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.plink.PathResolver;
import com.gentics.cr.template.ITemplateManager;

/**
 * Configuration usefull for initalization of a request processor without a real config.
 * @author bigbear3001
 */
public class SimpleCRConfig extends CRConfig {

	
	/**
	 * generated unique serialization id.
	 */
	private static final long serialVersionUID = -1210371851714131158L;
	
	private Datasource datasource;
	
	@Override
	public Datasource getDatasource() {
		return datasource;
	}

	private PathResolver pathResolver;
	
	@Override
	public PathResolver getPathResolver() {
		return pathResolver;
	}

	private String plinkTemplate;
	
	@Override
	public String getPlinkTemplate() {
		return plinkTemplate;
	}

	private String name;
	
	@Override
	public String getName() {
		return name;
	}
	
	private String encoding = "UTF-8";
	
	@Override
	public String getEncoding() {
		return encoding;
	}

	private String binaryType = "10008";
	
	@Override
	public String getBinaryType() {
		return binaryType;
	}

	private String folderType = "10002";
	
	@Override
	public String getFolderType() {
		return folderType;
	}

	private String pageType = "10007";
	
	@Override
	public String getPageType() {
		return pageType;
	}

	private String applicationRule;
	
	@Override
	public String getApplicationRule() {
		return applicationRule;
	}

	private Properties props;
	
	@Override
	public Properties getProps() {
		return props;
	}

	@Override
	public boolean getPortalNodeCompMode() {
		return false;
	}

	private ITemplateManager templateManager;
	
	@Override
	public ITemplateManager getTemplateManager() {
		return templateManager;
	}

	private String xmlUrl;
	
	@Override
	public String getXmlUrl() {
		return xmlUrl;
	}

	private String xsltUrl;
	
	@Override
	public String getXsltUrl() {
		return xsltUrl;
	}

	private String contentidRegex;
	
	@Override
	public String getContentidRegex() {
		return contentidRegex;
	}

	private ArrayList<String> filterChain;
	
	@Override
	public ArrayList<String> getFilterChain() {
		return filterChain;
	}

	private String objectPermissionAttribute;
	
	@Override
	public String getObjectPermissionAttribute() {
		return objectPermissionAttribute;
	}

	private String userPermissionAttribute;
	
	@Override
	public String getUserPermissionAttribute() {
		return userPermissionAttribute;
	}

	private boolean sharedCache = false;
	
	@Override
	public boolean useSharedCache() {
		return sharedCache;
	}

	private boolean usesContentidUrl = false;
	
	@Override
	public boolean usesContentidUrl() {
		return usesContentidUrl;
	}

	
	
	@Override
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	@Override
	public RequestProcessor getNewRequestProcessorInstance(
			int requestProcessorId) throws CRException {
		return null;
	}

}
