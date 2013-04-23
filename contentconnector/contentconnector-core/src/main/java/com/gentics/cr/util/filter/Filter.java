package com.gentics.cr.util.filter;

import java.util.Collection;
import java.util.HashMap;

import com.gentics.api.lib.resolving.Resolvable;

/**
 * 
 * Last changed: $Date: 2010-04-01 15:25:30 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 544 $
 * @author $Author: supnig@constantinopel.at $
 * @param <T> Any type of resolvable
 *
 */
public interface Filter<T extends Resolvable> {

	/**
	 * Interface for a generic Filter which can be applied to a Collection of Resolvables.
	 * Implementations suggested:
	 * - SortFilter
	 * - PermissionFilter
	 * @param objects Collection of Resolvables on which the filter should apply
	 * @param params HashMap with parameters
	 * @return returns a Collection of Resolvables. We suggest to return the modified Collection objects
	 */
	public Collection<T> apply(Collection<T> objects, HashMap<String, Object> params);

}
