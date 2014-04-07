/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.gentics.cr.template;

import com.gentics.cr.conf.gentics.ConfigDirectory;
import java.net.URISyntaxException;
import org.apache.jcs.JCS;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author sebastianvogel
 */
public class VelocityTemplateManagerFactoryTest {
    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final Logger LOGGER = Logger.getLogger(VelocityTemplateManagerFactoryTest.class);
    
    private final String encoding = "UTF-8";
    
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
	    
    @BeforeClass
    public static void init() throws URISyntaxException {
	    ConfigDirectory.useThis();
    }
    
    private String getMacroPath() {
	return folder.newFolder("templates").getAbsolutePath() + "/";
    }

    /**
     * Test of getTemplate method, of class VelocityTemplateManagerFactory.
     * Mainly tests if the caching of the velocity templates is done properly
     */
    @Test
    public void testGetTemplate() throws Exception {
	// configure Velocity
	VelocityTemplateManagerFactory.getConfiguredVelocityTemplateManagerInstance(encoding, getMacroPath());
	String encoding = "UTF-8";
	
	String name = "myTestTemplate";
	String source = "Test";
	// test if caching works
	Template template = VelocityTemplateManagerFactory.getTemplate(name, source, encoding);
	// change source and try to get template again
	source = "changed Source";
	Template fromCache = VelocityTemplateManagerFactory.getTemplate(name, source, encoding);
	// should be the same becaus the template came from cache
	assertEquals(template, fromCache);
	// clear the cache
	JCS cache = JCS.getInstance(VelocityTemplateManagerFactory.VELOCITY_TEMPLATE_CACHEZONE_KEY);
	cache.clear();
	// should not be the same because cache was cleared
	fromCache = VelocityTemplateManagerFactory.getTemplate(name, source, encoding);
	assertNotSame(template, fromCache);
    }
    
    private String getVelocityTemplateCodeWithMacro(String macroName, String macroBody) {
	StringBuilder sb = new StringBuilder();
	sb.append("#macro( ").append(macroName).append(")##").append(NEW_LINE);
	sb.append(macroBody);
	sb.append("#end##").append(NEW_LINE);
	sb.append("#").append(macroName).append("()");
	return sb.toString();
    }
    /**
     * Test if velocity is configured so that inline macros work as expected
     */
    @Test
    public void testVelocityInlineMacroConfig() throws Exception {
	// the macro name is the same every time
	String macroName = "testMacro";
	// get a VelocityTemplateManager
	VelocityTemplateManager tmplMngr = VelocityTemplateManagerFactory.getConfiguredVelocityTemplateManagerInstance(encoding, getMacroPath());
	
	// 1.) render a template with an inline macro
	String expectedResult1 = "result1";
	String templateName1 = "template1";
	String templateCode1 = getVelocityTemplateCodeWithMacro(macroName, expectedResult1);
	String renderedTemplate = tmplMngr.render(templateName1, templateCode1);
	LOGGER.debug("rendered Template with expected result \"" + expectedResult1 + "\":");
	LOGGER.debug(renderedTemplate);
	assertEquals(expectedResult1, renderedTemplate);

	// 2.) render a new template containing a macro with the same name but a different body
	String newTemplateName = "template2";
	String expectedResult2 = "result2";
	renderedTemplate = tmplMngr.render(newTemplateName, getVelocityTemplateCodeWithMacro(macroName, expectedResult2));
	LOGGER.debug("rendered Template with expected result \"" + expectedResult2 + "\":");
	LOGGER.debug(renderedTemplate);
	assertEquals(expectedResult2, renderedTemplate);
	
	// 3.) render the template from step 1 again to make sure the macro was not overwritten by step 2
	renderedTemplate = tmplMngr.render(templateName1, templateCode1);
	LOGGER.debug("rendered Template with expected result \"" + expectedResult1 + "\":");
	LOGGER.debug(renderedTemplate);
	assertEquals(expectedResult1, renderedTemplate);
	
	// 4.) test step3 without caching: force velocity to recreate the template
	JCS cache = JCS.getInstance(VelocityTemplateManagerFactory.VELOCITY_TEMPLATE_CACHEZONE_KEY);
	cache.clear();
	renderedTemplate = tmplMngr.render(templateName1, templateCode1);
	LOGGER.debug("rendered Template with expected result \"" + expectedResult1 + "\":");
	LOGGER.debug(renderedTemplate);
	assertEquals(expectedResult1, renderedTemplate);
    }
    
}
