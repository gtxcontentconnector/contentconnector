package com.gentics.cr.portalnode.expressions;

import java.util.Map;

import com.gentics.api.lib.resolving.Resolvable;

public class SimpleResolvable implements Resolvable {

	private Map<String, Object> attributes;

	public SimpleResolvable(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	public boolean canResolve() {
		return true;
	}

	public Object get(String parameter) {
		return attributes.get(parameter);
	}

	public Object getProperty(String parameter) {
		return get(parameter);
	}

}
