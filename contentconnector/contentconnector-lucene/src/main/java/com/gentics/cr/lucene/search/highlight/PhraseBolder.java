package com.gentics.cr.lucene.search.highlight;

import java.io.IOException;
import java.io.StringReader;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.TextFragment;
import org.apache.lucene.search.highlight.TokenGroup;

import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.indexer.index.LuceneAnalyzerFactory;
import com.gentics.cr.monitoring.MonitorFactory;
import com.gentics.cr.monitoring.UseCase;
/**
 * PhraseBolder.
 * Last changed: $Date: 2009-06-26 15:48:16 +0200 (Fr, 26 Jun 2009) $
 * @version $Revision: 105 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class PhraseBolder extends ContentHighlighter implements Formatter {
	/**
	  * Log4j logger for error and debug messages.
	  */
	private static final Logger LOGGER = Logger.getLogger(PhraseBolder.class);
	
	/**
	 * Default max fragments.
	 */
	private static final int DEFAULT_MAX_FRAGMENTS = 3;
	/**
	 * Default fragment size.
	 */
	private static final int DEFAULT_FRAGMENT_SIZE = 100;
	/**
	 * used analyzer.
	 */
	private Analyzer analyzer = null;

	
	/**
	 * Create new Instance of PhraseBolder.
	 * @param config configuration
	 */
	public PhraseBolder(final GenericConfiguration config) {
		super(config);
		analyzer = LuceneAnalyzerFactory.createAnalyzer(config);
	}

	/**
	 * Highlights Terms by enclosing them with &lt;b&gt;term&lt;/b&gt;.
	 * @param originalTermText 
	 * @param tokenGroup 
	 * @return highlightedterm
	 */
	public final String highlightTerm(final String originalTermText,
			final TokenGroup tokenGroup) {
		UseCase uc = MonitorFactory.startUseCase(
		"Highlight.PhraseBolder.highlightTerm()");
		if (tokenGroup.getTotalScore() <= 0) {
			uc.stop();
			return originalTermText;
		}
		uc.stop();
		return getHighlightPrefix() + originalTermText + getHighlightPostfix();

	}


	/**
	 * @param attribute 
	 * @param parsedQuery 
	 * @return highlighted text
	 * 
	 */
	public final String highlight(final String attribute,
			final Query parsedQuery) {
		UseCase uc = MonitorFactory.startUseCase(
				"Highlight.PhraseBolder.highlight()");
		String result = "";
		if (attribute != null && parsedQuery != null) {
			Highlighter highlighter =
				new Highlighter(this, new QueryScorer(parsedQuery));
			highlighter.setTextFragmenter(new WordCountFragmenter(getFragmentSize()));

			TokenStream tokenStream = analyzer.tokenStream(
					this.getHighlightAttribute(), new StringReader(attribute));
			try {
				UseCase ucFragments = MonitorFactory.startUseCase(
				"Highlight.PhraseBolder.highlight()#getFragments");
				TextFragment[] frags = highlighter.getBestTextFragments(tokenStream,
						attribute, true, getMaxFragments());
				ucFragments.stop();
				boolean first = true;
				int startPosition = -1;
				int endPosition = -1;
				for (TextFragment frag : frags) {
					String fragment = frag.toString();
					fragment = fragment.replaceAll(REMOVE_TEXT_FROM_FRAGMENT_REGEX, "");
					startPosition = attribute.indexOf(fragment);
					endPosition = startPosition + fragment.length();
					if (!first || (addSeperatorArroundAllFragments() 
							&& startPosition != 0)) {
						result += getFragmentSeperator();
					}
					result += fragment;
				}
				if (addSeperatorArroundAllFragments() && endPosition != attribute.length()
						&& result.length() != 0) {
					result += getFragmentSeperator();
				}
			} catch (IOException e) {
				LOGGER.error("Error getting fragments from highlighter.", e);
			} catch (InvalidTokenOffsetsException e) {
				LOGGER.error("Error getting fragments from highlighter.", e);
			}
		}
		uc.stop();
		return result;
	}

	@Override
	protected final int getDefaultFragmentSize() {
		return DEFAULT_FRAGMENT_SIZE;
	}

	@Override
	protected final int getDefaultMaxFragments() {
		return DEFAULT_MAX_FRAGMENTS;
	}

}
