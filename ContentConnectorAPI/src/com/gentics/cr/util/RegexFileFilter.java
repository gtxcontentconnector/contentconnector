package com.gentics.cr.util;

import java.io.File;
import java.io.FileFilter;

public class RegexFileFilter implements FileFilter {

	private String regex;
	
	public RegexFileFilter(String regularExpression){
		this.regex = regularExpression;
	}
	public boolean accept(File pathname) {
		return pathname.getName().matches(regex);
	}

}
