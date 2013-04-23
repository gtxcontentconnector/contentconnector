package com.gentics.cr.file;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

/**
 * the FileSystemChecker checks various files for updates.
 * @author bigbear3001
 *
 */
public class FileSystemChecker {

	/**
	 * paths to scan periodically for changes.
	 */
	private Collection<ResolvableFileBean> monitoredPaths = null;

	/**
	 * Iterator holding the current index iterator.
	 */
	private Iterator<ResolvableFileBean> indexRun = null;

	/**
	 * Array with {@link FileSystemCheckerJob}s working for this
	 * {@link FileSystemChecker}.
	 */
	private FileSystemCheckerJob[] jobs = null;

	/**
	 * Default number of jobs if nothing is configured.
	 */
	private static final int DEFAULT_JOB_COUNT = 1;

	/**
	 * Create a new Instance of the FileSystemChecker. Paths can be added later
	 * with the {@link #addPathToMonitoring(ResolvableFileBean)} method. The
	 * indexing starts automatically.
	 */
	public FileSystemChecker() {
		this(true);
	}

	/**
	 * Create a new Instance of the FileSystemChecker. Paths can be added later
	 * with the {@link #addPathToMonitoring(ResolvableFileBean)} method.
	 * @param autostart if the indexing should start automatically. Otherwise it
	 * must be triggered with the {@link #start()} method.
	 */
	public FileSystemChecker(final boolean autostart) {
		this(new Vector<ResolvableFileBean>(0), autostart);
	}

	/**
	 * Create a new Instance of the FileSystemChecker with a list of paths. The
	 * Threads to scan periodically for changes are automatically started.
	 * @param pathsToMonitor Collection of paths to monitor for changes
	 */
	public FileSystemChecker(final Collection<ResolvableFileBean> pathsToMonitor) {
		this(pathsToMonitor, true);
	}

	/**
	 * Create a new Instance of the FileSystemChecker with a list of paths.
	 * @param pathsToMonitor Collection of paths to monitor for changes
	 * @param autostart if the indexing should start automatically. Otherwise it
	 * must be triggered with the {@link #start()} method.
	 */
	public FileSystemChecker(final Collection<ResolvableFileBean> pathsToMonitor, final boolean autostart) {
		this.monitoredPaths = pathsToMonitor;
		if (autostart) {
			start();
		}
	}

	/**
	 * define an additional path to be monitored.
	 * @param monitoredPath path to be added.
	 */
	//TODO add counters to detect if a path is used by multiple RequestProcessors
	public final void addPathToMonitoring(final ResolvableFileBean monitoredPath) {
		monitoredPaths.add(monitoredPath);
	}

	/**
	 * remove the path from monitoring.
	 * @param monitoredPath path to be removed from monitoring.
	 */
	//TODO add counters to detect if a path is used by multiple RequestProcessors
	public final void removePathFromMonitoring(final ResolvableFileBean monitoredPath) {
		monitoredPaths.remove(monitoredPath);
	}

	/**
	 * get a list of paths monitored by the {@link FileSystemChecker}.
	 * @return {@link Collection} with {@link ResolvableFileBean} representing the
	 * paths monitored by the {@link FileSystemChecker}.
	 */
	@SuppressWarnings("unused")
	private Collection<ResolvableFileBean> getMonitoredPaths() {
		if (monitoredPaths != null) {
			return monitoredPaths;
		}
		return null;
	}

	/**
	 * Get the next root element in the indexing tree.
	 * @return {@link ResolvableFileBean} for the root element, <code>null</code>
	 * in case there are no monitored Paths in the index
	 */
	protected final ResolvableFileBean getNextElementToIndex() {
		if (indexRun == null || !indexRun.hasNext()) {
			indexRun = new Vector<ResolvableFileBean>(monitoredPaths).iterator();
		}
		if (indexRun.hasNext()) {
			return indexRun.next();
		} else {
			return null;
		}
	}

	/**
	 * Start the update checking of the monitored paths.
	 */
	public final void start() {
		stop();
		int numJobs = DEFAULT_JOB_COUNT;
		if (jobs == null) {
			jobs = new FileSystemCheckerJob[numJobs];
		}
		for (int i = 0; i < numJobs; i++) {
			jobs[i] = new FileSystemCheckerJob(this, "FileSystemCheckerJob#" + i);
			jobs[i].start();
		}
	}

	/**
	 * Stop the update checking of the monitored paths.
	 */
	public final void stop() {
		if (jobs != null) {
			for (FileSystemCheckerJob job : jobs) {
				job.stopUpdateCheck();
			}
		}
	}

}
