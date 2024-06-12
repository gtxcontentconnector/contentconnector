package com.gentics.cr.lucene.autocomplete;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.suggest.InputIterator;
import org.apache.lucene.util.BytesRef;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.lucene.indexaccessor.IndexAccessor;
import com.gentics.cr.lucene.indexer.IndexerUtil;
import com.gentics.cr.lucene.indexer.index.LuceneIndexLocation;
import com.gentics.cr.monitoring.MonitorFactory;
import com.gentics.cr.monitoring.UseCase;
import com.gentics.cr.util.indexing.AbstractUpdateCheckerJob;
import com.gentics.cr.util.indexing.IndexLocation;

/**
 * This job is used to re-index (or newly index) the autocomplete-index 
 * 
 * @author Sebastian Vogel <s.vogel@gentics.com>
 *
 */
public class AutocompleteIndexJob extends AbstractUpdateCheckerJob implements AutocompleteConfigurationKeys {

	private AutocompleteIndexExtension autocompleter;

	public AutocompleteIndexJob(CRConfig updateCheckerConfig, IndexLocation indexLoc,
		AutocompleteIndexExtension autocompleter) {
		super(updateCheckerConfig, indexLoc, null);

		this.identifyer = identifyer.concat(":reIndex");
		log = Logger.getLogger(AutocompleteIndexJob.class);
		this.autocompleter = autocompleter;
	}

	/**
	 * starts the job - is called by the IndexJobQueue
	 */
	@Override
	protected void indexCR(IndexLocation indexLocation, CRConfigUtil config) throws CRException {
		try {
			reIndex();
		} catch (IOException e) {
			throw new CRException("Could not access the Autocomplete index! " + e.getMessage());
		}

	}

	private synchronized void reIndex() throws IOException {
		UseCase ucReIndex = MonitorFactory.startUseCase("reIndex()");
		// build a dictionary (from the spell package)
		log.debug("Starting to reindex autocomplete index.");

		LuceneIndexLocation source = this.autocompleter.getSource();
		LuceneIndexLocation autocompleteLocation = this.autocompleter.getAutocompleteLocation();
		String autocompletefield = this.autocompleter.getAutocompletefield();

		IndexAccessor sia = source.getAccessor();
		IndexReader sourceReader = sia.getReader();
		IndexAccessor aia = autocompleteLocation.getAccessor();
		// IndexReader reader = aia.getReader(false);
		IndexWriter writer = aia.getWriter();
		
		Map<String, Integer> wordsMap = new HashMap<String, Integer>();

		//split autocompleteField by , to use more than one field for autocompletion
		List<String> autocompletefields = IndexerUtil.getListFromString(autocompletefield, ",");
	

		try {
			//go through every configured autocompleteField
			for (String autoField : autocompletefields){				
				// go through every word, storing the original word (incl. n-grams)
				// and the number of times it occurs
				// CREATE WORD LIST FROM SOURCE INDEX
				
				LuceneDictionary dict = new LuceneDictionary(sourceReader, autoField);			
				InputIterator iter = dict.getEntryIterator();

				BytesRef ref = iter.next();
				while (ref != null) {
					String word = ref.utf8ToString();
					int len = word.length();
					if (len < 3) {
						ref = iter.next();
						continue; // too short we bail but "too long" is fine...
					}
					
					int wordCount = sourceReader.docFreq(new Term(autoField, word));
					//if there is already an entry for this word, we add current count to existing cound
					if (wordsMap.containsKey(word)) {
						wordsMap.put(word,(wordsMap.get(word)+wordCount));
					} else {
						// use the number of documents this word appears in
						wordsMap.put(word, wordCount);
					}
					ref = iter.next();
				}
			}
			// DELETE OLD OBJECTS FROM INDEX
			writer.deleteAll();

			// UPDATE DOCUMENTS IN AUTOCOMPLETE INDEX
			for (String word : wordsMap.keySet()) {
				// ok index the word
				Document doc = new Document();
				doc.add(new Field(SOURCE_WORD_FIELD, word, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS)); // orig term
				doc.add(new Field(GRAMMED_WORDS_FIELD, word, Field.Store.YES, Field.Index.ANALYZED)); // grammed
				doc.add(new Field(COUNT_FIELD, Integer.toString(wordsMap.get(word)), Field.Store.YES,
						Field.Index.NOT_ANALYZED_NO_NORMS)); // count
				writer.addDocument(doc);
			}
			autocompleteLocation.createReopenFile();
		} finally {

			sia.release(sourceReader);
			// close writer

			aia.release(writer);
			// aia.release(reader,false);
		}
		log.debug("Finished reindexing autocomplete index.");
		ucReIndex.stop();
	}

}
