package com.gentics.cr.util.generics;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;

import com.gentics.cr.util.file.DirectoryScannerTest;

public class InstanciatorTest {
	private String s;
	private StringBuilder builder;

	public InstanciatorTest() {
		// TODO Auto-generated constructor stub
	}

	@Before
	public void prepare() {
	}

	@Test
	public void firstObjectArgumentIsRight() {
		/*
		 * Constructor new String("First Object")
		 */
		s = (String) Instanciator.getInstance("java.lang.String", new Object[][] {
			new Object[] {
				"First Object" }, 
			new Object[] {
				"Second Object", "plus second string" }
		});
		assertEquals("InstanciatorTest - First argument is right, the String must take the first valid argument!", 
				     s, "First Object");

	}

	@Test
	public void secondStringBuilderArgumentIsRight() {
		/*
		 * Constructor new String(Stringbuilder)
		 */
		builder = new StringBuilder();
		builder.append('h');
		s = (String) Instanciator.getInstance("java.lang.String", new Object[][] {
			new Object[] {
				"First Object", builder }, 
			new Object[] {
				builder }
		});
		assertEquals("InstanciatorTest - First object argument is invalid vor a String constructor!",
				    s, builder.toString());
	}

	@Test
	public void thirdStringBufferArgumentIsRight() {
		/*
		 * Constructor new String(StringBuffer)
		 */
		StringBuffer buffer = new StringBuffer();
		buffer.append(8);
		buffer.append('c');
		buffer.append("htung");

		s = (String) Instanciator.getInstance("java.lang.String", new Object[][] {
			new Object[] {
				builder, "First Object" }, 
			new Object[] {
				builder, 'a' },
			new Object[] {
				buffer }
		});
		assertEquals("InstanciatorTest - First and Second arguments are invalid!", 
				     s, buffer.toString());
	}

	@Test
	public void noArgumentsAreValid() {
		/*
		 * Constructor with no valid arguments
		 */
		s = (String) Instanciator.getInstance("java.lang.String", new Object[][] {
			new Object[] {
				builder, "First Object" }, 
			new Object[] {
				builder, 'a' },
			new Object[] {
				'f'+"k", 1.5 }, 
			new Object[] {
				'a',"k" }
		});
		assertEquals("InstanciatorTest - There arn't valid constructor arguments, so the String must be null!", 
					s,null);
	}

}
