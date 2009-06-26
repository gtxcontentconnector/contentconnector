package com.gentics.cr.lucene.indexer.transformer.ppt;
import java.io.ByteArrayInputStream;
import java.io.StringReader;

import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;



/**
 * 
 * Last changed: $Date: 2009-06-24 17:10:19 +0200 (Mi, 24 Jun 2009) $
 * @version $Revision: 99 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class PPTContentTransformer extends ContentTransformer{

	/**
	 * Get new instance of PPTContentTransformer
	 * @param config
	 */
	public PPTContentTransformer(GenericConfiguration config)
	{
		super(config);
	}
	
	/**
	 * Converts a byte array containing a ppt file to a String that can be indexed by lucene
	 * @param obj
	 * @return
	 */
	public String getStringContents(Object obj)
	{
		ByteArrayInputStream is;
		if(obj instanceof byte[])
		{
			is= new ByteArrayInputStream((byte[])obj);
		}
		else
		{
			throw new IllegalArgumentException("Parameter must be instance of byte[]");
		}
		MSPowerPointParser parser = new MSPowerPointParser(is);
		String contents = parser.getContents();
		return(contents);
	}
	/**
	 * Converts a byte array containing a ppt file to a StringReader that can be indexed by lucene
	 * @param obj
	 * @return StringReader of contents
	 */
	public StringReader getContents(Object obj)
	{
		String s = getStringContents(obj);
		if(s!=null)
		{
			return new StringReader(s);
		}
		return null;
    }
}
