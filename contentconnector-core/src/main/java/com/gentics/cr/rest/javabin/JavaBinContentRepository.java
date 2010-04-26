package com.gentics.cr.rest.javabin;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import com.gentics.cr.CRError;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.rest.ContentRepository;

/**
 *
 * Implementaion of Java serialized representation for a REST contentrepositroy.
 *
 * 
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 545 $
 * @author $Author: supnig@constantinopel.at $
 *  
 */
public class JavaBinContentRepository extends ContentRepository {

	

	/**
	 * 
	 */
	private static final long serialVersionUID = 713445758416480660L;

	/**
	 * Create instance
	 * @param attr
	 */
	public JavaBinContentRepository(String[] attr) {
		
		super(attr);

		this.setResponseEncoding("UTF-8");
		
	}
	/**
	 * Create instance
	 * @param attr
	 * @param encoding
	 */
	public JavaBinContentRepository(String[] attr, String encoding) {
		
		super(attr);

		this.setResponseEncoding(encoding);
		
	}
	/**
	 * Create instance
	 * @param attr
	 * @param encoding
	 * @param options
	 */
	public JavaBinContentRepository(String[] attr, String encoding, String[] options) {
		
		super(attr,encoding,options);

		//this.setResponseEncoding(encoding);
		
	}
	
	/**
	 * Returns contenttype "application/x-java-serialized-object"
	 * @return 
	 */
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
			JavaBinContentRepository.log.error("Unable to serialize object: "+o.getClass().getName());
			e.printStackTrace();
		}
	}
	
	/**
	 * Respond with Error
	 * 		Serialized CRError class
	 * @param stream 
	 * @param ex 
	 * @param isDebug 
	 * 
	 */
	public void respondWithError(OutputStream stream,CRException ex, boolean isDebug){

		CRError e = new CRError(ex);
		if(!isDebug)
		{
			e.setStringStackTrace(null);
		}
	
		serialize(e,stream);
		
	}
	/**
	 * Wrtites objects to stream
	 * @param stream 
	 * @throws CRException 
	 */
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
