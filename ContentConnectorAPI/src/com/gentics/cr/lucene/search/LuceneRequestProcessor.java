package com.gentics.cr.lucene.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRException;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class LuceneRequestProcessor extends RequestProcessor {

	protected static Logger log = Logger.getLogger(LuceneRequestProcessor.class);
	protected static Logger ext_log = Logger.getLogger(LuceneRequestProcessor.class);
	private CRSearcher searcher = null;
	protected String name=null;
	
	
	
	/**
	 * Create new instance of LuceneRequestProcessor
	 * @param config
	 * @throws CRException
	 */
	public LuceneRequestProcessor(CRConfig config) throws CRException {
		super(config);
		this.name=config.getName();
		
		this.searcher = new CRSearcher(config);
		
	}
	
	private static final String SEARCH_COUNT_KEY = "SEARCHCOUNT";
	
	private static final String ID_ATTRIBUTE_KEY = "idAttribute";
	
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
			count=new Integer((String)this.config.get(SEARCH_COUNT_KEY));
		if(count<=0)
			log.error("COUNT IS LOWER THAN 0! THIS WILL RESULT IN AN ERROR. OVERTHINK YOUR CONFIG!");
		//GET RESULT
		HashMap<String,Object> searchResult = this.searcher.search(request.getRequestFilter(),getSearchedAttributes(),count,doNavigation);
		LinkedHashMap<Document,Float> docs = objectToLinkedHashMapDocuments(searchResult.get("result"));
		if(docs!=null)
		{
			String idAttribute = (String)this.config.get(ID_ATTRIBUTE_KEY);
			for(Entry<Document,Float> e:docs.entrySet())
			{
				Document doc = e.getKey();
				Float score = e.getValue();
				CRResolvableBean crBean = new CRResolvableBean(doc.get(idAttribute));
				if(request.getAttributeArray()!=null)
				{
					List<String> atts = Arrays.asList(request.getAttributeArray());
					for(String s:atts)
					{
						String val = doc.get(s);
						crBean.set(s, val);
					}
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
	private LinkedHashMap<Document,Float> objectToLinkedHashMapDocuments(Object obj)
	{
		return((LinkedHashMap<Document,Float>) obj);
	}
	
	private static final String SEARCHED_ATTRIBUTES_KEY = "searchedAttributes";
	
	private String[] getSearchedAttributes()
	{
		String sa = (String)this.config.get(SEARCHED_ATTRIBUTES_KEY);
		String[] ret=null;
		if(sa!=null)
		{
			ret = sa.split(",");
		}
		return ret;
	}

}
