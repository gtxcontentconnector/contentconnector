package com.gentics.cr.lucene.indexer.index;

import java.util.ArrayList;
import java.util.Collection;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.exceptions.CRException;

public class DummyIndextestRequestProcessor extends RequestProcessor {

	public DummyIndextestRequestProcessor(CRConfig config) throws CRException {
		super(config);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Collection<CRResolvableBean> getObjects(CRRequest request,
			boolean doNavigation) throws CRException {
		Collection<CRResolvableBean> result = new ArrayList<CRResolvableBean>();
		
		result.add(createBean("10007.1", "content content test", "cat1", "tac1", System.currentTimeMillis()));
		result.add(createBean("10007.2", "content content test", "cat1", "tac1", System.currentTimeMillis()));
		result.add(createBean("10007.3", "content content test", "cat1", "tac2", System.currentTimeMillis()));
		result.add(createBean("10007.4", "content content test", "cat1", "tac2", System.currentTimeMillis()));
		result.add(createBean("10007.5", "content content test", "cat1", "tac3", System.currentTimeMillis()));
		result.add(createBean("10007.6", "content content test", "cat2", "tac3", System.currentTimeMillis()));
		result.add(createBean("10007.7", "content content test", "cat2", "tac4", System.currentTimeMillis()));
		result.add(createBean("10007.8", "content content test", "cat2", "tac4", System.currentTimeMillis()));
		result.add(createBean("10007.9", "content content test", "cat2", "tac5", System.currentTimeMillis()));
		result.add(createBean("10007.10", "content content test", "cat2", "tac5", System.currentTimeMillis()));
		
		return result;
	}
	
	private CRResolvableBean createBean(String contentid, String content, String category, String category2, long update) {
		CRResolvableBean bean = new CRResolvableBean(contentid);
		bean.set("content",content);
		bean.set("category", category);
		bean.set("category2", category2);
		bean.set("updatetimestamp", update);
		return bean;
	}

	@Override
	public void finalize() {
		// TODO Auto-generated method stub

	}

}
