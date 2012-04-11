package com.gentics.cr.util.file;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class ArchiveUtilTest {
	@Test
	public void testCompression() throws URISyntaxException, IOException {
		File textFile = new File(getClass().getResource("ArchiveUtil.txt").toURI());
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ArchiverUtil.generateGZippedTar(stream, textFile);
		byte[] result = stream.toByteArray();
		long unCompressedFileSize = FileUtils.sizeOf(textFile);
		assertTrue("the compressed file is larger (" + result.length + ") than the original file ("
				+ unCompressedFileSize + ")", unCompressedFileSize > result.length);
	}

	@Test
	public void testCompressionFullDirectory() throws IOException, URISyntaxException {
		File directory = new File(getClass().getResource("").toURI());
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ArchiverUtil.generateGZippedTar(stream, directory);
		byte[] result = stream.toByteArray();
		long unCompressedFileSize = FileUtils.sizeOfDirectory(directory);
		assertTrue("the compressed file is larger (" + result.length + ") than the original file ("
				+ unCompressedFileSize + ")", unCompressedFileSize > result.length);
	}
}
