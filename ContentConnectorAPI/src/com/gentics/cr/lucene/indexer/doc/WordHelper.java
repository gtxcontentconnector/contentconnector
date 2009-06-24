package com.gentics.cr.lucene.indexer.doc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;

import org.apache.poi.hwpf.extractor.WordExtractor;
/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class WordHelper {

	/**
	 * Converts a byte array that contains a word file into a string reader with its contents
	 * @param binarycontent
	 * @return StringReader or null if something bad happens
	 */
	public static StringReader getContents(byte[] binarycontent)
	{
		StringReader ret = null;
		ByteArrayInputStream is = new ByteArrayInputStream(binarycontent);
		WordExtractor docextractor;
		try {
			docextractor = new WordExtractor(is);
			
			ret = new StringReader(docextractor.getText());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(Exception e)
		{
			//Catch all exceptions here to not disturb the indexer
			e.printStackTrace();
		}
		
		
		
		return ret;
	}
}
