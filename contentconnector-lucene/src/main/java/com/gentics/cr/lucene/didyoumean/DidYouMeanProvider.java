package com.gentics.cr.lucene.didyoumean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.spell.CustomSpellChecker;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.store.Directory;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.events.Event;
import com.gentics.cr.events.EventManager;
import com.gentics.cr.events.IEventReceiver;
import com.gentics.cr.lucene.events.IndexingFinishedEvent;
import com.gentics.cr.lucene.indexer.index.LuceneIndexLocation;
import com.gentics.cr.monitoring.MonitorFactory;
import com.gentics.cr.monitoring.UseCase;

/**
 * This class can be used to build an autocomplete index over an existing lucene
 * index.
 * 
 * Last changed: $Date: 2010-04-01 15:20:21 +0200 (Do, 01 Apr 2010) $
 * 
 * @version $Revision: 528 $
 * @author $Author: supnig@constantinopel.at $
 * 
 */
public class DidYouMeanProvider implements IEventReceiver {

	protected static final Logger log = Logger
			.getLogger(DidYouMeanProvider.class);

	@Deprecated
	private Directory source = null;
	
	//@Deprecated
	//private Directory didyoumeanDirectory;
	
	private LuceneIndexLocation didyoumeanLocation;

	private static final String SOURCE_INDEX_KEY = "srcindexlocation";

	private static final String DIDYOUMEAN_INDEX_KEY = "didyoumeanlocation";

	private static final String DIDYOUMEAN_FIELD_KEY = "didyoumeanfields";

	private static final String DIDYOUMEAN_MIN_DISTANCESCORE = "didyoumeanmindistancescore";

	private static final String DIDYOUMEAN_MIN_DOCFREQ = "didyoumeanmindocfreq";

	/**
	 * Configuration key to activate the didyoumean feature for terms that are
	 * in the index but have a low result size.
	 */
	private static final String DIDYOUMEAN_EXISTINGTERMS_KEY = "didyoumean_forexisitingterms";

	/**
	 * 
	 */
	private static final String DIDYOUMEAN_USE_INDEX_EXTENSION = "didyoumeanUseIndexExtension";

	private String didyoumeanfield = "all";

	private CustomSpellChecker spellchecker = null;

	private boolean all = false;

	/**
	 * Mark if we should provide the didyoumean feature for existing terms (with
	 * low result count).
	 */
	private boolean checkForExistingTerms = false;

	private Collection<String> dym_fields = null;

	private boolean dymreopenupdate = false;

	private static final String UPDATE_ON_REOPEN_KEY = "dymreopenupdate";

	/**
	 * flag to indicate if the new DidyoumeanIndexExtension should be used <br>
	 * new implementations must set the config key "useDidyomeanIndexExtension"
	 * to true to use the extension
	 */
	@Deprecated
	private boolean useDidyomeanIndexExtension = false;

	private Float minDScore = null;
	private Integer minDFreq = null;

	public DidYouMeanProvider(CRConfig config) {

		useDidyomeanIndexExtension = config.getBoolean(
				DIDYOUMEAN_USE_INDEX_EXTENSION, useDidyomeanIndexExtension);

		if (!useDidyomeanIndexExtension) {
			GenericConfiguration src_conf = (GenericConfiguration) config
					.get(SOURCE_INDEX_KEY);
			source = LuceneIndexLocation.createDirectory(new CRConfigUtil(
					src_conf, "SOURCE_INDEX_KEY"));
		}

		GenericConfiguration auto_conf = (GenericConfiguration) config
				.get(DIDYOUMEAN_INDEX_KEY);
		CRConfigUtil dymConfUtil = new CRConfigUtil(auto_conf,
				DIDYOUMEAN_INDEX_KEY);
		didyoumeanLocation = LuceneIndexLocation
				.getIndexLocation(dymConfUtil);
		if (!useDidyomeanIndexExtension) {
			didyoumeanLocation.registerDirectoriesSpecial();
		} 

		checkForExistingTerms = config.getBoolean(DIDYOUMEAN_EXISTINGTERMS_KEY,
				checkForExistingTerms);

		minDScore = config.getFloat(DIDYOUMEAN_MIN_DISTANCESCORE, (float) 0.0);
		minDFreq = config.getInteger(DIDYOUMEAN_MIN_DOCFREQ, 0);

		didyoumeanfield = config.getString(DIDYOUMEAN_FIELD_KEY,
				didyoumeanfield);

		// FETCH DYM FIELDS
		if (this.didyoumeanfield.equalsIgnoreCase("ALL")) {
			all = true;
		} else if (this.didyoumeanfield.contains(",")) {
			String[] arr = this.didyoumeanfield.split(",");
			dym_fields = new ArrayList<String>(Arrays.asList(arr));
		} else {
			dym_fields = new ArrayList<String>(1);
			dym_fields.add(this.didyoumeanfield);
		}


		try {
			spellchecker = new CustomSpellChecker(didyoumeanLocation,
					minDScore, minDFreq);
		} catch (IOException e1) {
			log.error("Could not create didyoumean index.", e1);
		}

		if (!useDidyomeanIndexExtension) {
			String sDYMReopenUpdate = config.getString(UPDATE_ON_REOPEN_KEY);
			if (sDYMReopenUpdate != null) {
				dymreopenupdate = Boolean.parseBoolean(sDYMReopenUpdate);
			}

			try {
				reIndex();

			} catch (IOException e) {
				
			}

			EventManager.getInstance().register(this);
		}
	}

	// @Deprecated
	// public DidYouMeanProvider(CRConfig config)
	// {
	// this(config, false);
	// }

	public void processEvent(Event event) {
		if (IndexingFinishedEvent.INDEXING_FINISHED_EVENT_TYPE.equals(event
				.getType()) && !useDidyomeanIndexExtension) {
			try {
				reIndex();
			} catch (IOException e) {
				log.error("Could not reindex didyoumean index.", e);
			}
		}
	}

	public CustomSpellChecker getInitializedSpellchecker() {
		return this.spellchecker;
	}

	private long lastupdatestored = 0;

	
	private void checkForUpdate() {

		
		if (!useDidyomeanIndexExtension) {
			boolean reopened = false;
			try {
				if (source.fileExists("reopen")) {
					long lastmodified = source.fileModified("reopen");
					if (lastmodified != lastupdatestored) {
						reopened = true;
						lastupdatestored = lastmodified;
					}
				}
				if (reopened) {
					reIndex();
				}
			} catch (IOException e) {
				log.debug("Could not reIndex autocomplete index.", e);
			}
		} 
		
	}

	/**
	 * TODO javadoc.
	 * 
	 * @param termlist
	 *            TODO javadoc
	 * @param count
	 *            TODO javadoc
	 * @param reader
	 *            TODO javadoc
	 * @return TODO javadoc
	 */
	public Map<String, String[]> getSuggestions(Set<Term> termlist, int count,
			IndexReader reader) {
		return getSuggestionsStringFromMap(getSuggestionTerms(termlist, count,
				reader));
	}

	public Map<Term, Term[]> getSuggestionTerms(Set<Term> termlist, int count,
			IndexReader reader) {

		if (dymreopenupdate) {
			checkForUpdate();
		}
		Map<Term, Term[]> result = new LinkedHashMap<Term, Term[]>();
		Set<Term> termset = new HashSet<Term>();
		
		if (this.spellchecker != null) {
			for (Term t : termlist) {
				// CHECK IF ALL FIELDS ENABLED FOR SUGGESTIONS OTHERWHISE ONLY
				// ADD TERM IF IT COMES FROM A DYM FIELD
				if (all || dym_fields.contains(t.field())) {
					termset.add(t);
				}
			}
			log.debug("Will use the following fields for dym: "
					+ dym_fields.toString());
			for (Term term : termset) {
				try {
					if (checkForExistingTerms
							|| !this.spellchecker.exist(term.text())) {
						String[] ts = this.spellchecker.suggestSimilar(
								term.text(), count, reader, term.field(), true);
						if (ts != null && ts.length > 0) {
							Term[] suggestedTerms = new Term[ts.length];
							for (int i = 0; i < ts.length; i++) {
								suggestedTerms[i] = term.createTerm(ts[i]);
							}
							result.put(term, suggestedTerms);
						}
					}
				} catch (IOException ex) {
					log.error("Could not suggest terms", ex);
				}
			}
		} else {
			log.error("Spellchecker has not properly been initialized.");
		}
		return result;
	}

	@Deprecated
	private synchronized void reIndex() throws IOException {
		UseCase ucReIndex = MonitorFactory.startUseCase("reIndex()");
		// build a dictionary (from the spell package)
		log.debug("Starting to reindex didyoumean index.");
		IndexReader sourceReader = IndexReader.open(source);
		Collection<String> fields = null;
		if (all) {
			fields = sourceReader.getFieldNames(IndexReader.FieldOption.ALL);
		} else {
			fields = dym_fields;
		}
		try {
			for (String fieldname : fields) {
				LuceneDictionary dict = new LuceneDictionary(sourceReader,
						fieldname);
				spellchecker.indexDictionary(dict);
			}
		} finally {
			sourceReader.close();
		}
		log.debug("Finished reindexing didyoumean index.");
		ucReIndex.stop();
	}

	public void finalize() {
		didyoumeanLocation.stop();
		EventManager.getInstance().unregister(this);
	}

	public Map<String, String[]> getSuggestionsStringFromMap(
			Map<Term, Term[]> suggestions) {
		Map<String, String[]> result = new LinkedHashMap<String, String[]>();
		for (Term key : suggestions.keySet()) {
			Term[] values = suggestions.get(key);
			ArrayList<String> valueStrings = new ArrayList<String>(
					values.length);
			for (Term value : values) {
				valueStrings.add(value.text());
			}
			result.put(key.text(),
					valueStrings.toArray(new String[valueStrings.size()]));
		}
		return result;
	}

}
