package com.gentics.cr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import org.apache.commons.io.IOUtils;

import com.gentics.cr.exceptions.CRException;

public class AbstractTestHandler {
	
	public InputStream getFileAsStream(String path) throws FileNotFoundException, URISyntaxException {
		return new FileInputStream(new File(AbstractTestHandler.class.getResource("file").toURI()).getAbsolutePath() + "/" + path);
	}
	
	public byte[] getFileAsByteArray(String path) throws CRException, URISyntaxException {
		try {
			return IOUtils.toByteArray(getFileAsStream(path));
		} catch (IOException e) {
			throw new CRException(e);
		}
	}
}
