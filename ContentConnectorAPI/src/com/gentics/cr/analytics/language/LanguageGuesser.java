package com.gentics.cr.analytics.language;

import java.io.IOException;
import java.io.InputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.analysis.lang.LanguageIdentifier;

public class LanguageGuesser {
private static LanguageIdentifier langID = null;
	
	public static String detectLanguage(String text)
	{
		LanguageIdentifier li = getLIInstance();
		
		return li.identify(text);
	}
	
	public static String detectLanguage(InputStream is) throws IOException
	{
		LanguageIdentifier li = getLIInstance();
		return li.identify(is);
	}
	
	public static String detectLanguage(InputStream is, String charset) throws IOException
	{
		LanguageIdentifier li = getLIInstance();
		return li.identify(is,charset);
	}
	
	private static LanguageIdentifier getLIInstance()
	{
		if(langID==null)
		{
			langID = new LanguageIdentifier(new Configuration());
		}
		return(langID);
	}
}
