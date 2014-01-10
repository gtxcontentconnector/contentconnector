package com.gentics.cr.repository.writeable;

import java.util.Collection;

import com.gentics.cr.CRConfig;
import com.gentics.cr.CRRequest;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.RequestProcessor;
import com.gentics.cr.exceptions.CRException;

/**
 * The {@link SimpleWriteableRepository} updates the given objects in no
 * particular order.
 * @author bigbear3001
 *
 */
public abstract class SimpleWriteableRepository extends RequestProcessor implements WriteableRepository {

	/**
	 * Initializes a {@link SimpleWriteableRepository}.
	 * @param config Configuration of the Repository
	 * @throws CRException if the Repository could not be initialized
	 */
	public SimpleWriteableRepository(CRConfig config) throws CRException {
		super(config);
	}

	@Override
	public abstract void finalize();

	@Override
	public abstract Collection<CRResolvableBean> getObjects(CRRequest request, boolean doNavigation) throws CRException;

	public abstract CRResolvableBean createNewResolvable();

	public abstract boolean update(CRResolvableBean bean);

	/**
	 * Update a collection of beans in the repository. 
	 * When updating of one bean fails it proceeds with the other beans.
	 */
	public boolean update(Collection<CRResolvableBean> beans) {
		boolean result = true;
		for (CRResolvableBean bean : beans) {
			if (!update(bean)) {
				result = false;
			}
		}
		return result;
	}

}
