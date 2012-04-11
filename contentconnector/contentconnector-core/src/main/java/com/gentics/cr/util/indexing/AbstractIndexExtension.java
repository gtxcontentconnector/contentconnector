package com.gentics.cr.util.indexing;

import com.gentics.cr.CRConfig;

/**
 * Use this abstract class for implementations of {@link IndexExtension}
 * Implementations should always provide a constructor with the following method signature
 * <pre>public MyIndexExtension(CRConfig config, IndexLocation callingLocation)</pre>
 *  
 * @author Sebastian Vogel <s.vogel@gentics.com> * 
 */
public abstract class AbstractIndexExtension implements IndexExtension {

	protected CRConfig config;
	protected IndexLocation callingIndexLocation;

	/**
	 * The default constructor for the AbstractIndexExtension every
	 * implementation of this class should provide a Constructor with this
	 * method signature
	 * 
	 * @param config the config for this extension
	 * @param callingLocation the {@link IndexLocation} which uses this Extension
	 */
	public AbstractIndexExtension(CRConfig config, IndexLocation callingLocation) {
		this.config = config;
		this.callingIndexLocation = callingLocation;
	}

	/*
	 * (non-Javadoc)
	 * @see com.gentics.cr.util.indexing.IndexExtension#stop()
	 */
	public abstract void stop();

	/*
	 * (non-Javadoc)
	 * @see com.gentics.cr.util.indexing.IndexExtension#addJob(java.lang.String)
	 */
	public abstract void addJob(String name) throws NoSuchMethodException;

	/*
	 * (non-Javadoc)
	 * @see com.gentics.cr.util.indexing.IndexExtension#addJob(java.lang.String,
	 * com.gentics.cr.util.indexing.IndexLocation)
	 */
	public abstract void addJob(String name, IndexLocation indexLocation) throws NoSuchMethodException;

	/*
	 * (non-Javadoc)
	 * @see com.gentics.cr.util.indexing.IndexExtension#getJobs()
	 */
	public abstract String[] getJobs();
}
