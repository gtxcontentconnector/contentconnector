package com.gentics.cr.util.validation;

/**
 * {@link PositiveIntegerValidator} checks for a positive integer.
 * @author bigbear3001
 *
 */
public class PositiveIntegerValidator extends IntegerValidator {
	/**
	 * initialize a {@link PositiveIntegerValidator} to check for a positive
	 * integer.
	 */
	public PositiveIntegerValidator() {
		super(0, Integer.MAX_VALUE);
	}
}
