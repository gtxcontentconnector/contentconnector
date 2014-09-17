package com.gentics.cr.lucene.indexer.index.removeattr;

import java.util.ArrayList;
import java.util.Collection;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.exceptions.CRException;

/**
 * Dummy Request Processor that will return a fixed list of objects.
 * Is used by {@link RemoveAttributeTest}
 */
public class RemoveAttributeRequestProcessor extends RequestProcessor {
	/**
	 * Static setting, whether attribute "attr" shall be added with non-null values to the objects or not
	 */
	public static boolean withAttribute = true;

	/**
	 * Update timestamp of the returned objects
	 */
	public static long timestamp = 1;

	/**
	 * Create an instance
	 * @param config config
	 * @throws CRException
	 */
	public RemoveAttributeRequestProcessor(CRConfig config) throws CRException {
		super(config);
	}

	@Override
	public Collection<CRResolvableBean> getObjects(CRRequest request,
			boolean doNavigation) throws CRException {
		Collection<CRResolvableBean> result = new ArrayList<CRResolvableBean>();
		
		result.add(createBean("10007.1", "content content test", "value1"));
		result.add(createBean("10007.2", "content content test", "value1"));
		result.add(createBean("10007.3", "content content test", "value1"));
		result.add(createBean("10007.4", "content content test", "value1"));
		result.add(createBean("10007.5", "content content test", "value1"));
		result.add(createBean("10007.6", "content content test", "value2"));
		result.add(createBean("10007.7", "content content test", "value2"));
		result.add(createBean("10007.8", "content content test", "value2"));
		result.add(createBean("10007.9", "content content test", "value2"));
		result.add(createBean("10007.10", "content content test", "value2"));
		
		return result;
	}

	/**
	 * Create a single bean
	 * @param contentid content id
	 * @param content content
	 * @param attribute attribute value
	 * @return bean
	 */
	private CRResolvableBean createBean(String contentid, String content, String attribute) {
		CRResolvableBean bean = new CRResolvableBean(contentid);
		bean.set("content",content);
		if (withAttribute) {
			bean.set("attr", attribute);
		}
		bean.set("updatetimestamp", timestamp);
		return bean;
	}

	@Override
	public void finalize() {
	}
}
