package com.gentics.cr.template;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.gentics.cr.CRException;
/**
 * loads a template from a file usint an input stream
 * Last changed: $Date: 2009-05-18 17:31:58 +0200 (Mo, 18 Mai 2009) $
 * @version $Revision: 27 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class FileTemplate implements ITemplate {
	
	private String source;
	
	private String key;
	
	/**
	 * gets the key of the template. usually a md5 hash
	 * @return key
	 */
	public String getKey()
	{
		return(this.key);
	}
	/**
	 * gets the source of the template
	 * @return source
	 */
	public String getSource()
	{
		return(this.source);
	}
	
	/**
	 * Creates a new instance of FileTemplate
	 * @param stream
	 * @throws CRException
	 */
	public FileTemplate(InputStream stream) throws CRException
	{
		try {
			this.source = slurp(stream);
		
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(this.source.getBytes());
			this.key = new String(digest.digest());
		} catch (IOException e) {
			
			e.printStackTrace();
			throw new CRException(e);
		} catch (NoSuchAlgorithmException e) {
			
			e.printStackTrace();
			throw new CRException(e);
		}
	}
	
	private static String slurp (InputStream in) throws IOException {
		StringBuffer out = new StringBuffer();
	    byte[] b = new byte[4096];
	    for (int n; (n = in.read(b)) != -1;) {
	        out.append(new String(b, 0, n));
	    }
	    return out.toString();
	}

}
