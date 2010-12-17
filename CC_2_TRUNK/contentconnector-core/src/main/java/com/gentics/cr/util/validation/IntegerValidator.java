package com.gentics.cr.util.validation;

/**
 * {@link IntegerValidator} checks for an integer within defined limits.
 * @author bigbear3001
 *
 */
public class IntegerValidator implements Validator {

	/**
	 * integer must be larger or equal to this.
	 */
	private int minValid;
	
	/**
	 * integer must be smaller or equal to this.
	 */
	private int maxValid;
	
	/**
	 * initialize an {@link IntegerValidator} to validate an object as integer
	 * within specified limits.
	 * @param min - minimum value the integer can be.
	 * @param max - maximum value the integer can be.
	 */
	public IntegerValidator(final int min, final int max) {
		minValid = min;
		maxValid = max;
	}
	/**
	 * initialize an {@link IntegerValidator} to validate an object as integer.
	 */
	public IntegerValidator() {
		this(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public final boolean validate(final Object object) {
		if (object instanceof Integer) {
			return validate((Integer) object);
		} else if (object instanceof String) {
			return validate((String) object);
		}
		return false;
	}
	/**
	 * validate a string.
	 * @param integerString - string to validate as an integer
	 * @return <code>true</code> if string can be parsed as an integer and  the
	 * integer is within the limits, <code>false</code> otherwise.
	 */
	private boolean validate(final String integerString) {
		try {
			return validate(Integer.parseInt(integerString));
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	/**
	 * validate an integer.
	 * @param integer - integer to validate
	 * @return <code>true</code> if integer is within the limits,
	 * <code>false</code> otherwise.
	 */
	private boolean validate(final Integer integer) {
		if (integer >= minValid && integer <= maxValid) {
			return true;
		}
		return false;
	}

}
