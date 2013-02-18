package com.gentics.cr;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Test;

import com.gentics.cr.exceptions.CRException;

public abstract class RequestProcessorTest {

	protected abstract RequestProcessor getRequestProcessor();
	
	protected abstract int getExpectedCollectionSize();
	
	protected abstract CRRequest getRequest();

	@Test
	public void someTest() throws CRException {
		CRRequest request = getRequest();
		RequestProcessor processor = getRequestProcessor();
		Collection<CRResolvableBean> beans = processor.getObjects(request);
		int expectedSize = getExpectedCollectionSize();
		assertEquals(expectedSize, beans.size());
	}

}
