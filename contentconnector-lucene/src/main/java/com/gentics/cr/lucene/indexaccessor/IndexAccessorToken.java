package com.gentics.cr.lucene.indexaccessor;

/**
 * Acts as a consumer token for the IndexAccessorFactory {@link IndexAccessor}.
 */
public class IndexAccessorToken {

	private String name;
	
	/**
	 * Create a new indexaccessortoken
	 * @param n name of the token
	 */
	protected IndexAccessorToken(String n) {
		name = n;
	}
	
	/**
	 * Get the name of the token.
	 * @return
	 */
	public String getName() {
		return name;
	}

}
