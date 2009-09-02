package com.gentics.cr.lucene.search.highlight;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.TextFragment;
import org.apache.lucene.search.highlight.TokenGroup;

import com.gentics.cr.configuration.GenericConfiguration;
/**
 * 
 * Last changed: $Date: 2009-06-26 15:48:16 +0200 (Fr, 26 Jun 2009) $
 * @version $Revision: 105 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class PhraseBolder extends ContentHighlighter implements Formatter{

	private Analyzer analyzer=null;
	private int numMaxFragments=3;
	private int fragmentSize=100;
	
	private String highlightPrefix="";
	private String highlightPostfix="";
	private String fragmentSeperator="";
	
	private static final String NUM_MAX_FRAGMENTS_KEY = "fragments";
	private static final String NUM_FRAGMENT_SIZE_KEY = "fragmentsize";
	private static final String PHRASE_PREFIX_KEY = "highlightprefix";
	private static final String PHRASE_POSTFIX_KEY = "highlightpostfix";
	private static final String FRAGMENT_SEPERATOR_KEY = "fragmentseperator";
	/**
	 * Create new Instance of PhraseBolder
	 * @param config
	 */
	public PhraseBolder(GenericConfiguration config) {
		super(config);
		analyzer = new StandardAnalyzer();
		
		highlightPrefix = (String)config.get(PHRASE_PREFIX_KEY);
		if(highlightPrefix==null)highlightPrefix="<b>";
		highlightPostfix = (String)config.get(PHRASE_POSTFIX_KEY);
		if(highlightPostfix==null)highlightPostfix="</b>";
		fragmentSeperator = (String)config.get(FRAGMENT_SEPERATOR_KEY);
		if(fragmentSeperator==null)fragmentSeperator="...";
		
		String nmF = (String)config.get(NUM_MAX_FRAGMENTS_KEY);
		if(nmF!=null)
		{
			try
			{
				int i = Integer.parseInt(nmF);
				numMaxFragments = i;
			}catch(NumberFormatException e)
			{
				log.error("The configured count of fragments for this ContentHighlighter is not a number");
			}
		}
		String nFS = (String)config.get(NUM_FRAGMENT_SIZE_KEY);
		if(nFS!=null)
		{
			try
			{
				int i = Integer.parseInt(nFS);
				fragmentSize = i;
			}catch(NumberFormatException e)
			{
				log.error("The configured size for fragments for this ContentHighlighter is not a number");
			}
		}
	}

	/**
	 * Highlights Terms by enclosing them with &lt;b&gt;term&lt;/b&gt;
	 * @param originalTermText 
	 * @param tokenGroup 
	 * @return 
	 */
	public String highlightTerm(String originalTermText, TokenGroup tokenGroup) {
		
		if (tokenGroup.getTotalScore() <= 0) 
	      return originalTermText;
	  
	    return highlightPrefix + originalTermText + highlightPostfix;

	}

	
	/**
	 * @param attribute 
	 * @param parsedQuery 
	 * @return 
	 * 
	 */
	public String highlight(String attribute, Query parsedQuery) {
		String result="";
		if(attribute!=null && parsedQuery!=null)
		{
			Highlighter highlighter = new Highlighter(this,new QueryScorer(parsedQuery));
			highlighter.setTextFragmenter(new WordCountFragmenter(fragmentSize));
			
			TokenStream tokenStream = analyzer.tokenStream(this.getHighlightAttribute(), new StringReader(attribute));
	
			
			try {
				TextFragment[] frags = highlighter.getBestTextFragments(tokenStream, attribute, true, numMaxFragments);
				boolean first=true;
				for(TextFragment frag:frags)
				{
					if(!first)
					{
						result +=fragmentSeperator;
					}
					else
					{
						first=false;
					}
					result +=frag.toString();
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return(result);
	}

}
