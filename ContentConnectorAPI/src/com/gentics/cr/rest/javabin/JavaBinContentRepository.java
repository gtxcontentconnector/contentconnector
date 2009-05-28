package com.gentics.cr.rest.javabin;

import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.gentics.cr.CRError;
import com.gentics.cr.CRException;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.rest.ContentRepository;

/**
 *
 * Implementaion of Java serialized representation for a REST contentrepositroy.
 *
 * 
 * Last changed: $Date: 2009-05-18 17:31:58 +0200 (Mo, 18 Mai 2009) $
 * @version $Revision: 27 $
 * @author $Author: supnig@constantinopel.at $
 *  
 */
public class JavaBinContentRepository extends ContentRepository {

	

	/**
	 * 
	 */
	private static final long serialVersionUID = 713445758416480660L;

	public JavaBinContentRepository(String[] attr) {
		
		super(attr);

		this.setResponseEncoding("UTF-8");
		
	}
	public JavaBinContentRepository(String[] attr, String encoding) {
		
		super(attr);

		this.setResponseEncoding(encoding);
		
	}
	
	public JavaBinContentRepository(String[] attr, String encoding, String[] options) {
		
		super(attr,encoding,options);

		//this.setResponseEncoding(encoding);
		
	}
	

	public String getContentType() {
		return "application/x-java-serialized-object";
	}
	
	private void serialize(Object o, OutputStream stream)
	{
		try {
			ObjectOutputStream oos = new ObjectOutputStream(stream);
			oos.writeObject(o);
			oos.flush();
			oos.close();
		} catch (IOException e) {
			this.log.error("Unable to serialize object: "+o.getClass().getName());
			e.printStackTrace();
		}
	}
	
	
	public void respondWithError(OutputStream stream,CRException ex, boolean isDebug){

		CRError e = new CRError(ex);
		if(!isDebug)
		{
			e.setStringStackTrace(null);
		}
	
		serialize(e,stream);
		
	}
	
	public void toStream(OutputStream stream) throws CRException {
		
		if(this.resolvableColl.isEmpty())
		{
			//No Data Found
			throw new CRException("NoDataFound","Data could not be found.",CRException.ERRORTYPE.NO_DATA_FOUND);
		}
		else
		{
			//Elements found/status ok
			serialize(this.resolvableColl,stream);
		}
		
		
	}

}
