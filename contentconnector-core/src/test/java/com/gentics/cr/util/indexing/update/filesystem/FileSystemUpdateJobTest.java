package com.gentics.cr.util.indexing.update.filesystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.BeforeClass;
import org.junit.Test;

import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.StaticObjectHolderRequestProcessor;
import com.gentics.cr.conf.gentics.ConfigDirectory;
import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.util.StringUtils;
import com.gentics.cr.util.file.DirectoryScanner;
import com.gentics.cr.util.file.FileTypeDetector;
import com.gentics.cr.util.indexing.DummyIndexLocation;
import com.gentics.cr.util.indexing.DummyIndexLocationFactory;
import com.gentics.lib.log.NodeLogger;

public class FileSystemUpdateJobTest {

	private static final NodeLogger LOGGER = NodeLogger.getNodeLogger(FileSystemUpdateCheckerTest.class);
	
	static CRConfigUtil config = new CRConfigUtil();
	static ConcurrentHashMap<String, CRConfigUtil> configMap = new ConcurrentHashMap<String, CRConfigUtil>();

	FileSystemUpdateJob job;

	DummyIndexLocation indexLocation;

	static File directory;

	String fileContent;
	
	@BeforeClass
	public static void init() throws URISyntaxException {
		ConfigDirectory.useThis();
	}
	

	@Test
	public void testUpdateFiles() throws CRException, FileNotFoundException, URISyntaxException {
		directory = new File(FileSystemUpdateJobTest.class.getResource("flatdirectory" + File.separator +  "outdated.file").toURI()).getParentFile();
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
		
		indexLocation = DummyIndexLocationFactory.getDummyIndexLocation(config);
		job = new FileSystemUpdateJob(config, indexLocation, configMap);
		
		Collection<CRResolvableBean> objects = listFileBeans(directory, ".*\\.file");
		StaticObjectHolderRequestProcessor.setObjects(objects);
		job.indexCR(indexLocation, config);
		assertContentEquals(config, "outdated.file", new File(directory, "outdated.file"));

	}
	
	/**
	 * Check that the bean in the RequestProcessor has the given content.
	 * @param config - configuration to get the RequestProcessor from
	 * @param filename - filename of the bean to get from the RequestProcessor
	 * @param content - content that the bean should have
	 * @throws CRException if the RequestProcessor cannot be initialized from the configuration
	 */
	private void assertContentEquals(final CRConfigUtil config, final String filename, final String content)
			throws CRException {
		try {
			assertContentEquals(config, filename, content, null);
		} catch (FileNotFoundException e) {
			LOGGER.error("We got a FileNotFoundException without providing a file. Please Check the tests.", e);
		}
	}
	
	/**
	 * Check that the bean in the RequestProcessor has the same content as the file.
	 * @param config - configuration to get the RequestProcessor from
	 * @param filename - filename of the bean to get from the RequestProcessor
	 * @param file - 
	 * @throws CRException if the RequestProcessor cannot be initialized from the configuration
	 * @throws FileNotFoundException if the file cannot be opened with an FileInputStream 
	 */
	private void assertContentEquals(final CRConfigUtil config, final String filename, final File file)
			throws CRException, FileNotFoundException {
		assertContentEquals(config, filename, null, file);
	}
	
	/**
	 * 
	 * @param config
	 * @param filename
	 * @param content
	 * @param file
	 * @throws CRException
	 * @throws FileNotFoundException
	 */
	private void assertContentEquals(final CRConfigUtil config, final String filename, final String content, final File file)
			throws CRException, FileNotFoundException {
		String filecontent;
		if (file != null && content == null) {
			filecontent = StringUtils.streamToString(new FileInputStream(file));
		} else {
			filecontent = content;
		}
		File tmpFile = new File(filename);
		String path = tmpFile.getParent();
		String name = tmpFile.getName();
		CRResolvableBean bean;
		if (path != null && !path.equals("")) {
			bean = config.getNewRequestProcessorInstance(1).getFirstMatchingResolvable(
					new CRRequest("object.filename == '" + name + "' AND object.pub_dir == '" + path + "'"));
		} else {
			RequestProcessor rp = config.getNewRequestProcessorInstance(1);
			assertNotNull("Cannot get RequestProcessor from config", rp);
			bean = rp.getFirstMatchingResolvable(new CRRequest("object.filename == '" + name + "'"));
		}
		assertEquals("The contents of the file do not match the content of the RequestProcessor. Therefore the file was not updated.",
				bean.get("binarycontent"), filecontent);
	}

	@Test
	public void testUpdateFilesWithPubDir() throws URISyntaxException, FileNotFoundException, CRException {
		directory = new File(FileSystemUpdateJobTest.class.getResource("structureddirectory" + File.separator +  "outdated.file").toURI()).getParentFile();
		CRConfigUtil updateJobConfig = new CRConfigUtil();
		updateJobConfig.set("directory", directory.getPath());
		//TODO add test for ignorePubDir false
		updateJobConfig.set("ignorePubDir", "false");
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
		
		indexLocation = DummyIndexLocationFactory.getDummyIndexLocation(config);
		job = new FileSystemUpdateJob(config, indexLocation, configMap);
		
		Collection<CRResolvableBean> objects = listFileBeans(directory, ".*\\.file");
		objects.add(createFileBean("new.txt", "a", "10007", "bean a", getTimestamp()));
		objects.add(createFileBean("new.txt", "b", "10007", "bean b", getTimestamp()));
		StaticObjectHolderRequestProcessor.setObjects(objects);
		job.indexCR(indexLocation, config);
		//Check bean contents in the RequestProcessor
		assertContentEquals(config, "a" + File.separator + "new.txt", "bean a");
		assertContentEquals(config, "b" + File.separator + "new.txt", "bean b");
		//Check bean contents in the RequestProcessor with the filesystem
		assertContentEquals(config, "a" + File.separator + "new.txt", new File(directory, "a/new.txt"));
		assertContentEquals(config, "b" + File.separator + "new.txt", new File(directory, "b/new.txt"));
	}

	private Integer getTimestamp() {
		return (int) (new Date().getTime() / 1000L);
	}
	
	private Integer getTimestamp(File file) {
		return (int) (file.lastModified() / 1000L);
	}
	
	

	private CRResolvableBean createFileBean(String filename, String pubDir, String objecttype, String content, Integer timestamp) {
		CRResolvableBean bean = new CRResolvableBean();
		bean.set("filename", filename);
		bean.set("pub_dir", pubDir);
		bean.set("obj_type", objecttype);
		bean.set("binarycontent", content);
		bean.set("timestamp", timestamp);
		return bean;
	}

	public void testUpdatePages() {
		//TODO add test that write a page file (*.html)
	}

	private Collection<CRResolvableBean> listFileBeans(File directory, String filter) {
		Vector<CRResolvableBean> beans = new Vector<CRResolvableBean>();
		for (File file : DirectoryScanner.listFiles(directory, filter)) {
			if (file.getName().equals("outdated.file")) {
				beans.add(createFileBean(file.getName(),
						directory.toURI().relativize(file.getParentFile().toURI()).toString(),
						FileTypeDetector.getObjType(file),
						"" + Math.random(),
						getTimestamp(file) + 1000
					));
			} else {
				beans.add(createFileBean(file.getName(),
						directory.toURI().relativize(file.getParentFile().toURI()).toString(),
						FileTypeDetector.getObjType(file),
						"",
						getTimestamp(file)
					));
			}
		}
		return beans;
	}
}
