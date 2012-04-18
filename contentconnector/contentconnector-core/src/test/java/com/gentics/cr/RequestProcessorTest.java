package com.gentics.cr;

import java.util.Collection;

import org.junit.Test;

import com.gentics.cr.exceptions.CRException;

public abstract class RequestProcessorTest {

	protected abstract RequestProcessor getRequestProcessor();

	@Test
	public void someTest() throws CRException {
		CRRequest request = new CRRequest();
		RequestProcessor processor = getRequestProcessor();
		Collection<CRResolvableBean> beans = processor.getObjects(request);
	}

}
