package com.gentics.cr.lucene.indexer.index;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockFactory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SimpleFSLockFactory;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.lucene.AbstractLuceneTest;
import com.gentics.cr.util.CRUtil;

public class LuceneDirectoryFactoryTest extends AbstractLuceneTest {
	
	

	public LuceneDirectoryFactoryTest(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public void testDirectoryCaching() {

		Directory d1 = LuceneDirectoryFactory.getDirectory("RAM_1");
		Directory d2 = LuceneDirectoryFactory.getDirectory("RAM_1");
		Directory d3 = LuceneDirectoryFactory.getDirectory("RAM_2");

		assertEquals("Directories are not the same.", d1, d2);
		assertEquals("Directories should not be the same.", d1 != d3, true);
	}

	public void testRAMDirectory() {
		Directory ram = LuceneDirectoryFactory.getDirectory("RAM_1");
		assertEquals("Directory should be a RAMDirectory", ram instanceof RAMDirectory, true);
	}

	public void testFSDirectory() throws IOException {
		File tmp = CRUtil.createTempDir();

		Directory fs = LuceneDirectoryFactory.getDirectory(tmp.getAbsolutePath());

		assertEquals("Directory should be a FSDirectory", fs instanceof FSDirectory, true);

		tmp = ((FSDirectory) fs).getDirectory();
		fs.close();
		tmp.delete();
	}
	
	public void testFSDirectoryWithLockFactory() throws IOException {
		
		CRConfig config = new CRConfigUtil();
		config.set("lockFactoryClass", "org.apache.lucene.store.SimpleFSLockFactory");
		
		File tmp = CRUtil.createTempDir();

		Directory fs = LuceneDirectoryFactory.getDirectory(tmp.getAbsolutePath(), config);

		assertEquals("Directory should be a FSDirectory", fs instanceof FSDirectory, true);

		tmp = ((FSDirectory) fs).getDirectory();
		
		LockFactory lf = fs.getLockFactory();
		
		assertEquals("LockFactory should be a SimpleFSLockFactory", lf instanceof SimpleFSLockFactory, true);
		
		fs.close();
		tmp.delete();
	}

}
