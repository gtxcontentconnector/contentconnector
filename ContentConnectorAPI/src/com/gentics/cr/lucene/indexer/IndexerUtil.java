package com.gentics.cr.lucene.indexer;

import java.io.File;
import java.util.Arrays;
import java.util.List;
/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class IndexerUtil{

	/**
	 * Returns a File to a given path and does nothing if path is null
	 * @param path
	 * @return null if path is null or file does not exist
	 */
	public static File getFileFromPath(String path)
	{
		if(path!=null && !path.equals(""))
		{
			File f = new File(path);
			
			if(f.exists())
			{
				return(f);
			}
		}
		return(null);
	}
	
	/**
	 * Splits a string according to the given delimeter and returns a List with the elements of the string
	 * @param str
	 * @param delimeter
	 * @return
	 */
	public static List<String> getListFromString(String str,String delimeter)
	{
		if(str!=null && !str.equals(""))
		{
			String[] arr = str.split(delimeter);
			return(Arrays.asList(arr));
		}
		return(null);
	}
	
		
}
