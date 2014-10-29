package com.gentics.cr.lucene.search.highlight;

import com.gentics.cr.configuration.GenericConfiguration;

/**
 * Test highlighting with the VectorBolder
 * @author christopher
 *
 */
public class VectorBolderTest extends AbstractBolderTest {

	public VectorBolderTest(String name) {
		super(name);
	}
	@Override
	public String getBolderClass() {
		return "com.gentics.cr.lucene.search.highlight.VectorBolder";
	}
	@Override
	public void overwriteConfig(GenericConfiguration config) {
		// TODO Auto-generated method stub
		
	}

}
