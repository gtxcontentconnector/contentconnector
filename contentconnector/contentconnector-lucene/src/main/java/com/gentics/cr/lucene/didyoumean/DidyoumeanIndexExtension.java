package com.gentics.cr.lucene.didyoumean;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.apache.lucene.search.spell.CustomSpellChecker;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.events.Event;
import com.gentics.cr.events.EventManager;
import com.gentics.cr.events.IEventReceiver;
import com.gentics.cr.lucene.events.IndexingFinishedEvent;
import com.gentics.cr.lucene.indexer.index.LuceneIndexLocation;
import com.gentics.cr.util.indexing.AbstractIndexExtension;
import com.gentics.cr.util.indexing.AbstractUpdateCheckerJob;
import com.gentics.cr.util.indexing.IReIndexStrategy;
import com.gentics.cr.util.indexing.IndexExtension;
import com.gentics.cr.util.indexing.IndexLocation;
import com.gentics.cr.util.indexing.ReIndexNoSkipStrategy;

/**
 * This {@link IndexExtension} creates and maintains an autocomplete-index. The
 * {@link DidYouMeanProvider} uses the index created by this extension to
 * provide didyoumean search suggestions.
 * 
 */
public class DidyoumeanIndexExtension extends AbstractIndexExtension implements IEventReceiver {
	protected static final Logger log = Logger.getLogger(DidyoumeanIndexExtension.class);

	private static final String REINDEX_JOB = "reIndex";
	private static final String CLEAR_JOB = "clearDidyoumeanIndex";
	private static final String[] jobs = { REINDEX_JOB, CLEAR_JOB };

	private CRConfig config;

	private IReIndexStrategy reindexStrategy;

	private boolean subscribeToIndexFinished = false;

	private IndexLocation callingIndexLocation;

	private LuceneIndexLocation sourceLocation;
	private LuceneIndexLocation didyoumeanLocation;

	private static final String SOURCE_INDEX_KEY = "srcindexlocation";

	private static final String DIDYOUMEAN_INDEX_KEY = "didyoumeanlocation";

	private static final String DIDYOUMEAN_FIELD_KEY = "didyoumeanfields";

	private static final String DIDYOUMEAN_MIN_DISTANCESCORE = "didyoumeanmindistancescore";

	private static final String DIDYOUMEAN_MIN_DOCFREQ = "didyoumeanmindocfreq";

	public static final String REINDEXSTRATEGYCLASS_KEY = "reindexStrategyClass";

	public static final String DIDYOUMEAN_SUBSCRIBE_TO_INDEX_FINISHED = "reindexOnCRIndexFinished";

	private String didyoumeanfield = "all";

	private CustomSpellChecker spellchecker = null;

	private boolean all = false;

	private Collection<String> dym_fields = null;

	private Float minDScore = (float) 0.0;

	private Integer minDFreq = 0;

	/**
	 * The constructor is called in the {@link IndexLocation}
	 * 
	 * @see AbstractIndexExtension#AbstractIndexExtension(CRConfig,
	 *      IndexLocation)
	 * @param config
	 * @param callingLocation
	 */
	public DidyoumeanIndexExtension(CRConfig config, IndexLocation callingLocation) {
		super(config, callingLocation);
		this.config = config;
		this.callingIndexLocation = callingLocation;

		GenericConfiguration srcConf = (GenericConfiguration) config.get(SOURCE_INDEX_KEY);
		CRConfigUtil srcConfUtil = new CRConfigUtil(srcConf, "SOURCE_INDEX_KEY");
		if (srcConfUtil.getPropertySize() > 0) {
			sourceLocation = LuceneIndexLocation.getIndexLocation(srcConfUtil);
		}
		if (sourceLocation == null) {
			sourceLocation = (LuceneIndexLocation) callingLocation;
		}

		GenericConfiguration didyouConf = (GenericConfiguration) config.get(DIDYOUMEAN_INDEX_KEY);
		didyoumeanLocation = LuceneIndexLocation.getIndexLocation(new CRConfigUtil(didyouConf, DIDYOUMEAN_INDEX_KEY));
		didyoumeanLocation.registerDirectoriesSpecial();

		didyoumeanfield = config.getString(DIDYOUMEAN_FIELD_KEY, didyoumeanfield);
		minDScore = config.getFloat(DIDYOUMEAN_MIN_DISTANCESCORE, (float) 0.0);
		minDFreq = config.getInteger(DIDYOUMEAN_MIN_DOCFREQ, 0);

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
		reindexStrategy = initReindexStrategy(config);

		subscribeToIndexFinished = config.getBoolean(DIDYOUMEAN_SUBSCRIBE_TO_INDEX_FINISHED, subscribeToIndexFinished);

		if (subscribeToIndexFinished) {
			EventManager.getInstance().register(this);
		}

		try {
			spellchecker = new CustomSpellChecker(didyoumeanLocation, minDScore, minDFreq);
		} catch (IOException e) {
			log.debug("Could not create Spellchecker", e);
			// without spellchecker the extension won't work - stop it
			this.stop();
		}

		log.debug("Succesfully registered DidyoumeanIndexExtension");
	}

	/**
	 * EventHandler: subscribes to the
	 * {@link IndexingFinishedEvent#INDEXING_FINISHED_EVENT_TYPE}. <br>
	 * If enabled in the config this method adds a reIndexing Job to the queue
	 * of the {@link IndexLocation} which fired the event
	 */
	public void processEvent(Event event) {
		if (!subscribeToIndexFinished || !IndexingFinishedEvent.INDEXING_FINISHED_EVENT_TYPE.equals(event.getType())) {
			return;
		}

		Object obj = event.getData();
		LuceneIndexLocation callingLuceneLocation = (LuceneIndexLocation) callingIndexLocation;

		if (!callingLuceneLocation.equals(obj)) {
			return;
		}

		if (!reindexStrategy.skipReIndex(callingLuceneLocation)) {
			AbstractUpdateCheckerJob job = new DidyoumeanIndexJob(config, callingLuceneLocation, this);
			callingLuceneLocation.getQueue().addJob(job);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see com.gentics.cr.util.indexing.AbstractIndexExtension#stop()
	 */
	@Override
	public void stop() {
		sourceLocation.stop();
		if (spellchecker != null) {
			spellchecker.close();
		} else if (didyoumeanLocation != null) {
			didyoumeanLocation.stop();
		}
		if (subscribeToIndexFinished) {
			EventManager.getInstance().unregister(this);
		}
	}

	/**
	 * Initialize a config class for the periodical execution flag of the
	 * indexer. If init of the configured class fails, a fallback class is
	 * returned.
	 * 
	 * @return configclass
	 * @param config
	 */
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

	/*
	 * (non-Javadoc)
	 * @see com.gentics.cr.util.indexing.AbstractIndexExtension#addJob(java.lang. String)
	 */
	@Override
	public void addJob(String name) throws NoSuchMethodException {
		addJob(name, null);
	}

	/*
	 * (non-Javadoc)
	 * @see com.gentics.cr.util.indexing.AbstractIndexExtension#addJob(java.lang. String,
	 * com.gentics.cr.util.indexing.IndexLocation)
	 */
	@Override
	public void addJob(String name, IndexLocation indexLocation) throws NoSuchMethodException {
		IndexLocation actualLocation = callingIndexLocation;
		if (indexLocation != null) {
			actualLocation = indexLocation;
		}

		if (REINDEX_JOB.equalsIgnoreCase(name)) {
			AbstractUpdateCheckerJob job = new DidyoumeanIndexJob(this.config, actualLocation, this);
			actualLocation.getQueue().addJob(job);
		} else if (CLEAR_JOB.equalsIgnoreCase(name)) {
			AbstractUpdateCheckerJob job = new DidyoumeanIndexDeleteJob(config, actualLocation, this);
			actualLocation.getQueue().addJob(job);
		} else {
			throw new NoSuchMethodException("No Job-Method by the name: " + name);
		}
	}

	public boolean isAll() {
		return all;
	}

	public void setAll(boolean all) {
		this.all = all;
	}

	public Collection<String> getDym_fields() {
		return dym_fields;
	}

	public void setDym_fields(Collection<String> dym_fields) {
		this.dym_fields = dym_fields;
	}

	public LuceneIndexLocation getDidyoumeanLocation() {
		return didyoumeanLocation;
	}

	public LuceneIndexLocation getSourceLocation() {
		return sourceLocation;
	}

	public CustomSpellChecker getSpellchecker() {
		return spellchecker;
	}

	public void setSpellchecker(CustomSpellChecker spellchecker) {
		this.spellchecker = spellchecker;
	}

	/*
	 * (non-Javadoc)
	 * @see com.gentics.cr.util.indexing.AbstractIndexExtension#getJobs()
	 */
	@Override
	public String[] getJobs() {
		return jobs;
	}

	public Float getMinDScore() {
		return minDScore;
	}

	public Integer getMinDFreq() {
		return minDFreq;
	}

}
