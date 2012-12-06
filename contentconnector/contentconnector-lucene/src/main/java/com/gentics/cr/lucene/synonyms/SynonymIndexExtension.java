package com.gentics.cr.lucene.synonyms;

import java.lang.reflect.Constructor;

import org.apache.log4j.Logger;

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
 * This {@link IndexExtension} creates and maintains an synonyms-index. 
 * The synonym Index will always get indexed new, when the indexer runs. There is actually no check if Synonyms are already there.
 * 
 * @author Patrick Höfer <p.hoefer@gentics.com>
 */

public class SynonymIndexExtension extends AbstractIndexExtension implements IEventReceiver {
	/**
	 * Logger.
	 */
	protected static final Logger log = Logger.getLogger(SynonymIndexExtension.class);

	/**
	 * Name for Reindex_job.
	 */
	private static final String REINDEX_JOB = "reIndex";
	
	/**
	 * Name for Clear_job.
	 */
	private static final String CLEAR_JOB = "clearSynonymsIndex";
	
	/**
	 * available jobs.
	 */
	private static final String[] jobs = { REINDEX_JOB, CLEAR_JOB };
	
	/**
	 * The CRConfig.
	 */
	private CRConfig config;

	/**
	 * The ReIndexStrategy.
	 */
	private IReIndexStrategy reindexStrategy;

	/**
	 * Run on IndexFinished?
	 */
	private boolean subscribeToIndexFinished = false;

	/**
	 * IndexLocation which calls the Extension.
	 */
	private IndexLocation callingIndexLocation;

	/**
	 * synonymLocation where the Index of the Extension will ne stored.
	 */
	private LuceneIndexLocation synonymLocation;

	/**
	 * Config-Key for synonymLocation in Config-File.
	 */
	private static final String SYNONYM_INDEX_KEY = "synonymlocation";

	/**
	 * Config-Key for reindexStrategy in Config-File.
	 */
	public static final String REINDEXSTRATEGYCLASS_KEY = "reindexStrategyClass";

	/**
	 * Config-Key for suscribeToIndexFinished in Config-File.
	 */
	public static final String SYNONYM_SUBSCRIBE_TO_INDEX_FINISHED = "reindexOnCRIndexFinished";

	private boolean all = false;

	/**
	 * The constructor is called in the {@link IndexLocation}.
	 * 
	 * @see AbstractIndexExtension#AbstractIndexExtension(CRConfig,
	 *      IndexLocation)
	 * @param config the CRConfig for the Extension
	 * @param callingLocation the IndexLOcation which call the Extension
	 */
	public SynonymIndexExtension(final CRConfig config, IndexLocation callingLocation) {
		super(config, callingLocation);
		this.config = config;
		this.callingIndexLocation = callingLocation;

		GenericConfiguration synonymConf = (GenericConfiguration) config.get(SYNONYM_INDEX_KEY);
		synonymLocation = LuceneIndexLocation.getIndexLocation(new CRConfigUtil(synonymConf, SYNONYM_INDEX_KEY));
		synonymLocation.registerDirectoriesSpecial();

		reindexStrategy = initReindexStrategy(config);

		subscribeToIndexFinished = config.getBoolean(SYNONYM_SUBSCRIBE_TO_INDEX_FINISHED, subscribeToIndexFinished);

		if (subscribeToIndexFinished) {
			EventManager.getInstance().register(this);
		}

		log.debug("Succesfully registered SynonymIndexExtension");
	}

	/**
	 * EventHandler: subscribes to the
	 * {@link IndexingFinishedEvent#INDEXING_FINISHED_EVENT_TYPE}. <br>
	 * If enabled in the config this method adds a reIndexing Job to the queue
	 * of the {@link IndexLocation} which fired the event
	 * 
	 * @param event the Event which is fired
	 */
	public final void processEvent(final Event event) {
		if (!subscribeToIndexFinished || !IndexingFinishedEvent.INDEXING_FINISHED_EVENT_TYPE.equals(event.getType())) {
			return;
		}

		Object obj = event.getData();
		LuceneIndexLocation callingLuceneLocation = (LuceneIndexLocation) callingIndexLocation;

		if (!callingLuceneLocation.equals(obj)) {
			return;
		}

		if (!reindexStrategy.skipReIndex(callingLuceneLocation)) {
			//clear SynonymIndexLocation everytime before a SynonymIndexJob is created.
			AbstractUpdateCheckerJob job = new SynonymIndexDeleteJob(config, callingLuceneLocation, this);
			callingLuceneLocation.getQueue().addJob(job);
			job = new SynonymIndexJob(config, callingLuceneLocation, this);
			callingLuceneLocation.getQueue().addJob(job);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see com.gentics.cr.util.indexing.AbstractIndexExtension#stop()
	 */
	@Override
	public void stop() {
		if (synonymLocation != null) {
			synonymLocation.stop();
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
	 * @return IReIndexStrategy
	 * @param config configuration
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
	public void addJob(final String name) throws NoSuchMethodException {
		addJob(name, null);
	}

	/*
	 * (non-Javadoc)
	 * @see com.gentics.cr.util.indexing.AbstractIndexExtension#addJob(java.lang. String,
	 * com.gentics.cr.util.indexing.IndexLocation)
	 */
	@Override
	public void addJob(final String name, final IndexLocation indexLocation) throws NoSuchMethodException {
		IndexLocation actualLocation = callingIndexLocation;
		if (indexLocation != null) {
			actualLocation = indexLocation;
		}

		if (REINDEX_JOB.equalsIgnoreCase(name)){
			AbstractUpdateCheckerJob job = new SynonymIndexJob(this.config, actualLocation, this);
			actualLocation.getQueue().addJob(job);
		} else if (CLEAR_JOB.equalsIgnoreCase(name)) {
			AbstractUpdateCheckerJob job = new SynonymIndexDeleteJob(config, actualLocation, this);
			actualLocation.getQueue().addJob(job);
		} else {
			throw new NoSuchMethodException("No Job-Method by the name: " + name);
		}
	}

	/**
	 * @return all as boolean
	 */
	public final boolean isAll() {
		return all;
	}

	/**
	 * @param all the all to set
	 */
	public final void setAll(final boolean all) {
		this.all = all;
	}
	
	
	/**
	 * @return the SynonymIndexExtension
	 */
	public final LuceneIndexLocation getSynonymLocation() {
		return synonymLocation;
	}


	/*
	 * (non-Javadoc)
	 * @see com.gentics.cr.util.indexing.AbstractIndexExtension#getJobs()
	 */
	@Override
	public String[] getJobs() {
		return jobs;
	}

}
