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

public class YoungestTimestampContentRepositoryTest {

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
		assertEquals("Coult not find youngest member.", "131111011", s);
	}
	
}
