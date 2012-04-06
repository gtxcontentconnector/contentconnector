package com.gentics.cr.rest.php;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.exceptions.CRException;

public class PHPContentRepositoryTest {

	Collection<CRResolvableBean> beanCollection = new Vector<CRResolvableBean>();
	 private static final String UPDATE_TIMESTAMP_KEY = "updatetimestamp";
	
	@Before
	public void setUp() throws CRException {
		CRResolvableBean b1 = new CRResolvableBean("10002.1");
		b1.set(PHPContentRepositoryTest
				.UPDATE_TIMESTAMP_KEY, new Long(111111111L));
		beanCollection.add(b1);
		
		CRResolvableBean b2 = new CRResolvableBean("10002.2");
		b2.set(PHPContentRepositoryTest
				.UPDATE_TIMESTAMP_KEY, new Long(111211111L));
		beanCollection.add(b2);
		
		CRResolvableBean b3 = new CRResolvableBean("10002.3");
		b3.set(PHPContentRepositoryTest
				.UPDATE_TIMESTAMP_KEY, new Long(131111011L));
		beanCollection.add(b3);
		
		
		CRResolvableBean b4 = new CRResolvableBean("10002.4");
		b4.set(PHPContentRepositoryTest
				.UPDATE_TIMESTAMP_KEY, new Long(111111011L));
		beanCollection.add(b4);
		
		Collection<CRResolvableBean> children = new Vector<CRResolvableBean>();
		CRResolvableBean c1 = new CRResolvableBean("10002.5");
		c1.set(PHPContentRepositoryTest
				.UPDATE_TIMESTAMP_KEY, new Long(141111011L));
		children.add(c1);
		
		CRResolvableBean c2 = new CRResolvableBean("10002.6");
		c2.set(PHPContentRepositoryTest
				.UPDATE_TIMESTAMP_KEY, new Long(151111011L));
		c2.set("permissions", new String[]{"mar, sal, soc"});
		children.add(c2);
		b3.setChildRepository(children);
	}
	
	@Test
	public void someTest() throws CRException, UnsupportedEncodingException {
		PHPContentRepository cr = 
				new PHPContentRepository(
						new String[]{"updatetimestamp"});
		cr.addObjects(beanCollection);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		cr.toStream(stream);
		String s = stream.toString("utf-8");
		assertEquals("Coult not find youngest member.", "a:5:{s:7:\"10002.2\";a:6:{s:10:\"attributes\";a:1:{s:15:\"updatetimestamp\";s:9:\"111211111\";}s:11:\"mother_type\";s:0:\"\";s:8:\"obj_type\";s:4:\"null\";s:9:\"contentid\";s:7:\"10002.2\";s:9:\"mother_id\";s:0:\"\";s:6:\"obj_id\";s:4:\"null\";}s:7:\"10002.4\";a:6:{s:10:\"attributes\";a:1:{s:15:\"updatetimestamp\";s:9:\"111111011\";}s:11:\"mother_type\";s:0:\"\";s:8:\"obj_type\";s:4:\"null\";s:9:\"contentid\";s:7:\"10002.4\";s:9:\"mother_id\";s:0:\"\";s:6:\"obj_id\";s:4:\"null\";}s:6:\"status\";s:2:\"ok\";s:7:\"10002.3\";a:7:{s:10:\"attributes\";a:1:{s:15:\"updatetimestamp\";s:9:\"131111011\";}s:11:\"mother_type\";s:0:\"\";s:8:\"obj_type\";s:4:\"null\";s:9:\"contentid\";s:7:\"10002.3\";s:8:\"children\";a:2:{s:7:\"10002.6\";a:6:{s:10:\"attributes\";a:2:{s:11:\"permissions\";a:1:{s:1:\"0\";s:13:\"mar, sal, soc\";}s:15:\"updatetimestamp\";s:9:\"151111011\";}s:11:\"mother_type\";s:0:\"\";s:8:\"obj_type\";s:4:\"null\";s:9:\"contentid\";s:7:\"10002.6\";s:9:\"mother_id\";s:0:\"\";s:6:\"obj_id\";s:4:\"null\";}s:7:\"10002.5\";a:6:{s:10:\"attributes\";a:1:{s:15:\"updatetimestamp\";s:9:\"141111011\";}s:11:\"mother_type\";s:0:\"\";s:8:\"obj_type\";s:4:\"null\";s:9:\"contentid\";s:7:\"10002.5\";s:9:\"mother_id\";s:0:\"\";s:6:\"obj_id\";s:4:\"null\";}}s:9:\"mother_id\";s:0:\"\";s:6:\"obj_id\";s:4:\"null\";}s:7:\"10002.1\";a:6:{s:10:\"attributes\";a:1:{s:15:\"updatetimestamp\";s:9:\"111111111\";}s:11:\"mother_type\";s:0:\"\";s:8:\"obj_type\";s:4:\"null\";s:9:\"contentid\";s:7:\"10002.1\";s:9:\"mother_id\";s:0:\"\";s:6:\"obj_id\";s:4:\"null\";}}", s);
	}
	
}
