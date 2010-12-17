package com.gentics.cr.util.validation;

/**
 * Interface for validating objects.
 * @author bigbear3001
 *
 */
public interface Validator {
	/**
	 * Validate the object against this validator.
	 * @param object - object to validate.
	 * @return <code>true</code> if the object is valid,
	 * <code>false</code> otherwise.
	 */
	boolean validate(Object object);
}
