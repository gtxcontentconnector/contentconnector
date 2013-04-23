package com.gentics.cr.lucene.indexer.index;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;

import com.gentics.cr.CRConfig;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.facets.taxonomy.taxonomyaccessor.TaxonomyAccessor;
import com.gentics.cr.lucene.indexaccessor.IndexAccessor;
import com.gentics.cr.lucene.indexaccessor.IndexAccessorFactory;

/**
 * Default MultiIndexAccessor implementation.
 * 
 * Last changed: $Date: 2009-09-02 17:57:48 +0200 (Mi, 02 Sep 2009) $
 * @version $Revision: 180 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class LuceneMultiIndexLocation extends LuceneIndexLocation {

	ConcurrentHashMap<String, Directory> dirs = new ConcurrentHashMap<String, Directory>();

	/**
	 * Timestamp to store the lastmodified value of the reopen file.
	 */
	private long lastmodifiedStored = 0;

	/**
	 * Create a new Instance of LuceneMultiIndexLocation this IndexLocation can search over multiple index directories.
	 * It is not able to write to the index.
	 * @param config
	 */
	public LuceneMultiIndexLocation(final CRConfig config) {
		super(config);
		GenericConfiguration locs = (GenericConfiguration) config.get(INDEX_LOCATIONS_KEY);
		if (locs != null) {
			Map<String, GenericConfiguration> locationmap = locs.getSortedSubconfigs();
			if (locationmap != null) {
				for (GenericConfiguration locconf : locationmap.values()) {
					String path = locconf.getString(INDEX_PATH_KEY);
					if (path != null && !"".equals(path)) {
						dirs.put(path, loadDirectory(path, config));
					}
				}
			}
		}

	}

	private Directory loadDirectory(final String indexLocation, final CRConfig config) {
		Directory dir = createDirectory(indexLocation, config);

		//Create index accessor
		IndexAccessorFactory factory = IndexAccessorFactory.getInstance();
		if (!factory.hasAccessor(dir)) {
			try {
				factory.createAccessor(dir, getConfiguredAnalyzer());
			} catch (IOException ex) {
				log.fatal("COULD NOT CREATE INDEX ACCESSOR" + ex.getMessage());
			}
		} else {
			log.debug("Accessor already present. we will not create a new one.");
		}
		return dir;
	}

	@Override
	protected IndexAccessor getAccessorInstance() {
		IndexAccessorFactory factory = IndexAccessorFactory.getInstance();
		return factory.getMultiIndexAccessor(this.dirs.values().toArray(new Directory[] {}));
	}

	@Override
	protected IndexAccessor getAccessorInstance(final boolean reopenClosedFactory) {
		return getAccessorInstance();
	}

	@Override
	protected Directory[] getDirectories() {
		return this.dirs.values().toArray(new Directory[] {});
	}

	@Override
	public int getDocCount() {
		IndexAccessor indexAccessor = this.getAccessor();
		IndexReader reader = null;
		int count = 0;
		try {
			reader = indexAccessor.getReader(false);
			count = reader.numDocs();
		} catch (IOException ex) {
			log.error("IOX happened during test of index. " + ex.getMessage());
		} finally {
			indexAccessor.release(reader, false);
		}

		return count;
	}

	private String getReopenFilename(final String dir) {
		return dir + "/" + REOPEN_FILENAME;
	}

	@Override
	public final void createReopenFile() {
		boolean writeReopenFile = config.getBoolean("writereopenfile");

		if (writeReopenFile) {
			for (String dir : this.dirs.keySet()) {
				try {
					String filename = this.getReopenFilename(dir);
					log.debug("Writing reopen to " + filename);
					File reopenFile = new File(filename);
					FileUtils.touch(reopenFile);
				} catch (IOException e) {
					log.warn("Cannot create reopen file! " + e);
				}
			}
		}
	}

	@Override
	public boolean reopenCheck(IndexAccessor indexAccessor, TaxonomyAccessor taxonomyAccessor) {
		boolean reopened = false;
		if (reopencheck) {
			try {
				boolean reopen = false;
				for (String dir : dirs.keySet()) {
					log.debug("Check for reopen file at " + this.getReopenFilename(dir));
					File reopenFile = new File(this.getReopenFilename(dir));
					if (reopenFile.exists()) {
						if (reopencheckTimestamp) {
							long lastmodified = reopenFile.lastModified();
							if (lastmodified != lastmodifiedStored) {
								lastmodifiedStored = lastmodified;
								indexAccessor.reopen();
								reopened = true;
								log.debug("Reopen index because reopen file has changed");
							}
						} else {
							reopenFile.delete();
							reopen = true;
							log.debug("Reopen index because of simple reopen check");
						}
					}
				}
				if (reopen) {
					indexAccessor.reopen();
					reopened = true;
					log.debug("Reopened index.");
				}
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);
			}
		}
		return reopened;
	}

	@Override
	public final long indexSize() {
		long directorySize = 0;
		//TODO add caching
		for (String dir : dirs.keySet()) {
			directorySize += FileUtils.sizeOfDirectory(new File(dir));
		}
		return directorySize;
	}

	@Override
	public final Date lastModified() {
		long lastModified = 0;
		for (String dir : dirs.keySet()) {
			File reopenFile = new File(this.getReopenFilename(dir));
			if (reopenFile.exists()) {
				long reopenLastModified = reopenFile.lastModified();
				if (reopenLastModified > lastModified) {
					lastModified = reopenLastModified;
				}
			} else {
				File directory = new File(dir);
				if (directory.exists()) {
					long directoryLastModified = directory.lastModified();
					if (directoryLastModified > lastModified) {
						lastModified = directoryLastModified;
					}
				}
			}
		}
		return new Date(lastModified);
	}

	@Override
	public boolean isOptimized() {
		boolean ret = false;
		IndexAccessor indexAccessor = this.getAccessor();
		IndexReader reader = null;
		try {
			reader = indexAccessor.getReader(false);
			ret = reader.isOptimized();
		} catch (IOException ex) {
			log.error("IOException happened during test of index. ", ex);
		} finally {
			indexAccessor.release(reader, false);
		}

		return ret;
	}

	@Override
	public boolean isLocked() {
		boolean locked = false;
		IndexAccessor indexAccessor = this.getAccessor();
		locked = indexAccessor.isLocked();
		return locked;
	}

	/**
	* TODO: first implementation of hashCode Method in the {@link com.gentics.cr.lucene.indexer.index.LuceneMultiIndexLocation} 
	* - not tested yet!
	*/
	@Override
	public int hashCode() {
		int hash = 17;
		for (Entry<String, Directory> entry : this.dirs.entrySet()) {
			hash = 31 * hash + entry.getValue().getLockID().hashCode();
		}
		return hash;
	}

	@Override
	protected TaxonomyAccessor getTaxonomyAccessorInstance() {
		throw new UnsupportedOperationException("Method not implemented yet");
	}

}
