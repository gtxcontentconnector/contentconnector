package com.gentics.cr;

import java.util.ArrayList;
import java.util.Properties;

import com.gentics.api.lib.datasource.Datasource;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.plink.PathResolver;
import com.gentics.cr.template.ITemplateManager;

/**
 * Configuration for a ContentConnector ContenRepository.
 * Last changed: $Date: 2010-04-01 15:39:36 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 547 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public abstract class CRConfig extends GenericConfiguration {

	/**
	 * Generated unique serial version id.
	 */
	private static final long serialVersionUID = -5020784946538085838L;

	/*
	 * Reserved Attributes for Advanced Plinkreplacing without Velocity
	 * @link com.gentics.cr.plink.PathResolver and com.gentics.cr.plink.PlinkProcessor
	 */

	/**
	 * configuration property name for advanced plinkreplacing.
	 */
	public static final String ADVPLR_KEY = "ADVPLR";

	/**
	 * configuration property name for advanced plinkreplacing.
	 */
	public static final String ADVPLR_HOST = "ADVPLR_HOST";

	/**
	 * configuration property name for advanced plinkreplacing.
	 */
	public static final String ADVPLR_HOST_FORCE = "ADVPLR_HOST_FORCE";

	/**
	 * configuration property name for filename attribute used by advanced�
	 * plinkreplacing to generate beautiful URLs.
	 */
	public static final String ADVPLR_FN_KEY = "ADVPLR_FILENAME_ATTRIBUTE";
	/**
	 * configuration property name for pub_dir attribute used by advanced
	 * plinkreplacing to generate beautiful URLs.
	 */
	public static final String ADVPLR_PB_KEY = "ADVPLR_PUB_DIR_ATTRIBUTE";

	/**
	 * property key to configure multiple indexes.
	 * {@link com.gentics.cr.util.indexing.IndexLocation#getIndexLocation(CRConfig)}
	 */
	public static final String CR_KEY = "CR";

	/**
	 * Creates Datasource from configuration.
	 * @return Datasource from this configuration
	 */
	public abstract Datasource getDatasource();

	/**
	 * Returns a PathResolver to resolve Paths of a ContentObject.
	 * @return PathResolver
	 */
	public abstract PathResolver getPathResolver();

	/**
	 * Returns The configured PlinkTemplate.
	 * @return String PlinkTemplate
	 */
	public abstract String getPlinkTemplate();

	/**
	 * Returns the Name of the configuration.
	 * @return String Name
	 */
	public abstract String getName();

	/**
	 * Returns the configured Response Encoding.
	 * @return String Encoding
	 */
	public abstract String getEncoding();

	/**
	 * Returns a string to identify binary objects e.g. 10008
	 * @return String BinaryType
	 */
	public abstract String getBinaryType();

	/**
	 * Returns a string to identify folder objects e.g. 10002
	 * @return String FolderType
	 */
	public abstract String getFolderType();

	/**
	 * Returns a string to identify page objects e.g. 10007
	 * @return String PageType
	 */
	public abstract String getPageType();

	/**
	 * Returns the configured ApplicationRule. This Rule is intended to be added
	 * to each query, in order to provide a server side filtering method
	 * (personalization).
	 * @return String Application Rule
	 */
	public abstract String getApplicationRule();

	/**
	 * Returns the Properties Map.
	 * @return Properties
	 */
	public abstract Properties getProps();

	/**
	 * Returns if the Application is running in portal.node compatibility mode
	 * => Velocity turned off.
	 * @return boolean Portal.Node Compatibility Mode
	 */
	public abstract boolean getPortalNodeCompMode();

	/**
	 * Returns an instance of the configured TemplateManager to render Velocity.
	 * @return ITemplateManager
	 */
	public abstract ITemplateManager getTemplateManager();

	/**
	 * XML Url for XMLRequest Processor.
	 * @return String 
	 */
	public abstract String getXmlUrl();

	/**
	 * XSLT Url for XMLRequest Processor.
	 * @return String 
	 */
	public abstract String getXsltUrl();

	/**
	 * Contentid Regex for XMLRequest Processor.
	 * @return String 
	 */
	public abstract String getContentidRegex();

	/**
	 * Chain of filter classes that is performed on the result.
	 * @return String 
	 */
	public abstract ArrayList<String> getFilterChain();

	/**
	 * Custom Permission for XMLRequest Processor.
	 * @return String 
	 */
	public abstract String getObjectPermissionAttribute();

	/**
	 * Custom Permission for XMLRequest Processor.
	 * @return String 
	 */
	public abstract String getUserPermissionAttribute();

	/**
	 * Defines if RequestProcessor is to use shared caches with Portal.Node.
	 * @return boolean
	 */
	public abstract boolean useSharedCache();

	/**
	 * Defines if the BinaryContainer translates contentidurls eg:
	 * /ContentRepository/pcr_bin/90033.305/Quicklinks.png to
	 * "/ContentRepository/pcr_bin?contentid=90033.305&contentdisposition=
	 * Quicklinks.png".
	 * @return <code>true</code> if contentidurls are translated.
	 */
	public abstract boolean usesContentidUrl();

	/**
	 * Sets response encoding.
	 * @param encoding - encoding to use
	 */
	public abstract void setEncoding(String encoding);

	/**
	 * Returns a new instance of the RequestProcessor configured in the
	 * configuration with the given requestProcessorId.
	 * @param requestProcessorId id of the {@link RequestProcessor}
	 * @return RequestProcessor - RequestProcessor for the id. 
	 * @throws CRException in case no {@link RequestProcessor}s are configured
	 */
	public abstract RequestProcessor getNewRequestProcessorInstance(int requestProcessorId) throws CRException;

}
