package com.gentics.cr.util.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;

/**
 * Utility Class to help with generating tar archives.
 * @author bigbear3001
 *
 */
public final class ArchiverUtil {
	/**
	 * private constructor to prevent instanziation.
	 */
	private ArchiverUtil() { }
	/**
	 * Adds a file or directory to an TarArchiveOutputStream.
	 * @param stream - stream to pack the file(s) into
	 * @param file - file or directory to pack into the stream
	 * @param name - name of the directory/file in the archive
	 * (you don't want to show your local server paths in the generated files)
	 * @throws IOException - in case a file cannot be read
	 */
	public static void addFileToTar(final TarArchiveOutputStream stream,
			final File file, final String name) throws IOException {
		TarArchiveEntry tarEntry = new TarArchiveEntry(file, name);
		stream.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
		stream.putArchiveEntry(tarEntry);
		if (file.isFile()) {
			IOUtils.copy(new FileInputStream(file), stream);
			stream.closeArchiveEntry();
		} else {
			stream.closeArchiveEntry();
			File[] children = file.listFiles();
			if (children != null) {
				for (File child : children) {
					StringBuilder childName = new StringBuilder();
					childName.append(name);
					if (!name.endsWith("/")) {
						childName.append("/");
					}
					childName.append(child.getName());
					addFileToTar(stream, child, childName.toString());
				}
			}
		}
	}
	/**
	 * generate a gnu zipped tar archive from the given file/directory.
	 * @param outputStream - output stream to save the compressed archive
	 * @param file - file/directory to put into the archive
	 * @throws IOException - in case we cannot read a file or generate the
	 * archive successfully
	 */
	public static void generateGZippedTar(final OutputStream outputStream,
			final File file) throws IOException {
		BufferedOutputStream bOut = null;
		GzipCompressorOutputStream gzOut = null;
		TarArchiveOutputStream tOut = null;
		try {
			bOut = new BufferedOutputStream(outputStream);
			gzOut = new GzipCompressorOutputStream(bOut);
			tOut = new TarArchiveOutputStream(gzOut);
			if (file.isDirectory()) {
				addFileToTar(tOut, file, "/");
			} else {
				addFileToTar(tOut, file, file.getName());
			}
		} finally {
				if (tOut != null) {
					tOut.finish();
					tOut.close();
				}
				if (gzOut != null) {
					gzOut.close();
				}
				if (bOut != null) {
					bOut.close();
				}
		}
	}
}
