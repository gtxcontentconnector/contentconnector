package com.gentics.cr.repository.writeable;

import java.util.Collection;

import com.gentics.cr.CRResolvableBean;

/**
 * Interface for write support in a Repository
 * @author bigbear3001
 *
 */
public interface WriteableRepository {
	
	/**
	 * Create a new CRResolvableBean especially for this repository.
	 * @return
	 */
	CRResolvableBean createNewResolvable();

	/**
	 * Update the given bean in the repository.
	 * @param bean bean to update in the repository
	 * @return <code>true</code> if bean has been updated successfully,
	 * otherwise <code>false</code>
	 */
	boolean update(CRResolvableBean bean);

	/**
	 * Update a collection of beans in the repository
	 * @param beans collection for beans to update
	 * @return <code>true</code> if all beans where updated successfully,
	 * otherwise <code>false</code>.
	 */
	boolean update(Collection<CRResolvableBean> beans);

}
