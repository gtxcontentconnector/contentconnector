package com.gentics;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public class DefaultTestConfiguration {
	public static final String baseResourcePath = "conf/gentics/";
	public static final List<String> configResourceDirectories = Arrays.asList("templates");
	public static final List<String> configResourceFiles = Arrays.asList("cache.ccf", "nodelog.yml", "templates/velocitymacros.vm");
	/**
	 * Get a temporary config directory containing the basic files needed for a working content-connector
	 * (cache.ccf, nodelog.yml, templats/velocitymacros.vm)
	 *
	 * @return the temporary directory which will be removed after shutdown
	 */
	public static File createTempConfigDirectory() throws IOException {
		// create temp dir
		final File tmpDir = Files.createTempDirectory(new File("./target").toPath(), "TestConfig").toAbsolutePath().toFile();
		// create all necessary subfolders inside of it
		for (String directory : configResourceDirectories) {
			new File(tmpDir, directory).mkdirs();
		}
		// copy the classpath ressources to the temp dir
		for (String resource : configResourceFiles) {
			InputStream inputStream = DefaultTestConfiguration.class.getResource(baseResourcePath + resource).openStream();
			Files.copy(inputStream, new File(tmpDir, resource).toPath());
		}
		// delete the temp dir on jvm shutdown
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				super.run();
				try {
					FileUtils.deleteDirectory(tmpDir);
				} catch (IOException e) {
					// do nothing on exception since we are inside of the shutdown hook already
					;
				}
			}
		});

		return tmpDir;
	}



}
