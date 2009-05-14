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
public class XmlContentRepository extends ContentRepository {

	private static final long serialVersionUID = 0004L;

	private Element rootElement;

	private Document doc;

	private DOMSource src;

	public XmlContentRepository(String[] attr) {
		
		super(attr);

		// Create XML Document
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		this.setResponseEncoding("UTF-8");
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
	public XmlContentRepository(String[] attr, String encoding) {
		
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
	
	public XmlContentRepository(String[] attr, String encoding, String[] options) {
		
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
		this.rootElement = doc.createElement("Contentrepository");
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
			//No Data Found
			/*this.rootElement.setAttribute("status","error");
			Element errElement = doc.createElement("Error");
			errElement.setAttribute("type","NoDataFound");
			errElement.setAttribute("message","Data could not be found.");
			this.rootElement.appendChild(errElement);*/
			throw new CRException("NoDataFound","Data could not be found.");
		}
		else
		{
			//Elements found/status ok
			this.rootElement.setAttribute("status","ok");
			
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

	@SuppressWarnings("unchecked")
	private Element processElement(CRResolvableBean crBean) {
		Element objElement = doc.createElement("Object");
		
		
		objElement.setAttribute("contentid", "" + crBean.getContentid());
		objElement.setAttribute("obj_id", "" + crBean.getObj_id());
		objElement.setAttribute("obj_type", "" + crBean.getObj_type());
		objElement.setAttribute("mother_id",
				((crBean.getMother_id() == null) ? "" : ""
						+ crBean.getMother_id()));
		objElement.setAttribute("mother_type",
				((crBean.getMother_type() == null) ? "" : ""
						+ crBean.getMother_type()));
		
		if (crBean.getAttrMap() != null && (!crBean.getAttrMap().isEmpty())) {
			Element attrContainer = doc.createElement("attributes");
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
									attrContainer.appendChild(attrElement);
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
							attrContainer.appendChild(attrElement);
							
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
						attrContainer.appendChild(attrElement);
						
						Text text = doc.createCDATASection(value);
						attrElement.appendChild(text);
					}
					/*if (value == null) {
						value = "";
					}*/
					
				}
			}
			objElement.appendChild(attrContainer);
		}
		
		if(crBean.getChildRepository()!=null && crBean.getChildRepository().size()>0)
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
