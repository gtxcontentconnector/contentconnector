package com.gentics.cr.plink;

import com.gentics.api.lib.resolving.ResolvableBean;

/**
 * 
 * PathBean is needed in PathResolvers Expression to resolve Path and Filename
 * of an url. The constructor splits the passed URL on the last /.
 *
 * 
 * Last changed: $Date: 2010-03-31 16:14:26 +0200 (Mi, 31 Mär 2010) $
 * @version $Revision: 522 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class PathBean extends ResolvableBean {

	/**
	 * unique serialization id.
	 */
	private static final long serialVersionUID = 5915197938161912714L;

	protected String filename;

	protected String path;

	/**
	 * create new instance of pathbean.
	 * @param url
	 */
	public PathBean(final String url) {
		if (url != null) {
			int lastindex = url.lastIndexOf('/');
			this.filename = url.substring(lastindex + 1);
			if (lastindex > -1) {
				this.path = url.substring(0, url.lastIndexOf('/'));
			}
		}
	}

	/**
	 * Filename as string.
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * path as string.
	 */
	public String getPath() {
		return path;
	}
}
