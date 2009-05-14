package com.gentics.cr.rest.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.gentics.cr.CRException;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.rest.ContentRepository;

/**
 * @author haymo
 *
 * Implementaion of XML representation for a REST contentrepositroy.
 *  
 */
public class MnogosearchXmlContentRepository extends ContentRepository {

	private static final long serialVersionUID = 0004L;

	private Element rootElement;

	private Document doc;

	private DOMSource src;

	public MnogosearchXmlContentRepository(String[] attr) {
		
		super(attr);

		// Create XML Document
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		this.setResponseEncoding("utf-8");
		try {
			
			builder = factory.newDocumentBuilder();
			this.doc = builder.newDocument();
		
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		this.src = new DOMSource(doc);
		
		// Create Root Element
		this.rootElement = doc.createElement("Contentrepository");
		doc.appendChild(rootElement);

	}
	public MnogosearchXmlContentRepository(String[] attr, String encoding) {
		
		super(attr);

		// Create XML Document
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		this.setResponseEncoding(encoding);
		try {
			
			builder = factory.newDocumentBuilder();
			this.doc = builder.newDocument();
		
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		this.src = new DOMSource(doc);
		
		// Create Root Element
		this.rootElement = doc.createElement("Contentrepository");
		doc.appendChild(rootElement);

	}
	
	public MnogosearchXmlContentRepository(String[] attr, String encoding, String[] options) {
		
		super(attr,encoding,options);

		// Create XML Document
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		//this.setResponseEncoding(encoding);
		try {
			
			builder = factory.newDocumentBuilder();
			this.doc = builder.newDocument();
		
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		this.src = new DOMSource(doc);
		
		// Create Root Element
		this.rootElement = doc.createElement("search");
		doc.appendChild(rootElement);
	}
	

	public String getContentType() {
		return "text/xml";
	}
	
	private void clearElement(Element elem)
	{
		if(elem!=null)
		{
			NodeList list = elem.getChildNodes();
		
			//int len = list.getLength();
			for(int i=0;i<list.getLength();i++)
			{
				elem.removeChild(list.item(i));
			}
			NamedNodeMap map = elem.getAttributes();
			//len =map.getLength();
			for(int i=0;i<map.getLength();i++)
			{
				elem.removeAttribute(map.item(i).getNodeName());
			}
		}
	}
	
	public void respondWithError(OutputStream stream,CRException ex, boolean isDebug){
		clearElement(this.rootElement);
		Element errElement = doc.createElement("Error");
		errElement.setAttribute("type",ex.getType());
		errElement.setAttribute("messge",ex.getMessage());
		if(isDebug)
		{
			Element stackTrace = doc.createElement("StackTrace");
			Text text = doc.createCDATASection(ex.getStringStackTrace());
			stackTrace.appendChild(text);
			errElement.appendChild(stackTrace);
		}
		
		this.rootElement.setAttribute("status","error");
		this.rootElement.appendChild(errElement);
		
		//		 output xml
		StreamResult strRes = new StreamResult(stream);
		
		try {
			
			TransformerFactory.newInstance().newTransformer().transform(
					this.src, strRes);
		
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		}
		
	}

	public void toStream(OutputStream stream) throws CRException {
		
		
		
		if(this.resolvableColl.isEmpty())
		{
			Element statusElement = doc.createElement("status");
			statusElement.appendChild(doc.createTextNode("notfound"));
			this.rootElement.appendChild(statusElement);
		}
		else
		{
			//Elements found/status ok
			//this.rootElement.setAttribute("status","ok");
			Element statusElement = doc.createElement("status");
			statusElement.appendChild(doc.createTextNode("found"));
			this.rootElement.appendChild(statusElement);
			
			rootElement.appendChild(createMetaDataNode(doc));
			
			for (Iterator<CRResolvableBean> it = this.resolvableColl.iterator(); it.hasNext();) {
				
				CRResolvableBean crBean = (CRResolvableBean) it.next();
	
				Element objElement = processElement(crBean);
				this.rootElement.appendChild(objElement);
			}
		}
		
		// output xml
		
		
		try {
			OutputStreamWriter wr = new OutputStreamWriter(stream, this.getResponseEncoding());
									
			StreamResult strRes = new StreamResult(wr);
			TransformerFactory.newInstance().newTransformer().transform(
					this.src, strRes);
			wr.flush();
			wr.close();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Element createMetaDataNode(Document doc){
		Element metaElement = doc.createElement("meta");
		
		Element first = doc.createElement("first");
		first.appendChild(doc.createCDATASection("1"));
		metaElement.appendChild(first);
		
		Element last = doc.createElement("last");
		last.appendChild(doc.createCDATASection(this.resolvableColl.size()+""));
		metaElement.appendChild(last);
		
//		//TODO: calculate searchtime
//		Element searchtime = doc.createElement("searchtime");
//		searchtime.appendChild(doc.createCDATASection("1"));
//		metaElement.appendChild(searchtime);

//		//TODO: get wrdstat
//		Element wrdstat = doc.createElement("wrdstat");
//		wrdstat.appendChild(doc.createCDATASection("1"));
//		metaElement.appendChild(wrdstat);
		
		//TODO: calculate total - removed sites
		Element total = doc.createElement("total");
		total.appendChild(doc.createCDATASection(this.resolvableColl.size()+""));
		metaElement.appendChild(total);
		
//		//TODO: calculate total pages
//		Element pages = doc.createElement("pages");
//		pages.appendChild(doc.createCDATASection("1"));
//		metaElement.appendChild(pages);
		
//		//TODO: get searchquery
//		Element query = doc.createElement("query");
//		searchtime.appendChild(doc.createCDATASection(""));
//		metaElement.appendChild(query);
		
//		//TODO: calculate real pagesize
//		Element pagesize = doc.createElement("pagesize");
//		pagesize.appendChild(doc.createCDATASection(this.resolvableColl.size()+""));
//		metaElement.appendChild(pagesize);
		
		return metaElement;
	}
	
	@SuppressWarnings("unchecked")
	private Element processElement(CRResolvableBean crBean) {
		Element objElement = doc.createElement("res");
		if (crBean.getAttrMap() != null && (!crBean.getAttrMap().isEmpty())) {
			//Element attrContainer = doc.createElement("attributes");
			Iterator<String> bit = crBean.getAttrMap().keySet().iterator();
			while (bit.hasNext()) {
				
				String entry = bit.next();
				if(!"".equals(entry))
				{
					
					Object bValue=crBean.getAttrMap().get(entry);
					String value = "";
					if(bValue!=null)
					{
						
						if((!entry.equals("binarycontent")) &&(bValue.getClass().isArray()||bValue.getClass()==ArrayList.class))
						{
							
								Object[] arr;
								if(bValue.getClass()==ArrayList.class)
									arr = ((ArrayList<Object>)bValue).toArray();
								else
									arr= (Object[])bValue;
								for(int i=0;i<arr.length;i++)
								{
									Element attrElement = doc.createElement(entry);
									objElement.appendChild(attrElement);
	//								
									if(arr[i].getClass()==String.class)
									{
										value=(String)arr[i];
									}
									else
									{
										try {
											value = new String(getBytes(bValue));
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
									Text text = doc.createCDATASection(value);
									attrElement.appendChild(text);
								}
							
						}
						else
						{
							Element attrElement = doc.createElement(entry);
							objElement.appendChild(attrElement);
							
							if(entry.equals("binarycontent"))
							{
								
								try
								{
									value=new String((byte[])bValue);
								}
								catch(ClassCastException x)
								{
									try {
										value=new String(getBytes(bValue));
									} catch (IOException e) {
										value=bValue.toString();
										e.printStackTrace();
									}
								}
								//TODO return proper binary content
								//value=(String) bValue.toString();
							}
							else
							{
								if(bValue.getClass()==String.class)
								{
									value=(String)bValue;
								}
								else
								{
									value=bValue.toString();
//									try {
//										value = new String(getBytes(bValue));
//									} catch (IOException e) {
//										// TODO Auto-generated catch block
//										e.printStackTrace();
//									}
								}
	//							if(bValue.getClass()==String.class)
	//							{
	//								value=(String) bValue;
	//							}
	//							else
	//							{
	//								value=(String) bValue.toString();
	//							}
									
							}
							Text text = doc.createCDATASection(value);
							attrElement.appendChild(text);
						}
						
					}
					else
					{
						Element attrElement = doc.createElement(entry);
						objElement.appendChild(attrElement);
						
						Text text = doc.createCDATASection(value);
						attrElement.appendChild(text);
					}
					/*if (value == null) {
						value = "";
					}*/
					
				}
			}
		}
		
		if(crBean.getChildRepository().size()>0)
		{
			Element childContainer = doc.createElement("children");
			
			for (Iterator<CRResolvableBean> it = crBean.getChildRepository().iterator(); it.hasNext();) {
				
				CRResolvableBean chBean = (CRResolvableBean) it.next();
	
				Element chElement = processElement(chBean);
				childContainer.appendChild(chElement);
			}
			//Text t = doc.createCDATASection("Count: "+crBean.getChildRepository().size());
			//childContainer.appendChild(t);
			
			objElement.appendChild(childContainer);
		}
		return objElement;
	}
}
