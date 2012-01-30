package com.gentics.cr.portalnode.expressions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gentics.api.lib.exception.ParserException;
import com.gentics.api.lib.expressionparser.Expression;
import com.gentics.api.lib.expressionparser.ExpressionParserException;
import com.gentics.api.lib.resolving.Resolvable;

public class ExpressionParserHelperTest {
	
	Resolvable resolvable;
	
	Resolvable parentResolvable;
	
	
	@BeforeClass
	public static void init() throws URISyntaxException{
		File cacheConfigFile = new File(ExpressionParserHelperTest.class.getResource("/gentics").toURI());
		System.setProperty("com.gentics.portalnode.confpath", cacheConfigFile.toString());
	}
	
	@Before
	public void setUp() {
		Map<String, Object> parentAttributes = new HashMap<String, Object>();
		parentAttributes.put("contentid", "10001.1");
		parentResolvable = new SimpleResolvable(parentAttributes);
		
		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("contentid", "10001.2");
		attributes.put("parent", parentResolvable);
		resolvable = new SimpleResolvable(attributes);
		
	}
	
	@Test
	public void testExpressionParserHelperCorrectContentidString() throws ExpressionParserException, ParserException {
		assertTrue("contentid (10001.2) doesn't match", ExpressionParserHelper.match("object.contentid == \"10001.2\"", resolvable));
	}
	
	@Test
	public void testExpressionParserHelperWrongContentidString() throws ExpressionParserException, ParserException {
		assertFalse("contentid (10001.1) does match, but it shouldn't", ExpressionParserHelper.match("object.contentid == \"10001.1\"", resolvable));
	}
	
	@Test
	public void testExpressionParserHelperParentCorrectContentidString() throws ExpressionParserException, ParserException {
		assertTrue("contentid (10001.1) doesn't match", ExpressionParserHelper.match("object.parent.contentid == \"10001.1\"", resolvable));
	}
	
	@Test
	public void testExpressionParserHelperParentWrongContentidString() throws ExpressionParserException, ParserException {
		assertFalse("contentid (10001.2) does match, but it shouldn't", ExpressionParserHelper.match("object.parent.contentid == \"10001.2\"", resolvable));
	}
	
	@Test
	public void testExpressionParserHelperCorrectContentidStringExpression() throws ExpressionParserException, ParserException {
		Expression expression = ExpressionParserHelper.parse("object.contentid == \"10001.2\"");
		assertTrue("contentid (10001.2) doesn't match", ExpressionParserHelper.match(expression, resolvable));
	}
	
	@Test
	public void testExpressionParserHelperWrongContentidStringExpression() throws ExpressionParserException, ParserException {
		Expression expression = ExpressionParserHelper.parse("object.contentid == \"10001.1\"");
		assertFalse("contentid (10001.1) does match, but it shouldn't", ExpressionParserHelper.match(expression, resolvable));
	}
}
