package com.gentics.cr.lucene.indexer.index;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRConfigUtil;
import com.gentics.cr.configuration.GenericConfiguration;
import com.gentics.cr.lucene.AbstractLuceneTest;
import com.gentics.cr.lucene.indexaccessor.IndexAccessor;

public class LuceneIndexLocationTest extends AbstractLuceneTest {

	public LuceneIndexLocationTest(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	
	
	
	CRConfig singleConfig1;
	CRConfig singleConfig2;
	
	CRConfig singleConfig3;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		
		
		GenericConfiguration sc = new GenericConfiguration();
		sc.set("indexLocations.1.path", "RAM_1");
		sc.set("indexLocationClass", "com.gentics.cr.lucene.indexer.index.LuceneSingleIndexLocation");
		
		singleConfig1 = new CRConfigUtil(sc, "sc1");
		
		GenericConfiguration sc2 = new GenericConfiguration();
		sc2.set("indexLocations.2.path", "RAM_1");
		sc2.set("indexLocationClass", "com.gentics.cr.lucene.indexer.index.LuceneSingleIndexLocation");
		
		singleConfig2 = new CRConfigUtil(sc2, "sc2");
		
		GenericConfiguration sc3 = new GenericConfiguration();
		sc3.set("indexLocations.3.path", "RAM_3");
		sc3.set("indexLocationClass", "com.gentics.cr.lucene.indexer.index.LuceneSingleIndexLocation");
		
		singleConfig3 = new CRConfigUtil(sc3, "sc3");
	}
	
	
	public void testLuceneSingleIndexLocation(){
		LuceneIndexLocation singleLoc1 = LuceneIndexLocation.getIndexLocation(singleConfig1);
		IndexAccessor ia1 = singleLoc1.getAccessor();
		
		LuceneIndexLocation singleLoc2 = LuceneIndexLocation.getIndexLocation(singleConfig2);
		IndexAccessor ia2 = singleLoc2.getAccessor();
		
		LuceneIndexLocation singleLoc3 = LuceneIndexLocation.getIndexLocation(singleConfig3);
		IndexAccessor ia3 = singleLoc3.getAccessor();
		
		LuceneIndexLocation singleLoc4 = LuceneIndexLocation.getIndexLocation(singleConfig3);
		
		assertEquals("The accessors are not the same.", ia1, ia2);
		assertEquals("The accessors of different locations are the same.", ia1 == ia3, false);
		assertEquals("The locations are not the same.", singleLoc3, singleLoc4);
	}

}
