package com.gentics.cr.lucene.indexer.transformer.multivaluestring;

import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;

import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;

/**
 * 
 * Last changed: $Date: 2009-06-26 15:48:16 +0200 (Fr, 26 Jun 2009) $
 * @version $Revision: 105 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class SimpleMVString extends ContentTransformer {

	private String NULLValue="NULL";
	private static final String NULL_VALUE_KEY = "nullvalue";
	private static final String DEFAULT_NULL_VALUE="NULL";
	
	/**
	 * Create new Instance of SimpleMVString
	 * @param config
	 */
	public SimpleMVString(GenericConfiguration config) {
		super(config);
		//Define Value for NULLS
		String NULLValue = (String)config.get(NULL_VALUE_KEY);
		if(NULLValue==null)NULLValue=DEFAULT_NULL_VALUE;
	}

	/**
	 * 
	 */
	@Override
	public Reader getContents(Object obj) {
		StringReader r = null;
		String str = getStringContents(obj);
		if(str!=null)r=new StringReader(str);
		return r;
	}

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public String getStringContents(Object obj) {
		String ret=NULLValue;
		if(obj!=null && obj instanceof Collection)
		{
			ret="";
			Collection<Object> coll = (Collection<Object>)obj;
			if(coll==null || coll.size()<=0)return NULLValue;
			for(Object object:coll)
			{
				if(object!=null)
				{
					ret+=object.toString()+" ";
				}
			}
		}
		return ret;
	}

}
