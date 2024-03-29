package com.gentics.cr.lucene.autocomplete;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.events.Event;
import com.gentics.cr.events.IEventReceiver;
import com.gentics.cr.lucene.indexaccessor.IndexAccessor;
import com.gentics.cr.lucene.indexer.index.LuceneAnalyzerFactory;
import com.gentics.cr.lucene.indexer.index.LuceneIndexLocation;
import com.gentics.cr.util.indexing.IReIndexStrategy;
import com.gentics.cr.util.indexing.ReIndexNoSkipStrategy;
import com.gentics.lib.log.NodeLogger;

/**
 * This class can be used to build an autocomplete index over an existing lucene
 * index.
 * 
 * from version 2.0.0 the {@link AutocompleteIndexExtension} is used for all
 * Index related tasks and the {@link Autocompleter} will only handle search
 * requests.
 * 
 * Last changed: $Date: 2010-04-01 15:20:21 +0200 (Do, 01 Apr 2010) $
 * 
 * @version $Revision: 528 $
 * @author $Author: supnig@constantinopel.at $
 * @author Sebastian Vogel <s.vogel@gentics.com>
 */
public class Autocompleter implements IEventReceiver, AutocompleteConfigurationKeys {

	protected static final NodeLogger log = NodeLogger.getNodeLogger(Autocompleter.class);
	@Deprecated
	private LuceneIndexLocation source;
	private LuceneIndexLocation autocompleteLocation;
        
        private final Analyzer analyzer;

	private String autocompletefield = "content";

	@Deprecated
	private boolean autocompletereopenupdate = false;

	@Deprecated
	private long lastupdatestored = 0;

	@Deprecated
	private IReIndexStrategy reindexStrategy;

	/**
	 * to keep backward compatibility - new implementations must declare in the
	 * config if they use the new {@link AutocompleteIndexExtension} Class
	 */
	@Deprecated
	private boolean useAutocompleteIndexExtension = false;

	public Autocompleter(CRConfig config) {
		GenericConfiguration srcConf = (GenericConfiguration) config.get(SOURCE_INDEX_KEY);
		GenericConfiguration autoConf = (GenericConfiguration) config.get(AUTOCOMPLETE_INDEX_KEY);
		useAutocompleteIndexExtension = config.getBoolean(
			AUTOCOMPLETE_USE_AUTCOMPLETE_INDEXER,
			useAutocompleteIndexExtension);

		source = null;
		if (!useAutocompleteIndexExtension) {
			source = LuceneIndexLocation.getIndexLocation(new CRConfigUtil(srcConf, "SOURCE_INDEX_KEY"));
		}
		autocompleteLocation = LuceneIndexLocation
				.getIndexLocation(new CRConfigUtil(autoConf, AUTOCOMPLETE_INDEX_KEY));
		autocompleteLocation.registerDirectoriesSpecial();
                this.analyzer = LuceneAnalyzerFactory.createAnalyzer(autoConf);
		String s_autofield = config.getString(AUTOCOMPLETE_FIELD_KEY);
		if (!useAutocompleteIndexExtension) {
			reindexStrategy = initReindexStrategy(config);
		}
		if (s_autofield != null)
			this.autocompletefield = s_autofield;

		String sReopenUpdate = config.getString(AUTOCOMPLETE_REOPEN_UPDATE);
		if (sReopenUpdate != null) {
			autocompletereopenupdate = Boolean.parseBoolean(sReopenUpdate);
		}
	}

	

	public Collection<CRResolvableBean> suggestWords(CRRequest request) throws IOException {
		ArrayList<CRResolvableBean> result = new ArrayList<CRResolvableBean>();
		String term = request.getRequestFilter();
		// get the top 5 terms for query

		

		IndexAccessor ia = autocompleteLocation.getAccessor();
		IndexSearcher autoCompleteSearcher = ia.getPrioritizedSearcher();
		IndexReader autoCompleteReader = autoCompleteSearcher.getIndexReader();
                
                // analyze the search term
                String analyzedTerm = null;
                TokenStream stream  = analyzer.tokenStream(GRAMMED_WORDS_FIELD, new StringReader(term));            
                CharTermAttribute streamTerm = stream.addAttribute(CharTermAttribute.class);
                stream.reset();
                // get the last token from the stream
                while(stream.incrementToken()) {
                    analyzedTerm = streamTerm.toString();
                }
                stream.end();
                stream.close();
                // if the analyzer don't return any term, skip search because term is not valid
                if(analyzedTerm == null) {
                    return result;
                }
                
		try {
			Query query = new TermQuery(new Term(GRAMMED_WORDS_FIELD, analyzedTerm));
			Sort sort = new Sort(new SortField(COUNT_FIELD, SortField.Type.LONG, true));
			TopDocs docs = autoCompleteSearcher.search(query, null, 5, sort);
			int id = 1;
			for (ScoreDoc doc : docs.scoreDocs) {
				CRResolvableBean bean = new CRResolvableBean(id++);
				Document d = autoCompleteReader.document(doc.doc);
				bean.set(SOURCE_WORD_FIELD, d.get(SOURCE_WORD_FIELD));
				bean.set(COUNT_FIELD, d.get(COUNT_FIELD));
				result.add(bean);
			}
		} finally {
			ia.release(autoCompleteSearcher);
		}

		return result;
	}

	private void checkForUpdate() {
		// the new checkForUpdate Logic only calls reopenCheck on the
		// IndexLocation
		IndexAccessor ia = autocompleteLocation.getAccessor();
		autocompleteLocation.reopenCheck(ia, null);
		
	}

	public void finalize() {
		autocompleteLocation.stop();
	}

	/**
	 * from version 2.0.0 the {@link AutocompleteIndexExtension} is used for all
	 * Index related tasks and the {@link Autocompleter} will only handle search
	 * requests
	 * 
	 * Initialize a config class for the periodical execution flag of the
	 * indexer. If init of the configured class fails, a fallback class is
	 * returned.
	 * 
	 * @return configclass
	 * @param config
	 */
	@Deprecated
	private IReIndexStrategy initReindexStrategy(final CRConfig config) {
		String className = config.getString(REINDEXSTRATEGYCLASS_KEY);

		if (className != null && className.length() != 0) {
			try {
				Class<?> clazz = Class.forName(className);
				Constructor<?> constructor = clazz.getConstructor(CRConfig.class);
				return (IReIndexStrategy) constructor.newInstance(config);
			} catch (Exception e) {
				log.warn("Cound not init configured " + REINDEXSTRATEGYCLASS_KEY + ": " + className, e);
			}
		}
		return new ReIndexNoSkipStrategy(config);
	}
	@Override
	public void processEvent(Event event) {
		// TODO Auto-generated method stub
		
	}

}
