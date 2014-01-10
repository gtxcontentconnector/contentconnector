package com.gentics.cr.testutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import org.apache.commons.io.IOUtils;


public class AbstractTestHandler {
	
	public InputStream getFileAsStream(String path) throws FileNotFoundException, URISyntaxException {
		return new FileInputStream(new File(getClass().getResource("file").toURI()).getAbsolutePath() + "/" + path);
	}
	
	public byte[] getFileAsByteArray(String path) throws Exception, URISyntaxException {
		try {
			return IOUtils.toByteArray(getFileAsStream(path));
		} catch (IOException e) {
			throw new Exception(e);
		}
	}
}
