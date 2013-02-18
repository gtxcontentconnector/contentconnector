package com.gentics.cr;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.gentics.cr.exceptions.CRException;

public class AbstractTestHandler {
	
	public InputStream getFileAsStream(String path) throws FileNotFoundException {
		return new FileInputStream(AbstractTestHandler.class.getResource("file").getPath() + "/" + path);
	}
	
	public byte[] getFileAsByteArray(String path) throws CRException {
		try {
			return IOUtils.toByteArray(getFileAsStream(path));
		} catch (IOException e) {
			throw new CRException(e);
		}
	}
}
