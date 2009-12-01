package com.gentics.cr.lucene.indexer.transformer;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.poi.POITextExtractor;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.xmlbeans.XmlException;

import com.gentics.cr.CRException;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
/**
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class POIContentTransformer extends ContentTransformer{
	private static final String TRANSFORMER_ATTRIBUTE_KEY="attribute";
	private String attribute="";
	/**
	 * Get new instance of DOCContentTransformer
	 * @param config
	 */
	public POIContentTransformer(GenericConfiguration config)
	{
		super(config);
		attribute = (String)config.get(TRANSFORMER_ATTRIBUTE_KEY);
	}
	
	/**
	 * Converts a byte array that contains a word file into a string with its contents
	 * @param obj
	 * @return
	 */
	private String getStringContents(Object obj)throws CRException
	{
		ByteArrayInputStream is;
		if(obj instanceof byte[])
		{
			is= new ByteArrayInputStream((byte[])obj);
		}
		else if(obj instanceof String)
		{
			String str = (String)obj;
			is = new ByteArrayInputStream(str.getBytes());
		}
		else
		{
			throw new IllegalArgumentException("Parameter must be instance of byte[]");
		}
		String ret=null;
		POITextExtractor extractor;
        
		try {
			extractor = ExtractorFactory.createExtractor(is); 
			
			ret = extractor.getText();
			
		} catch (IOException e) {
			throw new CRException(e);
		} catch (InvalidFormatException e) {
			throw new CRException(e);
		} catch (OpenXML4JException e) {
			throw new CRException(e);
		} catch (XmlException e) {
			throw new CRException(e);
		} 
		return(ret);
	}
	

	@Override
	public void processBean(CRResolvableBean bean)throws CRException{
		if(this.attribute!=null)
		{
			Object obj = bean.get(this.attribute);
			if(obj!=null)
			{
				String newString = getStringContents(obj);
				if(newString!=null)
				{
					bean.set(this.attribute, newString);
				}
			}
		}
		else
		{
			log.error("Configured attribute is null. Bean will not be processed");
		}
	}
}
