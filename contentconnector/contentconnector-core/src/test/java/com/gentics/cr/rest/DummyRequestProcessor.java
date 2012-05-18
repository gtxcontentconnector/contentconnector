package com.gentics.cr.rest;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.exceptions.CRException;

public class DummyRequestProcessor extends RequestProcessor {

	private static ConcurrentHashMap<String, CRResolvableBean> beans = new ConcurrentHashMap<String, CRResolvableBean>();

	public static void addBean(CRResolvableBean bean) {
		beans.put(bean.getContentid(), bean);
	}

	public DummyRequestProcessor(CRConfig config) throws CRException {
		super(config);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Collection<CRResolvableBean> getObjects(CRRequest request, boolean doNavigation) throws CRException {
		String contentid = request.getContentid();
		return Collections.singleton(beans.get(contentid));
	}

	@Override
	public void finalize() {
		// TODO Auto-generated method stub

	}

}
