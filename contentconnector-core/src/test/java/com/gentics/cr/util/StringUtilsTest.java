package com.gentics.cr.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test correct output of custom string utils.
 */
public class StringUtilsTest {

	/** simple test. */
	@Test
	public void testEscapeSearchResult() {
		String input = "Test ÄÜÖßöäü test < hallo >";
		String expectedOutput = "Test ÄÜÖßöäü test &lt; hallo &gt;";
		String output = StringUtils.escapeSearchContent(input);

		assertEquals("Escaping failed.", expectedOutput, output);
	}

	/** simple test. */
	@Test
	public void testAbbreviate() {
		String input = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr";
		String input2 = "Lorem ipsum dolor sit amet, consetetur sadipscing elitrölkajdfkljasdfjlöasjdglöasfjasdlöfjasdkfasklödfjlösdfj";
		String separator = "...";

		assertEquals("Abbreviate failed.", "Lorem ...", StringUtils.abbreviate(input, 3, separator));
		assertEquals("Abbreviate failed.", "Lorem ipsum ...", StringUtils.abbreviate(input, 6, separator));
		assertEquals(
			"Abbreviate failed.",
			"Lorem ipsum dolor sit amet, consetetur sadipscing elitr",
			StringUtils.abbreviate(input, 50, separator));
		assertEquals(
			"Abbreviate failed.",
			"Lorem ipsum dolor sit amet, consetetur sadipscing ...",
			StringUtils.abbreviate(input2, 50, separator));
	}

	/** simple test. */
	@Test
	public void testAbbreviateWordCount() {
		String input = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr";
		String separator = "...";

		assertEquals("Abbreviate failed.", "Lorem ...", StringUtils.abbreviateWordCount(input, 1, separator));
		assertEquals("Abbreviate failed.", "Lorem ipsum dolor ...", StringUtils.abbreviateWordCount(input, 3, separator));
		assertEquals(
			"Abbreviate failed.",
			"Lorem ipsum dolor sit amet, consetetur ...",
			StringUtils.abbreviateWordCount(input, 6, separator));
	}

}
