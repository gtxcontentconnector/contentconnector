package com.gentics.cr.util.indexing.update.filesystem;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.StaticObjectHolderRequestProcessor;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.util.StringUtils;
import com.gentics.cr.util.file.DirectoryScanner;
import com.gentics.cr.util.file.FileTypeDetector;
import com.gentics.cr.util.indexing.DummyIndexLocation;
import com.gentics.cr.util.indexing.DummyIndexLocationFactory;

public class FileSystemUpdateJobTest {

	static CRConfigUtil config = new CRConfigUtil();
	static ConcurrentHashMap<String, CRConfigUtil> configMap = new ConcurrentHashMap<String, CRConfigUtil>();

	FileSystemUpdateJob job;

	DummyIndexLocation indexLocation;

	static File directory;

	String fileContent;

	@BeforeClass
	public static void initClass() throws URISyntaxException {
		directory = new File(FileSystemUpdateJobTest.class.getResource("outdated.file").toURI()).getParentFile();
		CRConfigUtil updateJobConfig = new CRConfigUtil();
		updateJobConfig.set("directory", directory.getPath());
		//TODO add test for ignorePubDir false
		updateJobConfig.set("ignorePubDir", "true");
		updateJobConfig.set("filter", ".*\\.file");
		config.setSubConfig("updatejob", updateJobConfig);
		CRConfigUtil requestProcessorConfig = new CRConfigUtil();
		requestProcessorConfig.set("rpclass", StaticObjectHolderRequestProcessor.class.getName());
		requestProcessorConfig.set("plinkcache", "false");
		requestProcessorConfig.set("crcontentcache", "false");
		CRConfigUtil requestProcessorsConfig = new CRConfigUtil();
		requestProcessorsConfig.setSubConfig("1", requestProcessorConfig);
		config.setSubConfig("rp", requestProcessorsConfig);
		config.set("updateattribute", "timestamp");
		configMap.put("test", config);
	}

	@Before
	public void setUp() throws FileNotFoundException {
		indexLocation = DummyIndexLocationFactory.getDummyIndexLocation(config);
		job = new FileSystemUpdateJob(config, indexLocation, configMap);
	}

	@Test
	public void testUpdateFiles() throws CRException, FileNotFoundException {
		Collection<CRResolvableBean> objects = listFileBeans(directory, ".*\\.file");
		StaticObjectHolderRequestProcessor.setObjects(objects);
		job.indexCR(indexLocation, config);
		File file = new File(directory, "outdated.file");
		String newFileContent = StringUtils.streamToString(new FileInputStream(file));
		assertEquals(
			"The contents of the file do not match the content of the RequestProcessor. Therefore the file was not updated.",
			fileContent,
			newFileContent);
	}

	public void testUpdatePages() {
		//TODO add test that write a page file (*.html)
	}

	private Collection<CRResolvableBean> listFileBeans(File directory, String filter) {
		Vector<CRResolvableBean> beans = new Vector<CRResolvableBean>();
		for (File file : DirectoryScanner.listFiles(directory, filter)) {
			CRResolvableBean bean = new CRResolvableBean();
			bean.set("filename", file.getName());
			bean.set("pub_dir", directory.toURI().relativize(file.toURI()));
			bean.set("obj_type", FileTypeDetector.getObjType(file));
			if (file.getName().equals("outdated.file")) {
				fileContent = "" + Math.random();
				bean.set("binarycontent", fileContent);
				bean.set("timestamp", new Integer((int) ((file.lastModified() / 1000) + 1000)));
			} else {
				bean.set("timestamp", new Integer((int) (file.lastModified() / 1000)));
				bean.set("binarycontent", "");
			}
			beans.add(bean);
		}
		return beans;
	}
}
