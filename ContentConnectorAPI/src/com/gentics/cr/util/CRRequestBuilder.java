package com.gentics.cr.util;

import java.util.ArrayList;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRRequest;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.search.LuceneRequestProcessor;
import com.gentics.cr.rest.ContentRepository;
import com.gentics.cr.rest.javabin.JavaBinContentRepository;
import com.gentics.cr.rest.javaxml.JavaXmlContentRepository;
import com.gentics.cr.rest.json.JSONContentRepository;
import com.gentics.cr.rest.php.PHPContentRepository;
import com.gentics.cr.rest.rss.RomeContentRepository;
import com.gentics.cr.rest.velocity.VelocityContentRepository;
import com.gentics.cr.rest.xml.MnogosearchXmlContentRepository;
import com.gentics.cr.rest.xml.XmlContentRepository;
/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class CRRequestBuilder {

	protected RepositoryType repotype;
	protected boolean isDebug=false;
	protected boolean metaresolvable=false;
	protected String highlightquery;
	protected String filter;
	protected String start;
	protected String count;
	protected String debug;
	protected String type;
	protected String contentid;
	protected String[] attributes;
	protected String[] sorting;
	protected String[] plinkattributes;
	protected String[] permissions;
	protected String[] options;
	protected Object request;
	protected Object response;
	
	private Logger logger = Logger.getLogger(CRRequestBuilder.class);
	
	/**
	 * Returns String Array of Attributes to request
	 * @return
	 */
	public String[] getAttributeArray()
	{
		return(this.attributes);
	}
	
	/**
	 * Returns Array of Options
	 * @return
	 */
	public String[] getOptionArray()
	{
		return(this.options);
	}
	
	/**
	 * Get Type of ContentRepository
	 * @return
	 */
	public RepositoryType getRepositoryType()
	{
		return this.repotype;
	}
	
	/**
	 * Returns true if this is a debug request
	 * @return
	 */
	public boolean isDebug()
	{
		return this.isDebug;
	}
	
	/**
	 * Create new Instance
	 * @param request
	 */
	public CRRequestBuilder(PortletRequest request)
	{
		this.request = request;
					
		this.filter = (String) request.getParameter("filter");
		this.contentid = (String) request.getParameter("contentid");
		this.start = (String) request.getParameter("start");
		this.count = request.getParameter("count");
		this.sorting = request.getParameterValues("sorting");
		this.attributes =  this.prepareAttributesArray(request.getParameterValues("attributes"));
		this.plinkattributes =  request.getParameterValues("plinkattributes");
		this.debug = (String)request.getParameter("debug");
		this.permissions = request.getParameterValues("permissions"); 
		this.options = request.getParameterValues("options");
		this.type=request.getParameter("type");
		this.isDebug = (request.getParameter("debug")!=null && request.getParameter("debug").equals("true"));
		this.metaresolvable = Boolean.parseBoolean(request.getParameter(LuceneRequestProcessor.META_RESOLVABLE_KEY));
		this.highlightquery = request.getParameter(LuceneRequestProcessor.HIGHLIGHT_QUERY_KEY);
		//if filter is not set and contentid is => use contentid instad
		if (("".equals(filter) || filter == null)&& contentid!=null && !contentid.equals("")){
			filter = "object.contentid ==" + contentid;
		}
		//SET PERMISSIONS-RULE
		filter = this.createPermissionsRule(filter, permissions);
		
		setRepositoryType(this.type);
		
		//Set debug flag
		if("true".equals(debug))
			this.isDebug=true;

	}
	
	/**
	 * 
	 *	Create New Instance
	 * @param request
	 */
	public CRRequestBuilder(HttpServletRequest request)
	{
		this.request = request;
		this.filter = (String) request.getParameter("filter");
		this.contentid = (String) request.getParameter("contentid");
		this.count = request.getParameter("count");
		this.start = (String) request.getParameter("start");
		this.sorting = request.getParameterValues("sorting");
		this.attributes =  this.prepareAttributesArray(request.getParameterValues("attributes"));
		this.plinkattributes =  request.getParameterValues("plinkattributes");
		this.debug = (String)request.getParameter("debug");
		this.permissions = request.getParameterValues("permissions"); 
		this.options = request.getParameterValues("options");
		this.type=request.getParameter("type");
		this.isDebug = (request.getParameter("debug")!=null && request.getParameter("debug").equals("true"));
		this.metaresolvable = Boolean.parseBoolean(request.getParameter(LuceneRequestProcessor.META_RESOLVABLE_KEY));
		this.highlightquery = request.getParameter(LuceneRequestProcessor.HIGHLIGHT_QUERY_KEY);
		
		//Parameters used in mnoGoSearch for easier migration (Users should use type=MNOGOSEARCHXML)
		if ( this.filter == null ) { this.filter = request.getParameter("q"); }
		if ( this.count == null ) { this.count = request.getParameter("ps"); }
		if ( this.start == null && this.count != null) {
			String numberOfPage = (String) request.getParameter("np");
			this.start = (Integer.parseInt(numberOfPage) * Integer.parseInt(this.count)) + "";
		}
		
		
		
		//if filter is not set and contentid is => use contentid instad
		if (("".equals(filter) || filter == null)&& contentid!=null && !contentid.equals("")){
			filter = "object.contentid == '" + contentid+"'";
		}
		//SET PERMISSIONS-RULE
		filter = this.createPermissionsRule(filter, permissions);
		
		
		
		
		setRepositoryType(this.type);
		
		//Set debug flag
		if("true".equals(debug))
			this.isDebug=true;
	}
	
	
	private void setRepositoryType(String type) {
		//Initialize RepositoryType
		if(type!=null)
		{
			if(type.equalsIgnoreCase("JSON"))this.repotype=RepositoryType.JSON;
			else if(type.equalsIgnoreCase("PHP"))this.repotype=RepositoryType.PHP;
			else if(type.equalsIgnoreCase("JAVAXML"))this.repotype=RepositoryType.JAVAXML;
			else if(type.equalsIgnoreCase("RSS"))this.repotype = RepositoryType.RSS;
			else if(type.equalsIgnoreCase("MNOGOSEARCHXML"))this.repotype=RepositoryType.MNOGOSEARCHXML;
			else if(type.equalsIgnoreCase("JAVABIN"))this.repotype=RepositoryType.JAVABIN;
			else if(type.equalsIgnoreCase("VELOCITY"))this.repotype=RepositoryType.VELOCITY;
			else this.repotype=RepositoryType.XML;
		}
		else
		{
			this.repotype=RepositoryType.XML;
		}
	}

	public CRRequestBuilder(HttpServletRequest request,GenericConfiguration defaultparameters){
		this(request);
		if(this.type == null){
			this.type = (String) defaultparameters.get("type");
			setRepositoryType(this.type);
		}
	}
	
	
	/**
	 * Creates a CRRequest
	 * @return
	 */
	public CRRequest getCRRequest()
	{
		CRRequest req = new CRRequest(filter,start,count,sorting,attributes,plinkattributes);
		req.setContentid(this.contentid);
		req.setRequest(this.request);
		req.setResponse(this.response);
		req.set(LuceneRequestProcessor.META_RESOLVABLE_KEY, this.metaresolvable);
		if(this.highlightquery!=null)req.set(LuceneRequestProcessor.HIGHLIGHT_QUERY_KEY, this.highlightquery);
		return req;
	}
	
	/**
	 * returns the Request Object
	 * @return
	 */
	public Object getRequest(){
		return this.request;
	}
	
	//Wrapps filter rule with the given set of permissions
	protected String createPermissionsRule(String filter, String[] permissions)
	{
		String ret=filter;
		if((permissions != null) && (permissions.length>0))
		{
			if((filter!=null)&&(!filter.equals("")))
			{
				ret="("+filter+") AND object.permissions CONTAINSONEOF "+CRUtil.prepareParameterArrayForRule(permissions);
			}
			else
			{
				ret="object.permissions CONTAINSONEOF "+CRUtil.prepareParameterArrayForRule(permissions);
			}
		}
		return ret;
	}
	
	protected String[] prepareAttributesArray(String[] attributes)
	{
		ArrayList<String> ret = new ArrayList<String>();
		if(attributes!=null)
		{
			for(String item: attributes)
			{
				if(item.contains(","))
				{
					String[] items = item.split(",");
					for(String subatt:items)
					{
						ret.add(subatt);
					}
				}
				else
				{
					ret.add(item);
				}
			}
		}
		return ret.toArray(new String[ret.size()]);
	}
	
	
	/**
	 * Create the ContentRepository for this request
	 * @param encoding
	 * @return
	 */
	public ContentRepository getContentRepository(String encoding)
	{
		ContentRepository cr = null;
		switch(this.getRepositoryType())
		{
			case JSON:
				cr = new JSONContentRepository(this.getAttributeArray(),encoding,this.getOptionArray());
				break;
			case PHP:
				cr = new PHPContentRepository(this.getAttributeArray(),encoding,this.getOptionArray());
				break;
			case JAVAXML:
				cr = new JavaXmlContentRepository(this.getAttributeArray(),encoding,this.getOptionArray());
				break;
			case RSS:
				cr = new RomeContentRepository(this.getAttributeArray(),encoding,this.getOptionArray());
				break;
			case MNOGOSEARCHXML:
				cr = new MnogosearchXmlContentRepository(this.getAttributeArray(), encoding, this.getOptionArray());
				break;
			case JAVABIN:
				cr = new JavaBinContentRepository(this.getAttributeArray(), encoding, this.getOptionArray());
				break;
			case VELOCITY:
				logger.debug("Velocity can only be initialized via getContentRepository(encoding,config)");
			default:
				cr = new XmlContentRepository(this.getAttributeArray(),encoding,this.getOptionArray());
		}
		return cr;
	}
	/**
	 * Create the ContentRepository for this request and give it the configuration. This is needed for the VelocityContentRepository
	 * @param encoding Output encoding should be used
	 * @param configUtil Config to get the Velocity Engine from
	 * @return ContentRepository with the given settings.
	 */
	
	public ContentRepository getContentRepository(String encoding, CRConfigUtil configUtil)
	{
		ContentRepository cr;
		switch (this.getRepositoryType()) {
			case VELOCITY:
				cr = new VelocityContentRepository(this.getAttributeArray(), encoding, this.getOptionArray(), configUtil);
				break;
			default:
				cr = getContentRepository(encoding);
		}
		return cr;
	}
}
