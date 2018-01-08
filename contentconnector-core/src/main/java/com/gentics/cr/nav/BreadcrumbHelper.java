package com.gentics.cr.nav;

import java.util.List;
import java.util.Vector;

import com.gentics.cr.CRResolvableBean;

/**
 * Provides helper methods to generate a breadcrumb.
 * @author bigbear3001
 *
 */
public final class BreadcrumbHelper {

	/**
	 * default name of the folderattribute, used when no explicit attributename
	 * was given.
	 */
	public static final String DEFAULT_FOLDERATTRIBUTE = "folder_id";

	/**
	 * Private Constructor to prevent initialization.
	 */
	private BreadcrumbHelper() {
	}

	/**
	 * Get all parents of the given resolvable using
	 * {@value #DEFAULT_FOLDERATTRIBUTE} as attribute to get the parent element.
	 * @param resolvable - resolvable to get the parents from.
	 * @return List containing the parents in descending order:<br>
	 * [0]  =&gt; ROOT<br>
	 * [..] =&gt; Folders<br>
	 * [n]  =&gt; Page<br>
	 */
	public static List<CRResolvableBean> getParents(final CRResolvableBean resolvable) {
		return getParents(resolvable, DEFAULT_FOLDERATTRIBUTE);
	}

	/**
	 * Get all parents of the given resolvable.
	 * @param resolvable - resolvable to get the parents from.
	 * @param folderattribute - attribute name to get the parent of an element
	 * @return List containing the parents in descending order:<br>
	 * [0]  =&gt; ROOT<br>
	 * [..] =&gt; Folders<br>
	 * [n]  =&gt; Page<br>
	 */
	public static List<CRResolvableBean> getParents(final CRResolvableBean resolvable, final String folderattribute) {
		Vector<CRResolvableBean> parents = new Vector<CRResolvableBean>();
		CRResolvableBean parent;
		CRResolvableBean current = resolvable;
		while ((parent = (CRResolvableBean) current.get(folderattribute)) != null) {
			parents.add(0, parent);
			current = parent;
		}
		return parents;
	};

}
