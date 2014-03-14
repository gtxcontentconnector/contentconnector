package com.gentics.cr.util.velocity;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.velocity.tools.generic.AlternatorTool;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.IteratorTool;
import org.apache.velocity.tools.generic.ListTool;
import org.apache.velocity.tools.generic.MathTool;
import org.apache.velocity.tools.generic.NumberTool;
import org.apache.velocity.tools.generic.RenderTool;
import org.apache.velocity.tools.generic.SortTool;
import org.junit.Before;
import org.junit.Test;

public class VelocityToolsTest {
	VelocityTools tools;
	
	@Before
	public void init() {
		tools = new VelocityTools();
	}
	
	@Test
	public void testGetDate() {
		assertNotNull("Fetched tool was null.", tools.getDate());
		assertTrue("Fetched tool was not the correct instance.", tools.getDate() instanceof DateTool);
	}
	
	@Test
	public void testGetMath() {
		assertNotNull("Fetched tool was null.", tools.getMath());
		assertTrue("Fetched tool was not the correct instance.", tools.getMath() instanceof MathTool);
	}
	
	@Test
	public void testGetNumber() {
		assertNotNull("Fetched tool was null.", tools.getNumber());
		assertTrue("Fetched tool was not the correct instance.", tools.getNumber() instanceof NumberTool);
	}
	
	@Test
	public void testGetRender() {
		assertNotNull("Fetched tool was null.", tools.getRender());
		assertTrue("Fetched tool was not the correct instance.", tools.getRender() instanceof RenderTool);
	}
	
	@Test
	public void testGetAlternator() {
		assertNotNull("Fetched tool was null.", tools.getAlternator());
		assertTrue("Fetched tool was not the correct instance.", tools.getAlternator() instanceof AlternatorTool);
	}
	
	@Test
	public void testGetList() {
		assertNotNull("Fetched tool was null.", tools.getList());
		assertTrue("Fetched tool was not the correct instance.", tools.getList() instanceof ListTool);
	}
	
	@Test
	public void testGetSort() {
		assertNotNull("Fetched tool was null.", tools.getSort());
		assertTrue("Fetched tool was not the correct instance.", tools.getSort() instanceof SortTool);
	}
	
	@Test
	public void testGetIterator() {
		assertNotNull("Fetched tool was null.", tools.getIterator());
		assertTrue("Fetched tool was not the correct instance.", tools.getIterator() instanceof IteratorTool);
	}
}
