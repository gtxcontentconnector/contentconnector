package com.gentics.cr.lucene.search.highlight;

import com.gentics.cr.configuration.GenericConfiguration;

/**
 * Test highlighting with the WhitespaceVectorBolder
 * @author christopher
 *
 */
public class WhitespaceVectorBolderTest extends AbstractBolderTest {

	public WhitespaceVectorBolderTest(String name) {
		super(name);
	}
	@Override
	public String getBolderClass() {
		return "com.gentics.cr.lucene.search.highlight.WhitespaceVectorBolder";
	}
	@Override
	public void overwriteConfig(GenericConfiguration config) {
		// TODO Auto-generated method stub
		
	}

}
