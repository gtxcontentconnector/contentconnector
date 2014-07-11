package com.gentics.cr.lucene.search.highlight;


public class WhitespaceVectorBolderTest extends AbstractBolderTest {

	public WhitespaceVectorBolderTest(String name) {
		super(name);
	}
	@Override
	public String getBolderClass() {
		return "com.gentics.cr.lucene.search.highlight.WhitespaceVectorBolder";
	}

}
