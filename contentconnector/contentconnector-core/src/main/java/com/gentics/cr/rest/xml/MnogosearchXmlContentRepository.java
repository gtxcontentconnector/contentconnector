package com.gentics.cr.rest.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;

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
import org.w3c.dom.Text;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.exceptions.CRException;

/**
 * 
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 545 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class MnogosearchXmlContentRepository extends XmlContentRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4197488418716139110L;

	/**
	 * Create Instance.
	 * 	Set response encoding to utf-8
	 * @param attr
	 */
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

	/**
	 * Create instance.
	 * @param attr
	 * @param encoding
	 */
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

	/**
	 * Create Instance.
	 * @param attr
	 * @param encoding
	 * @param options
	 */
	public MnogosearchXmlContentRepository(String[] attr, String encoding, String[] options) {
		super(attr, encoding, options, "search");
	}

	/**
	 * 
	 * returns test/xml
	 * @return 
	 * 
	 */
	public String getContentType() {
		return "text/xml";
	}

	/**
	 * @param stream 
	 * @throws CRException 
	 * 
	 */
	public void toStream(OutputStream stream) throws CRException {

		if (this.resolvableColl.isEmpty()) {
			Element statusElement = doc.createElement("status");
			statusElement.appendChild(doc.createTextNode("notfound"));
			this.rootElement.appendChild(statusElement);
		} else {
			//Elements found/status ok
			//this.rootElement.setAttribute("status","ok");
			Element statusElement = doc.createElement("status");
			statusElement.appendChild(doc.createTextNode("found"));
			this.rootElement.appendChild(statusElement);

			rootElement.appendChild(createMetaDataNode(doc));

			for (Iterator<CRResolvableBean> it = this.resolvableColl.iterator(); it.hasNext();) {

				CRResolvableBean crBean = it.next();

				Element objElement = processElement(crBean);
				this.rootElement.appendChild(objElement);
			}
		}

		// output xml

		try {
			OutputStreamWriter wr = new OutputStreamWriter(stream, this.getResponseEncoding());

			StreamResult strRes = new StreamResult(wr);
			TransformerFactory.newInstance().newTransformer().transform(this.src, strRes);
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

	private Element createMetaDataNode(Document doc) {
		Element metaElement = doc.createElement("meta");

		Element first = doc.createElement("first");
		first.appendChild(doc.createCDATASection("1"));
		metaElement.appendChild(first);

		Element last = doc.createElement("last");
		last.appendChild(doc.createCDATASection(this.resolvableColl.size() + ""));
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
		total.appendChild(doc.createCDATASection(this.resolvableColl.size() + ""));
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
		if (crBean.getAttrMap() != null && !crBean.getAttrMap().isEmpty()) {
			//Element attrContainer = doc.createElement("attributes");
			Iterator<String> bit = crBean.getAttrMap().keySet().iterator();
			while (bit.hasNext()) {

				String entry = bit.next();
				if (!"".equals(entry)) {

					Object bValue = crBean.getAttrMap().get(entry);
					String value = "";
					if (bValue != null) {

						if (!entry.equals("binarycontent") && (bValue.getClass().isArray() || bValue.getClass() == ArrayList.class)) {

							Object[] arr;
							if (bValue.getClass() == ArrayList.class) {
								arr = ((ArrayList<Object>) bValue).toArray();
							} else {
								arr = (Object[]) bValue;
							}
							for (int i = 0; i < arr.length; i++) {
								Element attrElement = doc.createElement(entry);
								objElement.appendChild(attrElement);
								//								
								if (arr[i].getClass() == String.class) {
									value = (String) arr[i];
								} else {
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

						} else {
							Element attrElement = doc.createElement(entry);
							objElement.appendChild(attrElement);

							if (entry.equals("binarycontent")) {

								try {
									value = new String((byte[]) bValue);
								} catch (ClassCastException x) {
									try {
										value = new String(getBytes(bValue));
									} catch (IOException e) {
										value = bValue.toString();
										e.printStackTrace();
									}
								}
								//TODO return proper binary content
								//value=(String) bValue.toString();
							} else {
								if (bValue.getClass() == String.class) {
									value = (String) bValue;
								} else {
									value = bValue.toString();
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

					} else {
						Element attrElement = doc.createElement(entry);
						objElement.appendChild(attrElement);

						Text text = doc.createCDATASection(value);
						attrElement.appendChild(text);
					}
					/*
					 * if (value == null) { value = ""; }
					 */

				}
			}
		}

		if (crBean.getChildRepository() != null && crBean.getChildRepository().size() > 0) {
			Element childContainer = doc.createElement("children");

			for (Iterator<CRResolvableBean> it = crBean.getChildRepository().iterator(); it.hasNext();) {

				CRResolvableBean chBean = it.next();

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
