package com.gentics.cr.rest.json;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.gentics.cr.CRException;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.rest.ContentRepository;

/**
 * Implementaion of Json rappresentation for a REST contentrepositroy.
 *
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class JSONContentRepository extends ContentRepository {

	private static final long serialVersionUID = 0003L;

	private JSONObject rootObject;

	public JSONContentRepository(String[] attr) {

		super(attr);
		this.setResponseEncoding("UTF-8");
		rootObject = new JSONObject();
	}
	
	public JSONContentRepository(String[] attr, String encoding) {

		super(attr);
		this.setResponseEncoding(encoding);
		rootObject = new JSONObject();
	}
	
	public JSONContentRepository(String[] attr, String encoding, String[] options) {

		super(attr,encoding,options);
		//this.setResponseEncoding(encoding);
		rootObject = new JSONObject();
	}

	public String getContentType() {
		//return "text/javascript";
		return("application/jsonrequest");
	}
	
	
	public void respondWithError(OutputStream stream,CRException ex, boolean isDebug)
	{
		//Clear the root element and set the status
		this.rootObject.clear();
		this.rootObject.element("status","error");
		JSONObject errElement = new JSONObject();
		
		errElement.element("type",ex.getType());
		errElement.element("message",ex.getMessage());
		if(isDebug)
		{
			//if in debug mode, add stack trace
			errElement.element("stacktrace",ex.getStringStackTrace());
		}
		this.rootObject.element("Error",errElement);
		
		//Write stream 
		try {
			stream.write(this.rootObject.toString().getBytes());
		} catch (IOException e) {
			;
		}
		
	}
	

	public void toStream(OutputStream stream) throws CRException{
		if(this.resolvableColl.isEmpty())
		{
			//No Data Found
			throw new CRException("NoDataFound","Data could not be found.");
		}
		else
		{
			this.rootObject.element("status","ok");
			JSONObject objListElement = new JSONObject();
			
			for (Iterator<CRResolvableBean> it = this.resolvableColl.iterator(); it.hasNext();) {
	
				CRResolvableBean crBean =  it.next();
	
				JSONObject objElement = processElement(crBean);
	
				objListElement.element(crBean.getContentid(), objElement);
			}
			this.rootObject.element("Objects",objListElement);
		}

		try
		{
			// use JSONObject.write instead toString is not a good solution
			OutputStreamWriter wr = new OutputStreamWriter(stream, this.getResponseEncoding());
			//TODO auch bei den anderen repositories den output charset übergeben
			//TODO output charset über web.xml konfigurierbar machen
			this.rootObject.write(wr);
			wr.flush();
			wr.close();
			//stream.write(this.rootObject.toString().getBytes());
		}catch(IOException ioex)
		{
			;
		}

	}

	@SuppressWarnings("unchecked")
	private JSONObject processElement(CRResolvableBean crBean) {
		JSONObject objElement = new JSONObject();

		objElement.element("contentid", "" + crBean.getContentid());
		objElement.element("obj_id", "" + crBean.getObj_id());
		objElement.element("obj_type", "" + crBean.getObj_type());
		objElement.element("mother_id",
				((crBean.getMother_id() == null) ? "" : ""
						+ crBean.getMother_id()));
		objElement.element("mother_type",
				((crBean.getMother_type() == null) ? "" : ""
						+ crBean.getMother_type()));

		if (crBean.getAttrMap() != null && (!crBean.getAttrMap().isEmpty())) {
			JSONObject attrContainer = new JSONObject();
			
			Iterator<String> bit = crBean.getAttrMap().keySet().iterator();
			while (bit.hasNext()) {

				String entry = bit.next();
				// Element attrElement = doc.createElement(entry);
				Object bValue=crBean.getAttrMap().get(entry);
				
				if(bValue!=null)
				{
					if((!entry.equals("binarycontent")) &&(bValue.getClass().isArray()||bValue.getClass()==ArrayList.class))
					{
						JSONArray value = new JSONArray();
						ArrayList<Object> arr;
						if(bValue.getClass()==ArrayList.class)
							arr=(ArrayList<Object>)bValue;
						else
						{
							arr=new ArrayList<Object>();
							Object[] ob_arr = (Object[])bValue;
							for(int i=0;i<ob_arr.length;i++ )
								arr.add(ob_arr[i]);
						}
						value.addAll(arr);
						attrContainer.element(entry, value);
					}
					else
					{
						String value="";
						if(entry.equals("binarycontent"))
						{
							byte[] bs = (byte[])bValue;
							value=new String(bs);
						}
						else
						{
							//value=(String) bValue;
							if(bValue.getClass()==String.class)
							{
								
								value=(String)bValue;
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
	//						if(bValue.getClass()==String.class)
	//						{
	//							value=(String) bValue;
	//						}
	//						else
	//						{
	//							value=(String) bValue.toString();
	//						}
						}
						attrContainer.element(entry, value);
					}
					
				}
				/* 
				if (value == null) {
					value = "";
				}*/
				
				
			}
			objElement.element("attributes",attrContainer);
		}
		if(crBean.getChildRepository().size()>0)
		{
			//JSONObject childContainer = new JSONObject();
			JSONArray childContainer = new JSONArray();
			for (Iterator it = crBean.getChildRepository().iterator(); it.hasNext();) {
				
				CRResolvableBean chBean = (CRResolvableBean) it.next();
	
				JSONObject chElement = processElement(chBean);
				childContainer.add(chElement);
				//childContainer.element(chBean.getContentid(),chElement);
			}
			objElement.element("children",childContainer);
		}
		return objElement;
	}
	
	
}
