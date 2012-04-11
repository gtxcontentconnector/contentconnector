package com.gentics.cr.lucene.search.highlight;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.vectorhighlight.FastVectorHighlighter;
import org.apache.lucene.search.vectorhighlight.FieldQuery;
import org.apache.lucene.search.vectorhighlight.ScoreOrderFragmentsBuilder;
import org.apache.lucene.search.vectorhighlight.SimpleFragListBuilder;

import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.monitoring.MonitorFactory;
import com.gentics.cr.monitoring.UseCase;

/**
 * VectorBolder.
 * Last changed: $Date: 2009-06-26 15:48:16 +0200 (Fr, 26 Jun 2009) $
 * @version $Revision: 105 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class VectorBolder extends AdvancedContentHighlighter {
	/**
		 * Log4j logger for error and debug messages.
		 */
	private static final Logger LOGGER = Logger.getLogger(VectorBolder.class);
	/**
	 * Default max fragments.
	 */
	private static final int DEFAULT_MAX_FRAGMENTS = 3;
	/**
	 * Default fragment size.
	 */
	private static final int DEFAULT_FRAGMENT_SIZE = 100;

	/**
	 * max fragments.
	 */
	private int numMaxFragments = DEFAULT_MAX_FRAGMENTS;
	/**
	 * fragment size.
	 */
	private int fragmentSize = DEFAULT_FRAGMENT_SIZE;

	/**
	 * Create new Instance of PhraseBolder.
	 * @param config configuration.
	 * 
	 * @deprecated this class has been replaced with {@link WhitespaceVectorBolder}.
	 */
	@Deprecated
	public VectorBolder(final GenericConfiguration config) {
		super(config);
	}

	/**
	 * highlight method.
	 * @param parsedQuery 
	 * @param reader reader
	 * @param docId docid
	 * @param fieldName fieldname
	 * @return highlighted text.
	 * 
	 */
	public final String highlight(final Query parsedQuery, final IndexReader reader, final int docId,
			final String fieldName) {
		UseCase uc = MonitorFactory.startUseCase("Highlight.VectorBolder.highlight()");
		String result = "";
		if (fieldName != null && parsedQuery != null) {
			FastVectorHighlighter highlighter = new FastVectorHighlighter(true, true, new SimpleFragListBuilder(),
					new ScoreOrderFragmentsBuilder(new String[] { getHighlightPrefix() },
							new String[] { getHighlightPostfix() }));
			FieldQuery fieldQuery = highlighter.getFieldQuery(parsedQuery);
			//highlighter.setTextFragmenter(new WordCountFragmenter(fragmentSize));

			//TokenStream tokenStream = analyzer.tokenStream(
			//		this.getHighlightAttribute(), new StringReader(attribute));
			try {
				UseCase ucFragments = MonitorFactory.startUseCase("Highlight.VectorBolder.highlight()#getFragments");
				//TextFragment[] frags = highlighter.getBestTextFragments(tokenStream,
				//		attribute, true, numMaxFragments);
				String[] frags = highlighter.getBestFragments(
					fieldQuery,
					reader,
					docId,
					fieldName,
					fragmentSize,
					numMaxFragments);
				ucFragments.stop();
				boolean first = true;
				if (frags != null) {
					for (String frag : frags) {
						frag = frag.replaceAll(REMOVE_TEXT_FROM_FRAGMENT_REGEX, "");
						if (!first) {
							result += getFragmentSeperator();
						} else {
							first = false;
						}
						result += frag;
					}
				}
			} catch (IOException e) {
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
