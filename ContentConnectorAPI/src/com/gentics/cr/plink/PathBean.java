package com.gentics.cr.plink;

import com.gentics.api.lib.resolving.ResolvableBean;

/**
 * 
 * PathBean is needed in PathResolvers Expression to resolve Path and Filename
 * of an url. The constructor splits the passed URL on the last /.
 *
 * 
 * Last changed: $Date$
 * @version $Revision$
 * @author $Author$
 *
 */
public class PathBean extends ResolvableBean {

	private static final long serialVersionUID = 1L;

	protected String filename;

	protected String path;

	public PathBean(String url) {
		if (url != null) {
			this.filename = url.substring(url.lastIndexOf('/') + 1);
			this.path = url.substring(0, url.lastIndexOf('/'));
		}
	}

	public String getFilename() {
		return filename;
	}

	public String getPath() {
		return path;
	}
}