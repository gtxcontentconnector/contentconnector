package com.gentics.cr.lucene.autocomplete;

import java.lang.reflect.Constructor;

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
import com.gentics.lib.log.NodeLogger;

/**
 * This {@link IndexExtension} creates and maintains an autocomplete-index. The
 * {@link Autocompleter} uses the index created by this extension to provide
 * autocomplete search results.
 * 
 * 
 * @author Sebastian Vogel <s.vogel@gentics.com>
 */
public class AutocompleteIndexExtension extends AbstractIndexExtension implements IEventReceiver,
		AutocompleteConfigurationKeys {
	protected static final NodeLogger log = NodeLogger.getNodeLogger(AutocompleteIndexExtension.class);

	private static final String REINDEX_JOB = "reIndex";
	private static final String CLEAR_JOB = "clearAutocompleteIndex";
	private static final String[] jobs = { REINDEX_JOB, CLEAR_JOB };

	private CRConfig config;
	private LuceneIndexLocation source = null;
	private LuceneIndexLocation autocompleteLocation;
	private String autocompletefield = "content";

	private IReIndexStrategy reindexStrategy;

	private boolean autocompletereopenupdate = false;

	private boolean subscribeToIndexFinished = false;

	private IndexLocation callingIndexLocation;

	/**
	 * The constructor is called in the {@link IndexLocation}
	 * 
	 * @see AbstractIndexExtension#AbstractIndexExtension(CRConfig,
	 *      IndexLocation)
	 * @param config
	 * @param callingLocation
	 */
	public AutocompleteIndexExtension(CRConfig config, IndexLocation callingLocation) {
		super(config, callingLocation);
		this.config = config;
		this.callingIndexLocation = callingLocation;

		GenericConfiguration src_conf = (GenericConfiguration) config.get(SOURCE_INDEX_KEY);
		if (src_conf != null) {
			CRConfigUtil src_conf_util = new CRConfigUtil(src_conf, "SOURCE_INDEX_KEY");
			if (src_conf_util.getPropertySize() > 0) {
				source = LuceneIndexLocation.getIndexLocation(src_conf_util);
			}
		}
		if (source == null) {
			source = (LuceneIndexLocation) callingLocation;
		}

		GenericConfiguration autoConf = (GenericConfiguration) config.get(AUTOCOMPLETE_INDEX_KEY);
		autocompleteLocation = LuceneIndexLocation
				.getIndexLocation(new CRConfigUtil(autoConf, AUTOCOMPLETE_INDEX_KEY));
		autocompleteLocation.registerDirectoriesSpecial();

		String s_autofield = config.getString(AUTOCOMPLETE_FIELD_KEY);
		reindexStrategy = initReindexStrategy(config);
		if (s_autofield != null)
			this.autocompletefield = s_autofield;

		autocompletereopenupdate = config.getBoolean(AUTOCOMPLETE_REOPEN_UPDATE, autocompletereopenupdate);

		subscribeToIndexFinished = config
				.getBoolean(AUTOCOMPLETE_SUBSCRIBE_TO_INDEX_FINISHED, subscribeToIndexFinished);

		if (subscribeToIndexFinished) {
			EventManager.getInstance().register(this);
		}
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
			AbstractUpdateCheckerJob job = (AbstractUpdateCheckerJob) new AutocompleteIndexJob(config,
					callingLuceneLocation, this);
			callingLuceneLocation.getQueue().addJob(job);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see com.gentics.cr.util.indexing.AbstractIndexExtension#stop()
	 */
	@Override
	public void stop() {
		source.stop();
		autocompleteLocation.stop();
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
			AbstractUpdateCheckerJob job = (AbstractUpdateCheckerJob) new AutocompleteIndexJob(this.config,
					actualLocation, this);
			actualLocation.getQueue().addJob(job);
		} else if (CLEAR_JOB.equalsIgnoreCase(name)) {
			AbstractUpdateCheckerJob job = (AbstractUpdateCheckerJob) new AutocompleteIndexDeleteJob(this.config,
					actualLocation, this);
			actualLocation.getQueue().addJob(job);
		} else {
			throw new NoSuchMethodException("No Job-Method by the name: " + name);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.gentics.cr.util.indexing.AbstractIndexExtension#getJobs()
	 */
	@Override
	public String[] getJobs() {
		return jobs;
	}

	/**
	 * get the {@link LuceneIndexLocation} that is used as source for the autocomplete index
	 * @return the source index location
	 */
	public LuceneIndexLocation getSource() {
		return source;
	}

	/**
	 * get the {@link LuceneIndexLocation} where the autocomplete-index is
	 * stored
	 * 
	 * @return the autocomplete index location
	 */
	public LuceneIndexLocation getAutocompleteLocation() {
		return autocompleteLocation;
	}

	/**
	 * get the field in the source index which should be used for autocomplete
	 * 
	 * @return the name of field (default: content)
	 */
	public String getAutocompletefield() {
		return autocompletefield;
	}

}
