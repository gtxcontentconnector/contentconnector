package com.gentics.cr.lucene.indexer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gentics.api.portalnode.connector.PortalConnectorFactory;
import com.gentics.contentnode.datasource.CNWriteableDatasource;
/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class IndexerCRConfig {

	public IndexerCRConfig(String name)
	{
		this.crName = name;
	}
	
	protected Map<String,String> datasourceHandleProperties = new HashMap<String,String>();
	
	protected String rule = null;
	
	protected List<String> indexedAttributes = new ArrayList<String>();
	
	protected List<String> containedAttributes = new ArrayList<String>();
	
	protected String crName = null;
	
	protected String idattribute = null;
	
	protected String htmlattribute=null;
	
	protected String stopwordfilepath=null;
	
	public File getStopWordFile()
	{
		if(this.stopwordfilepath!=null && !this.stopwordfilepath.equals(""))
		{
			File f = new File(this.stopwordfilepath);
			
			if(f.exists())
			{
				return(f);
			}
		}
		return(null);
	}
	
	public String getStopwordfilepath() {
		return stopwordfilepath;
	}

	public void setStopwordfilepath(String stopwordfilepath) {
		this.stopwordfilepath = stopwordfilepath;
	}

	public String getHtmlattribute() {
		return htmlattribute;
	}

	public void setHtmlattribute(String htmlattribute) {
		this.htmlattribute = htmlattribute;
	}

	public String getIdattribute() {
		if(idattribute!=null)
			return idattribute;
		
		
		return "";
	}

	public void setIdattribute(String idattribute) {
		this.idattribute = idattribute;
	}

	public CNWriteableDatasource getDatasource()
	{
		CNWriteableDatasource ds = (CNWriteableDatasource)PortalConnectorFactory.createWriteableDatasource(this.datasourceHandleProperties);
		return(ds);
	}

	public String getRule() {
		return rule;
	}

	public List<String> getIndexedAttributes() {
		return indexedAttributes;
	}

	public List<String> getContainedAttributes() {
		return containedAttributes;
	}
	
	public void putDatasourceHandleProperty(String key, String prop)
	{
		this.datasourceHandleProperties.put(key, prop);
	}
	
	public void setRule(String rule)
	{
		this.rule = rule; 
	}
	
	public void setIndexedAttributes(String[] attributes)
	{
		this.indexedAttributes.addAll(Arrays.asList(attributes));
	}
	
	public void setContainedAttributes(String[] attributes)
	{
		this.containedAttributes.addAll(Arrays.asList(attributes));
	}

	public String getCrName() {
		return crName;
	}

	public void setCrName(String crName) {
		this.crName = crName;
	}
}
