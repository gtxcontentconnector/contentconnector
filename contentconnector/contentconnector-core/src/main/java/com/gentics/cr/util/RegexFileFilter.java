package com.gentics.cr.util;

import java.io.File;
import java.io.FileFilter;

/**
 * 
 * 
 * Last changed: $Date: 2010-01-12 19:10:48 +0100 (Di, 12 Jän 2010) $
 * @version $Revision: 390 $
 * @author $Author: bigbear.ap $
 *
 */
public class RegexFileFilter implements FileFilter {

	private String regex;

	/**
	 * Create a new Instance of the RegexFileFilter
	 * @param regularExpression
	 */
	public RegexFileFilter(String regularExpression) {
		this.regex = regularExpression;
	}

	public boolean accept(File pathname) {
		return pathname.getName().matches(regex);
	}

}
