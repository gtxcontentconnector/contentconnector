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
