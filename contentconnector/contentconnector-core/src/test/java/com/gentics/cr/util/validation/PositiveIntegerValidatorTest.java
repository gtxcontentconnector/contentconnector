package com.gentics.cr.util.validation;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PositiveIntegerValidatorTest {
	
	@Test
	public void testPositiveIntegerValidator() {
		Validator v = new PositiveIntegerValidator();
		assertTrue("Validation failed.", v.validate(13));
	}
	
	@Test
	public void testNegativeIntegerValidator() {
		Validator v = new PositiveIntegerValidator();
		assertTrue("Validation failed.", !v.validate(-12));
	}
}
