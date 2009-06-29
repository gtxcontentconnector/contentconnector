package com.gentics.cr.lucene.indexer.transformer.txt;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;
import com.gentics.cr.util.CRUtil;


/**
 * 
 * Last changed: $Date: 2009-06-24 17:10:19 +0200 (Mi, 24 Jun 2009) $
 * @version $Revision: 99 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class TXTContentTransformer extends ContentTransformer {

	/**
	 * Get new instance of TXTContentTransformer
	 * @param config
	 */
	public TXTContentTransformer(GenericConfiguration config)
	{
		super(config);
	}
	
	
	/**
	 *
	 */
	@Override
	public Reader getContents(Object obj) {
		ByteArrayInputStream is;
		if(obj instanceof byte[])
		{
			is= new ByteArrayInputStream((byte[])obj);
		}
		else
		{
			throw new IllegalArgumentException("Parameter must be instance of byte[]");
		}
		return new InputStreamReader(is);
	}

	/**
	 * 
	 */
	@Override
	public String getStringContents(Object obj) {
		String ret = null;
		Reader r = getContents(obj);
		try
		{
			if(r!=null)
			{
				ret = CRUtil.readerToString(r);
			}
			r.close();
		}catch(Exception ex)
		{
			//Catch all exceptions here to not disturb the indexer
			ex.printStackTrace();
		}
		return(ret);
	}

}
