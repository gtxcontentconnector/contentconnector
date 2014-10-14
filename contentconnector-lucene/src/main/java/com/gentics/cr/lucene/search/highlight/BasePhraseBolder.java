package com.gentics.cr.lucene.search.highlight;

import java.io.IOException;
import java.io.StringReader;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Fragmenter;
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
 * Base class for PhraseBolders.
 *
 */
public abstract class BasePhraseBolder extends ContentHighlighter implements Formatter {
	/**
	  * Logger.
	  */
	private static final Logger LOGGER = Logger.getLogger(BasePhraseBolder.class);

	/**
	 * Default max fragments.
	 */
	private static final int DEFAULT_MAX_FRAGMENTS = 3;
	
	private Analyzer analyzer = null;

	/**
	 * Create new Instance of PhraseBolder.
	 * @param config configuration
	 */
	public BasePhraseBolder(final GenericConfiguration config) {
		super(config);
		analyzer = LuceneAnalyzerFactory.createAnalyzer(config);
	}

	/**
	 * Highlights Terms by enclosing them with &lt;b&gt;term&lt;/b&gt;.
	 * @param originalTermText 
	 * @param tokenGroup 
	 * @return highlightedterm
	 */
	public final String highlightTerm(final String originalTermText, final TokenGroup tokenGroup) {
		UseCase uc = MonitorFactory.startUseCase("Highlight.PhraseBolder.highlightTerm()");
		if (tokenGroup.getTotalScore() <= 0) {
			uc.stop();
			return originalTermText;
		}
		uc.stop();
		return getHighlightPrefix() + originalTermText + getHighlightPostfix();

	}
	
	
	/**
	 * This function returns the Fragmenter implementation for this PhraseBolder.
	 * @return
	 */
	public abstract Fragmenter getFragmenter();
	
	/**
	 * returns true if adjacent fragments should be merged to one.
	 * @return
	 */
	public abstract boolean isMergeAdjacentFragments();
	
	
	/**
	 * @param attribute 
	 * @param parsedQuery 
	 * @return highlighted text
	 * 
	 */
	public final String highlight(final String attribute, final Query parsedQuery) {
		UseCase uc = MonitorFactory.startUseCase("Highlight.PhraseBolder.highlight()");
		StringBuilder result = new StringBuilder();
		if (attribute != null && parsedQuery != null) {
			Highlighter highlighter = new Highlighter(this, new QueryScorer(parsedQuery));
			highlighter.setTextFragmenter(getFragmenter());

			try {
				TokenStream tokenStream = analyzer.tokenStream(this.getHighlightAttribute(), new StringReader(attribute));
				UseCase ucFragments = MonitorFactory.startUseCase("Highlight.PhraseBolder.highlight()#getFragments");
				TextFragment[] frags = highlighter.getBestTextFragments(tokenStream, attribute, isMergeAdjacentFragments(), getMaxFragments());
				ucFragments.stop();
				boolean first = true;
				int startPosition = -1;
				int endPosition = -1;
				for (TextFragment frag : frags) {
					String fragment = frag.toString();
					fragment = fragment.replaceAll(REMOVE_TEXT_FROM_FRAGMENT_REGEX, "");
					startPosition = attribute.indexOf(fragment);
					endPosition = startPosition + fragment.length();
					if (!first || (addSeperatorArroundAllFragments() && startPosition != 0)) {
						result.append(getFragmentSeperator());
					}
					result.append(fragment);
				}
				if (addSeperatorArroundAllFragments() && endPosition != attribute.length() && result.length() != 0) {
					result.append(getFragmentSeperator());
				}
			} catch (IOException e) {
				LOGGER.error("Error getting fragments from highlighter.", e);
			} catch (InvalidTokenOffsetsException e) {
				LOGGER.error("Error getting fragments from highlighter.", e);
			}
		}
		uc.stop();
		return result.toString();
	}

	@Override
	protected final int getDefaultMaxFragments() {
		return DEFAULT_MAX_FRAGMENTS;
	}

}
