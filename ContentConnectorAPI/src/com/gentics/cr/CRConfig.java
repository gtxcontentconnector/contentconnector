package com.gentics.cr;

import java.util.ArrayList;
import java.util.Properties;

import com.gentics.api.lib.datasource.Datasource;
import com.gentics.cr.plink.PathResolver;
import com.gentics.cr.template.ITemplateManager;

/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public interface CRConfig {

	/**
	 * Creates Datasource from Config
	 * @return Datasource
	 */
	public Datasource getDatasource();

	/**
	 * Returns a PathResolver to resolve Paths of a ContentObject
	 * @return PathResolver
	 */
	public PathResolver getPathResolver();

	/**
	 * Returns The configured PlinkTemplate
	 * @return String PlinkTemplate
	 */
	public String getPlinkTemplate();
	
	/**
	 * Returns the Name of the current Config (ServletName, PortletName, ApplicationName,...)
	 * @return String Name
	 */
	public String getName();
	
	/**
	 * Returns the configured Response Encoding
	 * @return String Encoding
	 */
	public String getEncoding();
	
	/**
	 * Returns a string to identify binary objects e.g. 10008
	 * @return String BinaryType
	 */
	public String getBinaryType();
	
	/**
	 * Returns a string to identify folder objects e.g. 10002
	 * @return String FolderType
	 */
	public String getFolderType();
	
	/**
	 * Returns a string to identify page objects e.g. 10007
	 * @return String PageType
	 */
	public String getPageType();
	
	/**
	 * Returns the configured ApplicationRule. This Rule is intended to be added to each query, in order to provide a server side filtering method (personalization)
	 * @return String Application Rule
	 */
	public String getApplicationRule();
	
	/**
	 * Returns the Properties Map
	 * @return Properties
	 */
	public Properties getProps();
	
	/**
	 * Returns if the Application is running in portal.node compatibility mode => Velocity turned off
	 * @return boolean Portal.Node Compatibility Mode
	 */
	public boolean getPortalNodeCompMode();
	
	/**
	 * Returns an instance of the configured Template Manager to render Velocity
	 * @return ITemplateManager
	 */
	public ITemplateManager getTemplateManager();

	/**
	 * XML Url for XMLRequest Processor
	 * @return String 
	 */
	public String getXmlUrl();
	
	/**
	 * XSLT Url for XMLRequest Processor
	 * @return String 
	 */
	public String getXsltUrl();
	
	/**
	 * Contentid Regex for XMLRequest Processor
	 * @return String 
	 */
	public String getContentidRegex();
	
	/**
	 * Chain of filter classes that is performed on the result
	 * @return String 
	 */
	public ArrayList<String> getFilterChain();
	
	/**
	 * Custom Permission for XMLRequest Processor
	 * @return String 
	 */
	public String getObjectPermissionAttribute();
	
	/**
	 * Custom Permission for XMLRequest Processor
	 * @return String 
	 */
	public String getUserPermissionAttribute();
	
	/**
	 * Defines if RequestProcessor is to use shared caches with Portal.Node
	 * @return boolean
	 */
	public boolean useSharedCache();
	
	/**
	 * Defines if the BinaryContainer translates contentidurls eg: /ContentRepository/pcr_bin/90033.305/Quicklinks.png to /ContentRepository/pcr_bin?contentid=90033.305&contentdisposition=Quicklinks.png
	 * @return
	 */
	public boolean usesContentidUrl();
	
	/**
	 * Sets response encoding
	 * @param encoding
	 */
	public void setEncoding(String encoding);
	
	/**
	 * Returns a new instance of the RequestProcessor configured in the config with the given requestProcessorId
	 * @param requestProcessorId
	 * @return RequestProcessor
	 * @throws CRException
	 */
	public RequestProcessor getNewRequestProcessorInstance(int requestProcessorId) throws CRException;
	
	
}
