package com.gentics.cr;

/**
 * CompareableBean ads an equals function to the CRResolvableBean which compares
 * the objects by their identifier.
 * @author bigbear3001
 *
 */
public class ComparableBean extends CRResolvableBean {

	/**
	 * generated serial version id.
	 */
	private static final long serialVersionUID = -5221322438813879634L;

	@Override
	public final boolean equals(final Object obj) {
		if (obj instanceof CRResolvableBean) {
			CRResolvableBean bean = (CRResolvableBean) obj;
			String beanContentid = bean.getContentid();
			if (beanContentid != null && beanContentid.equals(getContentid())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public final int hashCode() {
		return 0;
	}

}
