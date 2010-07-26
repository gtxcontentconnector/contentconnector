package com.gentics.cr.lucene.search.highlight;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.TokenGroup;
import org.apache.lucene.search.vectorhighlight.FastVectorHighlighter;
import org.apache.lucene.search.vectorhighlight.FieldQuery;

import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.indexer.index.LuceneAnalyzerFactory;
import com.gentics.cr.monitoring.MonitorFactory;
import com.gentics.cr.monitoring.UseCase;
/**
 * 
 * Last changed: $Date: 2009-06-26 15:48:16 +0200 (Fr, 26 Jun 2009) $
 * @version $Revision: 105 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class VectorBolder extends AdvancedContentHighlighter
    implements Formatter {

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
  public VectorBolder(GenericConfiguration config) {
    super(config);
    analyzer = LuceneAnalyzerFactory.createAnalyzer(config);
    
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
    UseCase uc = MonitorFactory.startUseCase(
    "Highlight.VectorBolder.highlightTerm()");
    if (tokenGroup.getTotalScore() <= 0) {
      uc.stop();
      return originalTermText;
    }
    uc.stop();
    return highlightPrefix + originalTermText + highlightPostfix;

  }


  /**
   * @param attribute 
   * @param parsedQuery 
   * @return 
   * 
   */
  public String highlight(Query parsedQuery, IndexReader reader, int docId,
      String fieldName) {
    UseCase uc = MonitorFactory.startUseCase(
        "Highlight.VectorBolder.highlight()");
    String result = "";
    if (fieldName != null && parsedQuery != null) {
      FastVectorHighlighter highlighter =
        new FastVectorHighlighter(true, true);
      FieldQuery fieldQuery = highlighter.getFieldQuery(parsedQuery);
      //highlighter.setTextFragmenter(new WordCountFragmenter(fragmentSize));

      //TokenStream tokenStream = analyzer.tokenStream(
      //    this.getHighlightAttribute(), new StringReader(attribute));
      try {
        UseCase ucFragments = MonitorFactory.startUseCase(
        "Highlight.VectorBolder.highlight()#getFragments");
        //TextFragment[] frags = highlighter.getBestTextFragments(tokenStream,
        //    attribute, true, numMaxFragments);
        String[] frags = highlighter.getBestFragments(fieldQuery, reader, docId,
            fieldName, fragmentSize, numMaxFragments);
        ucFragments.stop();
        boolean first = true;
        if (frags != null) {
          for (String frag : frags) {
            if (!first) {
              result += fragmentSeperator;
            } else {
              first = false;
            }
            result += frag;
          }
        }
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    uc.stop();
    return result;
  }

}
