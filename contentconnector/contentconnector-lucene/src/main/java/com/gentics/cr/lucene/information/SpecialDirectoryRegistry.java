package com.gentics.cr.lucene.information;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.lucene.store.Directory;

/**
 * This is a registry class for special indices such as the DYM or autocomplete
 * index.
 * @author Christopher
 *
 */
public final class SpecialDirectoryRegistry {
	/**
	 * Singleton instance.
	 */
	private static SpecialDirectoryRegistry instance;
	/**
	 * Directory map.
	 */
	private ConcurrentHashMap<String, SpecialDirectoryInformationEntry> dirMap = new ConcurrentHashMap<String, SpecialDirectoryInformationEntry>();

	/**
	 * Prevent instantiation.
	 */
	private SpecialDirectoryRegistry() {
	}

	/**
	 * Get the singleton instance.
	 * @return singleton instance.
	 */
	public static SpecialDirectoryRegistry getInstance() {
		if (instance == null) {
			instance = new SpecialDirectoryRegistry();
		}
		return instance;
	}

	/**
	 * Register a directory.
	 * @param dir directory to register.
	 */
	public void register(final Directory dir) {
		dirMap.put(
			SpecialDirectoryInformationEntry.createDirectoryIdentifyer(dir),
			new SpecialDirectoryInformationEntry(dir));
	}

	/**
	 * Unregister the directory.
	 * @param dir directory to unregister.
	 */
	public void unregister(final Directory dir) {
		dirMap.remove(SpecialDirectoryInformationEntry.createDirectoryIdentifyer(dir));
	}

	/**
	 * Get special directory entries.
	 * @return entries.
	 */
	public Collection<SpecialDirectoryInformationEntry> getSpecialDirectories() {
		return dirMap.values();
	}

}
