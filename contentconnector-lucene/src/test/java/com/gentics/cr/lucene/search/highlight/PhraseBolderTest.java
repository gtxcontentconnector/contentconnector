package com.gentics.cr.lucene.search.highlight;

import com.gentics.cr.configuration.GenericConfiguration;

/**
 * Test highlighting with the PhraseBolder
 * @author christopher
 *
 */
public class PhraseBolderTest extends AbstractBolderTest {

	public PhraseBolderTest(String name) {
		super(name);
	}
	@Override
	public String getBolderClass() {
		return "com.gentics.cr.lucene.search.highlight.PhraseBolder";
	}
	@Override
	public void overwriteConfig(GenericConfiguration config) {
		// TODO Auto-generated method stub
		
	}

}
