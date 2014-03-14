package com.gentics.cr.file;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

public class ResolvableFileBeanTest {

	@Test
	public void testNullFileMimetype() throws IOException {
		ResolvableFileBean bean = new ResolvableFileBean(null);
		assertEquals(ResolvableFileBean.UNKNOWN_MIMETYPE, bean.getMimetype());
		assertEquals(ResolvableFileBean.UNKNOWN_MIMETYPE, bean.getDetectedMimetype());
		assertNull(bean.getFile());
		assertNull(bean.getFileName());
		assertNull(bean.getPubDir());
		assertNull(bean.getBinaryContent());
		assertNull(bean.getContentid());
	}

	@Test
	public void testCurrentDirectory() throws URISyntaxException, IOException {
		File file = new File(this.getClass().getResource("Main Page.pdf").toURI());
		ResolvableFileBean bean = new ResolvableFileBean(file);
		//assertEquals("application/octet-stream", bean.getMimetype());
		assertEquals("application/pdf", bean.getDetectedMimetype());
		
		assertNotNull(bean.getFile());

		assertNotNull(bean.getFileName());
		assertEquals(bean.get("filename"), bean.getFileName());
		
		assertNotNull(bean.getPubDir());
		assertEquals(bean.get("pub_dir"), bean.getPubDir());
		
		assertNotNull(bean.getBinaryContent());
		assertNotNull(bean.get("binarycontent"));
		
		assertNotNull("10008", bean.get("obj_type"));
		assertEquals(bean.get("obj_type"), bean.getObj_type());

		assertNotNull(bean.getContentid());
		assertEquals(bean.get("contentid"), bean.getContentid());
	}
}
