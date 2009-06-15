package com.gentics.cr.lucene.search;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRException;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.util.CRUtil;
/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class LuceneRequestProcessor extends RequestProcessor {

	protected static Logger log = Logger.getLogger(LuceneRequestProcessor.class);
	protected static Logger ext_log = Logger.getLogger(LuceneRequestProcessor.class+".extended");
	private CRSearcher searcher = null;
	protected String name=null;
	
	protected String path=null;
	protected String idAttribute = null;
	protected String [] searchedAttributes = null;
	protected int count = 30;
	
	/**
	 * Create new instance of LuceneRequestProcessor
	 * @param config
	 * @throws CRException
	 */
	public LuceneRequestProcessor(CRConfig config) throws CRException {
		super(config);
		this.name=config.getName();
		//LOAD ADDITIONAL CONFIG
		loadConfig();
		this.searcher = new CRSearcher(this.path);
		
	}
	
	/**
	 * Load additional Config from file
	 */
	protected void loadConfig()
	{
		//TODO Manage this over config
		Properties props = new Properties();
		try {
			String confpath = CRUtil.resolveSystemProperties("${com.gentics.portalnode.confpath}/rest/"+this.name+".properties");
			
			props.load(new FileInputStream(confpath));
			
			for (Iterator<Entry<Object,Object>> i = props.entrySet().iterator() ; i.hasNext() ; ) {
				Map.Entry<Object,Object> entry = (Entry<Object,Object>) i.next();
				Object value = entry.getValue();
				Object key = entry.getKey();
				this.setProperty((String)key, (String)value);
			}
			
		} catch (FileNotFoundException e1) {
			log.error("Could not load configuration file at: "+CRUtil.resolveSystemProperties("${com.gentics.portalnode.confpath}/rest/"+this.name+".properties")+"!");
		} catch (IOException e1) {
			log.error("Could not load configuration file at: "+CRUtil.resolveSystemProperties("${com.gentics.portalnode.confpath}/rest/"+this.name+".properties")+"!");
		}catch(NullPointerException e){
			log.error("Could not load configuration file at: "+CRUtil.resolveSystemProperties("${com.gentics.portalnode.confpath}/rest/"+this.name+".properties")+"!");
			e.printStackTrace();
		}
	}
	
	/**
	 * Set Properties that are read in from config file
	 * @param key
	 * @param value
	 */
	protected void setProperty(String key, String value)
	{
		//TODO Manage this over config
		if(key instanceof String)
		{
			if("INDEXLOCATION".equalsIgnoreCase(key))
			{
				this.path = value;
			}
			else if("IDATTRIBUTE".equalsIgnoreCase(key))
			{
				this.idAttribute = value;
			}
			else if("SEARCHEDATTRIBUTES".equalsIgnoreCase(key))
			{
				this.searchedAttributes = value.split(",");
			}
			else if("SEARCHCOUNT".equalsIgnoreCase(key))
			{
				this.count = Integer.parseInt(value);
			}
			
		}
	}
	
	/**
	 * @param request - CRRequest containing the query in RequestFilter
	 * @param doNavigation - if set to true there will be generated explanation output to the explanation logger of CRSearcher
	 * @return search result as Collection of CRResolvableBean
	 */
	public Collection<CRResolvableBean> getObjects(CRRequest request,
			boolean doNavigation) throws CRException {
		ArrayList<CRResolvableBean> result = new ArrayList<CRResolvableBean>();
		int count = request.getCount();
		//IF COUNT IS NOT SET IN THE REQUEST, USE DEFAULT VALUE LOADED FROM CONFIG
		if(count<=0)
			count=this.count;
		if(count<=0)
			log.error("COUNT IS LOWER THAN 0! THIS WILL RESULT IN AN ERROR. OVERTHINK YOUR CONFIG!");
		//GET RESULT
		HashMap<String,Object> searchResult = this.searcher.search(request.getRequestFilter(),this.searchedAttributes,count,doNavigation);
		LinkedHashMap<Float,Document> docs = objectToLinkedHashMapDocuments(searchResult.get("result"));
		if(docs!=null)
		{
			for(Entry<Float,Document> e:docs.entrySet())
			{
				Document doc = e.getValue();
				Float score = e.getKey();
				score.floatValue();
				CRResolvableBean crBean = new CRResolvableBean(doc.get(this.idAttribute));
				if(request.getAttributeArray()!=null)
				{
					List<String> atts = Arrays.asList(request.getAttributeArray());
					if(atts.contains("score"))
					{
						crBean.set("score", score);
					}
				}
				this.ext_log.debug("Found "+crBean.getContentid()+" with score "+score.toString());
				result.add(crBean);
			}
		}
		/*if(doNavigation)
		{
			//NOT IMPLEMENTED YET, BUT WE DO GENERATE MORE EXPLANATION OUTPUT YET
			//log.error("LUCENEREQUESTPROCESSER CAN NOT YET RETURN A TREE STRUCTURE");
		}*/
		return result;
	}
	
	
	@SuppressWarnings("unchecked")
	private LinkedHashMap<Float,Document> objectToLinkedHashMapDocuments(Object obj)
	{
		return((LinkedHashMap<Float,Document>) obj);
	}

}
