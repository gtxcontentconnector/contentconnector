package com.gentics.cr.util.indexing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.exceptions.WrongOrderException;
import com.gentics.cr.monitoring.MonitorFactory;
import com.gentics.cr.monitoring.UseCase;

/**
 * This class is designed as an UpdateChecker for a ContentRepository. It checks
 * a Gentics ContentRepository for Updates and gives updated Documents to some
 * class
 * @author perhab
 *
 */
public abstract class AbstractUpdateCheckerJob implements Runnable {

	/**
	 * Log4j logger for error and debug messages.
	 */
	protected static Logger log = Logger.getLogger(AbstractUpdateCheckerJob.class);

	/**
	 * Name of class to use for IndexLocation, must extend
	 * {@link com.gentics.cr.util.indexing.IndexLocation}.
	 */
	public static final String INDEXLOCATIONCLASS = "com.gentics.cr.util.indexing.IndexLocation";

	/**
	 * Configuration key for the attribute containing the id.
	 * @see #idAttribute
	 */
	protected static final String ID_ATTRIBUTE_KEY = "IDATTRIBUTE";

	/**
	 * Configuration key for the attribute containing an indicator if the object
	 * was updated.
	 * @see #timestampAttribute
	 */
	private static final String TIMESTAMP_ATTR_KEY = "updateattribute";

	/**
	 * Configuration of the UpdateCheckerJob.
	 */
	protected CRConfig config;
	/**
	 * Identifier of the UpdateCheckerJob.
	 */
	protected String identifyer;
	/**
	 * Status of the Indexer.
	 */
	protected IndexerStatus status;

	/**
	 * Name of the attribute identifying the beans.
	 */
	protected String idAttribute = "contentid";

	/**
	 * Name of the attribute indicating if the object has changed. This
	 * attribute should change whenever the object is changed in the repository.
	 */
	protected String timestampAttribute = "";

	private ConcurrentHashMap<String, CRConfigUtil> configmap;

	/**
	 * index location to compare the objects with.
	 */
	protected IndexLocation indexLocation;

	/**
	 * marker for saving the duration in milliseconds.
	 */
	private long duration = 0;

	/**
	 * start time of the job as timestamp.
	 */
	private long start = 0;

	/**
	 * Initialises the default values for any implementation of the
	 * {@link AbstractUpdateCheckerJob}.
	 * @param updateCheckerConfig Configuration of the update job
	 * @param indexLoc index location to compare with the repository
	 * @param updateCheckerConfigmap TODO javadoc
	 */
	public AbstractUpdateCheckerJob(final CRConfig updateCheckerConfig, final IndexLocation indexLoc,
		final ConcurrentHashMap<String, CRConfigUtil> updateCheckerConfigmap) {
		config = updateCheckerConfig;
		configmap = updateCheckerConfigmap;
		if (configmap == null) {
			log.debug("Configmap is empty");
		}
		identifyer = (String) updateCheckerConfig.getName();
		indexLocation = indexLoc;
		status = new IndexerStatus();
		idAttribute = updateCheckerConfig.getString(ID_ATTRIBUTE_KEY, idAttribute);
		timestampAttribute = updateCheckerConfig.getString(TIMESTAMP_ATTR_KEY, timestampAttribute);
	}

	/**
	 * Gets the config for this UpdateCheckerJob.
	 * 
	 * @return configuration as CRConfig-object
	 */
	public final CRConfig getConfig() {
		return config;
	}

	/**
	 * Gets the Job Identifyer. In most cases this is the CR id.
	 * @return identifyer as string
	 */
	public final String getIdentifyer() {
		return identifyer;
	}

	/**
	 * Get job duration in milliseconds.
	 * @return duration of the job in milliseconds
	 */
	public final long getDuration() {
		return duration;
	}

	/**
	 * Get the job's start time as timestamp.
	 * @return start time of the job as timestamp
	 */
	public final long getStart() {
		return this.start;
	}

	/**
	 * Get the job's start time as date.
	 * @return start time of the job as date
	 */
	public final Date getStartDate() {
		return new Date(getStart());
	}

	/**
	 * Get total count of objects to index.
	 * @return object count as int.
	 */
	public final int getObjectsToIndex() {
		return status.getObjectCount();
	}

	/**
	 * Get the number ob objects already indexed.
	 * @return objects already indexed in the current job
	 */
	public final int getObjectsDone() {
		return status.getObjectsDone();
	}

	/**
	 * Calculates ETA of the current job.
	 * @return ETA in ms
	 */
	public final long getETA() {
		long eta = 0;

		long objDone = this.getObjectsDone();
		long objToIndex = this.getObjectsToIndex();
		long objTodo = objToIndex - objDone;

		long timetaken = System.currentTimeMillis() - this.getStart();

		long timePerObj = 0;
		if (objDone != 0) {
			timePerObj = timetaken / objDone;
		}

		eta = objTodo * timePerObj;

		return eta;
	}

	/**
	 * Get Current Status as String.
	 * @return current status string
	 */
	public final String getStatusString() {
		return status.getCurrentStatusString();
	}

	/**
	 * Check if job had an error.
	 * @return true if error.
	 */
	public final boolean hasError() {
		return status.hasError();
	}

	/**
	 * Get the current error message if set.
	 * @return error message.
	 */
	public final String getErrorMessage() {
		return status.getErrorMessage();
	}

	/**
	 * Tests if a {@link AbstractUpdateCheckerJob} has the same identifier as
	 * the given object being an instance of {@link AbstractUpdateCheckerJob}.
	 * @param obj Object to test if it is equal to this
	 * @return <code>true</code> if is equal to obj, otherwise false.
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof AbstractUpdateCheckerJob) {
			if (this.identifyer.equalsIgnoreCase(((AbstractUpdateCheckerJob) obj).getIdentifyer())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.identifyer.hashCode();
	}

	/**
	 * get the objects to update and update them in the index.
	 * @param indexLocation - index location to update
	 * @param config - configuration of the update job
	 * @throws CRException in case something goes wrong please document in your implementation if you plan to throw an exception
	 */
	protected abstract void indexCR(IndexLocation indexLocation, CRConfigUtil config) throws CRException;

	/**
	 * get all objects that are not up to date.
	 * @param forceFullUpdate - boolean use to force a full update in the index
	 * @param request - Request describing the objects to index.
	 * @param rp - RequestProcessor to get the objects from.
	 * @param indexUpdateChecker - update checker for the index.
	 * @return {@link Collection} of {@link CRResolvableBean} that need to be
	 * updated in the index.
	 * @see IndexUpdateChecker#isUpToDate(String, Object, String,
	 * com.gentics.api.lib.resolving.Resolvable)
	 * @see IndexUpdateChecker#deleteStaleObjects()
	 */
	protected Collection<CRResolvableBean> getObjectsToUpdate(final CRRequest request, final RequestProcessor rp,
			final boolean forceFullUpdate, final IndexUpdateChecker indexUpdateChecker) {
		Collection<CRResolvableBean> updateObjects = new Vector<CRResolvableBean>();

		UseCase objectsToUpdateCase = MonitorFactory.startUseCase("AbstractUpdateCheck.getObjectsToUpdate("
				+ request.get("CRID") + ")");
		try {
			if (forceFullUpdate || "".equals(timestampAttribute)) {
				try {
					updateObjects = (Collection<CRResolvableBean>) rp.getObjects(request);
				} catch (CRException e) {
					String message = "Error getting objects to full index from " + "RequestProcessor. "
							+ e.getMessage();
					log.error(message, e);
					status.setError(message);
				}
			} else {
				//Sorted (by the idAttribute) list of Resolvables to check for
				//Updates.
				Collection<CRResolvableBean> objectsToIndex;
				try {
					defaultizeRequest(request);
					objectsToIndex = (Collection<CRResolvableBean>) rp.getObjects(request);
				} catch (CRException e) {
					String message = "Error getting objects to index from " + "RequestProcessor. " + e.getMessage();
					log.error(message, e);
					status.setError(message);
					return null;
				}
				Iterator<CRResolvableBean> resolvableIterator = objectsToIndex.iterator();
				try {
					while (resolvableIterator.hasNext()) {
						CRResolvableBean crElement = resolvableIterator.next();
						Object crElementIDObject = crElement.get(idAttribute);
						if (crElementIDObject == null) {
							log.error("IDAttribute is null!");
						}
						String crElementID = crElementIDObject.toString();
						Object crElementTimestamp = crElement.get(timestampAttribute);
						//TODO: if any transformers change an attribute that is used for the update check we have to run the transformers
						//before
						if (!indexUpdateChecker.isUpToDate(
							crElementID,
							crElementTimestamp,
							timestampAttribute,
							crElement)) {
							updateObjects.add(crElement);
						}
					}
				} catch (WrongOrderException e) {
					log.error("Got the objects from the datasource in the wrong" + "order.", e);
					status.setError("Got the objects from the datasource in the" + "wrong order.");
					return null;
				}
			}
			//Finally delete all Objects from Index that are not checked for an
			//Update
			//fixing possible npe
			if (indexUpdateChecker != null) {
				indexUpdateChecker.deleteStaleObjects();
			}
		} finally {
			objectsToUpdateCase.stop();
		}
		return updateObjects;
	}

	private void defaultizeRequest(CRRequest request) {
		String[] prefill = request.getAttributeArray(idAttribute);
		List<String> prefillList = Arrays.asList(prefill);
		if (!"".equals(timestampAttribute) && !prefillList.contains(timestampAttribute)) {
			ArrayList<String> pf = new ArrayList<String>(prefillList);
			pf.add(timestampAttribute);
			request.setAttributeArray(pf.toArray(prefill));
		}
		String[] sorting = request.getSortArray();
		if (sorting == null) {
			request.setSortArray(new String[] { idAttribute + ":asc" });
		} else if (!Arrays.asList(sorting).contains(idAttribute + ":asc")) {
			ArrayList<String> sf = new ArrayList<String>(Arrays.asList(sorting));
			sf.add(idAttribute + ":asc");
			request.setSortArray(sf.toArray(sorting));
		}
	}

	/**
	 * Executes the index process.
	 */
	public void run() {
		this.start = System.currentTimeMillis();
		try {
			indexCR(this.indexLocation, (CRConfigUtil) this.config);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		this.duration = System.currentTimeMillis() - start;
	}

}
