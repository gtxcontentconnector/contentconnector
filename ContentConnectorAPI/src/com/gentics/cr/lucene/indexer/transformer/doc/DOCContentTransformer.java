package com.gentics.cr.lucene.indexer.transformer.doc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;

import org.apache.poi.hwpf.extractor.WordExtractor;

import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;
/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class DOCContentTransformer extends ContentTransformer{

	/**
	 * Get new instance of DOCContentTransformer
	 * @param config
	 */
	public DOCContentTransformer(GenericConfiguration config)
	{
		super(config);
	}
	
	/**
	 * Converts a byte array that contains a word file into a string with its contents
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
		String ret=null;
		WordExtractor docextractor;
		try {
			docextractor = new WordExtractor(is);
			
			ret = docextractor.getText();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(Exception e)
		{
			//Catch all exceptions here to not disturb the indexer
			e.printStackTrace();
		}
		return(ret);
	}
	/**
	 * Converts a byte array that contains a word file into a string reader with its contents
	 * @param obj
	 * @return StringReader or null if something bad happens
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
