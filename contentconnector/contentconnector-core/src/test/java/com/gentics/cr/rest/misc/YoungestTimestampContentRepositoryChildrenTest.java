package com.gentics.cr.rest.misc;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.exceptions.CRException;

public class YoungestTimestampContentRepositoryChildrenTest {

	Collection<CRResolvableBean> beanCollection = new Vector<CRResolvableBean>();
	
	
	@Before
	public void setUp() throws CRException {
		CRResolvableBean b1 = new CRResolvableBean();
		b1.set(YoungestTimestampContentRepository
				.UPDATE_TIMESTAMP_KEY, new Long(111111111L));
		beanCollection.add(b1);
		
		CRResolvableBean b2 = new CRResolvableBean();
		b2.set(YoungestTimestampContentRepository
				.UPDATE_TIMESTAMP_KEY, new Long(111211111L));
		beanCollection.add(b2);
		
		CRResolvableBean b3 = new CRResolvableBean();
		b3.set(YoungestTimestampContentRepository
				.UPDATE_TIMESTAMP_KEY, new Long(131111011L));
		beanCollection.add(b3);
		
		
		CRResolvableBean b4 = new CRResolvableBean();
		b4.set(YoungestTimestampContentRepository
				.UPDATE_TIMESTAMP_KEY, new Long(111111011L));
		beanCollection.add(b4);
		
		Collection<CRResolvableBean> children = new Vector<CRResolvableBean>();
		CRResolvableBean c1 = new CRResolvableBean();
		c1.set(YoungestTimestampContentRepository
				.UPDATE_TIMESTAMP_KEY, new Long(141111011L));
		children.add(c1);
		
		CRResolvableBean c2 = new CRResolvableBean();
		c2.set(YoungestTimestampContentRepository
				.UPDATE_TIMESTAMP_KEY, new Long(151111011L));
		children.add(c2);
		b3.setChildRepository(children);
	}
	
	@Test
	public void someTest() throws CRException, UnsupportedEncodingException {
		YoungestTimestampContentRepository cr = 
				new YoungestTimestampContentRepository(
						new String[]{"updatetimestamp"});
		cr.addObjects(beanCollection);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		cr.toStream(stream);
		String s = stream.toString("utf-8");
		assertEquals("Coult not find youngest member.", "151111011", s);
	}
	
}
