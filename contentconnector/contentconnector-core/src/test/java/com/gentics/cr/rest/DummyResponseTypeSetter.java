package com.gentics.cr.rest;

import com.gentics.cr.util.response.IResponseTypeSetter;

public class DummyResponseTypeSetter implements IResponseTypeSetter {

	String type = "";
	
	int responseCode = 0;
	
	@Override
	public void setContentType(String type) {
		this.type = type; 
	}
	
	public String getContentType() {
		return type;
	}

	@Override
	public void setResponseCode(int responsteCode) {
		this.responseCode = responsteCode;
	}
	
	public int getResponseCode() {
		return this.responseCode;
	}

}
