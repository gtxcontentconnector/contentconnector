package com.gentics.cr.lucene.search.highlight;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.vectorhighlight.FastVectorHighlighter;
import org.apache.lucene.search.vectorhighlight.FieldQuery;
import org.apache.lucene.search.vectorhighlight.SimpleFragListBuilder;
import org.apache.lucene.search.vectorhighlight.WhitespaceFragmentsBuilder;

import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.monitoring.MonitorFactory;
import com.gentics.cr.monitoring.UseCase;
/**
 * WhitespaceVectorBolder.
 * Last changed: $Date: 2009-06-26 15:48:16 +0200 (Fr, 26 Jun 2009) $
 * @version $Revision: 105 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class WhitespaceVectorBolder extends AdvancedContentHighlighter {

	/**
	   * Log4j logger for error and debug messages.
	   */
	  private static Logger log =
	    Logger.getLogger(VectorBolder.class);
	/**
	 * default value for max frequents.
	 */
	private static final int DEFAULT_MAX_FRAGMENTS = 3;
	/**
	 * max number of fragments.
	 */
  private int numMaxFragments = DEFAULT_MAX_FRAGMENTS;
  
  /**
   * default fragment size.
   */
  private static final int DEFAULT_FRAGMENT_SIZE = 100;
  
  /**
   * size of a fragment.
   */
  private int fragmentSize = DEFAULT_FRAGMENT_SIZE;

  /**
   * highlight prefix.
   */
  private String highlightPrefix = "";
  /**
   * highlight suffix.
   */
  private String highlightPostfix = "";
  /**
   * Fragment seperator.
   */
  private String fragmentSeperator = "";

  /**
   * max frequents key.
   */
  private static final String NUM_MAX_FRAGMENTS_KEY = "fragments";
  /**
   * fragment size key.
   */
  private static final String NUM_FRAGMENT_SIZE_KEY = "fragmentsize";
  /**
   * highlight prefix key.
   */
  private static final String PHRASE_PREFIX_KEY = "highlightprefix";
  /**
   * highlight postfix key.
   */
  private static final String PHRASE_POSTFIX_KEY = "highlightpostfix";
  /**
   * fragment seperator key.
   */
  private static final String FRAGMENT_SEPERATOR_KEY = "fragmentseperator";
  
 
  /**
   * Create new Instance of PhraseBolder.
   * @param config config
   */
  public WhitespaceVectorBolder(final GenericConfiguration config) {
    super(config);
        
    highlightPrefix = (String) config.get(PHRASE_PREFIX_KEY);
    if (highlightPrefix == null) {
    	highlightPrefix = "<b>";
    }
    highlightPostfix = (String) config.get(PHRASE_POSTFIX_KEY);
    if (highlightPostfix == null) {
    	highlightPostfix = "</b>";
    }
    fragmentSeperator = (String) config.get(FRAGMENT_SEPERATOR_KEY);
    if (fragmentSeperator == null) {
    	fragmentSeperator = "...";
    }
    
    String nmF = (String) config.get(NUM_MAX_FRAGMENTS_KEY);
    if (nmF != null) {
      try {
        int i = Integer.parseInt(nmF);
        numMaxFragments = i;
      } catch (NumberFormatException e) {
        log.error("The configured count of fragments for this"
        		+ "ContentHighlighter is not a number");
      }
    }
    String nFS = (String) config.get(NUM_FRAGMENT_SIZE_KEY);
    if (nFS != null) {
      try {
        int i = Integer.parseInt(nFS);
        fragmentSize = i;
      } catch (NumberFormatException e) {
        log.error("The configured size for fragments for this "
        		+ "ContentHighlighter is not a number");
      }
    }
  }

  
  /**
   * @param parsedQuery query.
   * @param reader reader.
   * @param docId docid.
   * @param fieldName fieldname.
   * @return string.
   */
  public final String highlight(final Query parsedQuery,
		  final IndexReader reader, final int docId, final String fieldName) {
    UseCase uc = MonitorFactory.startUseCase(
        "Highlight.VectorBolder.highlight()");
    String result = "";
    if (fieldName != null && parsedQuery != null) {
      FastVectorHighlighter highlighter =
        new FastVectorHighlighter(true, true, new SimpleFragListBuilder(),
        		new WhitespaceFragmentsBuilder(
        		new String[]{this.highlightPrefix},
        		new String[]{this.highlightPostfix}));
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
        	  frag = frag.replaceAll(REMOVE_TEXT_FROM_FRAGMENT_REGEX, "");
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
