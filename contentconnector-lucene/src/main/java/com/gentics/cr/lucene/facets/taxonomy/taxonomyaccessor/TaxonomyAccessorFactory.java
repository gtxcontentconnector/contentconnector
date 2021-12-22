package com.gentics.cr.lucene.facets.taxonomy.taxonomyaccessor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.LogManager;

import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.Directory;

import com.gentics.cr.CRConfig;
import com.gentics.cr.lucene.facets.taxonomy.TaxonomyMapping;
import com.gentics.lib.log.NodeLogger;

/**
 * An TaxonomyAccessorFactory allows the sharing of {@link TaxonomyAccessor} 
 * across threads.
 * TODO: fix shared access to instances of {@link TaxonomyAccessor}
 * $Date$
 * @version $Revision$
 * @author Sebastian Vogel <s.vogel@gentics.com>
 */
public class TaxonomyAccessorFactory {

	private static final OpenMode DEFAULT_OPEN_MODE = OpenMode.CREATE_OR_APPEND;

	/**
	 * Log4j logger for debug and error messages.
	 */
	private static NodeLogger logger = NodeLogger
			.getNodeLogger(TaxonomyAccessorFactory.class);

	/**
	 * Holds an single instance of {@link TaxonomyAccessorFactory} to give it to
	 * others who want to read a lucene taxonomy.
	 */
	private static final TaxonomyAccessorFactory TAXONOMYACCESSORFACTORY = new TaxonomyAccessorFactory();

	private ConcurrentHashMap<Directory, TaxonomyAccessor> taxonomyAccessors =
			new ConcurrentHashMap<Directory, TaxonomyAccessor>();

	private Vector<TaxonomyAccessorToken> consumer = new Vector<TaxonomyAccessorToken>();

	/**
	 * boolean mark for indicating {@link TaxonomyAccessorFactory} was closed
	 * before.
	 */
	private static boolean wasClosed = false;

	static {
		LogManager manager = LogManager.getLogManager();
		InputStream is = ClassLoader
				.getSystemResourceAsStream("logger.properties");

		if (is != null) {
			try {
				manager.readConfiguration(is);
			} catch (SecurityException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	public static TaxonomyAccessorFactory getInstance() {
		return TAXONOMYACCESSORFACTORY;
	}

	private TaxonomyAccessorFactory() {
		// prevent instantiation.
	}

	public synchronized TaxonomyAccessorToken registerConsumer() {
		TaxonomyAccessorToken token = new TaxonomyAccessorToken();
		this.consumer.add(token);
		return token;
	}

	public synchronized void releaseConsumer(TaxonomyAccessorToken token) {
		this.consumer.remove(token);
		if (this.consumer.size() == 0) {
			close();
		}
	}

	/**
	 * Closes the open TaxonomyAccessor and releases any open resources.
	 */
	private synchronized void close() {
		if (!wasClosed) {
			for (TaxonomyAccessor accessor : taxonomyAccessors.values()) {
				accessor.close();
			}
			taxonomyAccessors.clear();
			wasClosed = true;
			if (logger.isDebugEnabled()) {
				try {
					throw new Exception(
							"Closing index accessory factory lucene search is now disabled.");
				} catch (Exception e) {
					logger.debug("IndexAccessorFactory is now closed.", e);
				}
			}
		}
	}

	public void createAccessor(final CRConfig config, Directory dir) throws IOException {
		createAccessor(config, dir, null);
	}

	public void createAccessor(final CRConfig config, Directory dir, OpenMode openMode)
			throws IOException {
		
		if (openMode == null) {
			openMode = DEFAULT_OPEN_MODE;
		}
		
		TaxonomyAccessor accessor = new DefaultTaxonomyAccessor(openMode, dir);
		accessor.addTaxonomyMappings(TaxonomyMapping.mapTaxonomies(config));
		accessor.open();
		
		TaxonomyAccessor existingAccessor = taxonomyAccessors.putIfAbsent(dir, accessor);
		if (existingAccessor != null) {
			throw new IllegalStateException("TaxonomyAccessor already exists: " + dir);
		}
	}

	/**
	 * Get an {@link DefaultTaxonomyAccessor} for the specified {@link Directory}.
	 * 
	 * @param dir
	 *            {@link Directory} to get the {@link DefaultTaxonomyAccessor} for.
	 * @return {@link DefaultTaxonomyAccessor} for the {@link Directory}.
	 */
	public TaxonomyAccessor getAccessor(Directory dir) {
		if (wasClosed) {
			throw new AlreadyClosedException(
					"TaxonomyAccessorFactory was already closed"
							+ ". Maybe there is a shutdown in progress.");
		}
		
		TaxonomyAccessor accessor = taxonomyAccessors.get(dir);
		if (accessor == null) {
			throw new IllegalStateException("Requested TaxonomyAccessor does not exist");
		}
		return accessor;
	}

	public boolean hasAccessor(Directory dir) {
		return taxonomyAccessors.containsKey(dir);
	}

}
