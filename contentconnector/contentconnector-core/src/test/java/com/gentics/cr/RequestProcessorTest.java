package com.gentics.cr;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
