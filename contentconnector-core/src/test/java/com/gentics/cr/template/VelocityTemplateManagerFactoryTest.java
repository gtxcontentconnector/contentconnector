package com.gentics.cr.template;

import com.gentics.cr.conf.gentics.ConfigDirectory;
import com.gentics.cr.exceptions.CRException;
import java.net.URISyntaxException;
import org.apache.jcs.JCS;
import org.apache.jcs.engine.stats.behavior.IStatElement;
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
     * Test of getTemplate method, of class VelocityTemplateManagerFactory. Mainly tests if the caching of the velocity
     * templates is done properly
     */
    @Test
    public void testGetTemplate() throws Exception {
	// configure Velocity
	VelocityTemplateManagerFactory.getConfiguredVelocityTemplateManagerInstance(encoding, getMacroPath());

	String name = "myTestTemplate";
	String source = "Test";

	// test if a template is really stored in the cache
	Template template = VelocityTemplateManagerFactory.getTemplate(name, source, encoding);
	String cacheKey = VelocityTemplateManagerFactory.createCacheKey(name, source, encoding);
	JCS cache = JCS.getInstance(VelocityTemplateManagerFactory.VELOCITY_TEMPLATE_CACHEZONE_KEY);
	VelocityTemplateWrapper cachedTemplate = (VelocityTemplateWrapper) cache.get(cacheKey);
	assertNotNull(cachedTemplate);
	assertEquals(template, cachedTemplate.getTemplate());

	// test if the cached is accessed by the getTemplate Method
	Integer hitCountBefore = getHitCountRamFromCache(cache);
	// retrieve the same template again this should increase the hit count
	VelocityTemplateManagerFactory.getTemplate(name, source, encoding);
	Integer hitCountAfter = getHitCountRamFromCache(cache);
	LOGGER.debug("HitCount before: " + hitCountBefore + " and HitCount after: " + hitCountAfter);
	assertTrue(hitCountBefore < hitCountAfter);

	// test that templates with different sources are never cached
	assertNotSame(
		template,
		VelocityTemplateManagerFactory.getTemplate(name, "changed Source", encoding)
	);

	// test if template sources with the same hash code produce different templates 
	// the strings "FB" and "Ea" should have the same hashcode
	String hasCodeTestSource1 = "FB";
	String hasCodeTestSource2 = "Ea";
	// assert that the hashcodes are the same
	assertEquals(hasCodeTestSource1.hashCode(), hasCodeTestSource2.hashCode());
	// assert that the cache key is really the same for both sources
	assertEquals(
		VelocityTemplateManagerFactory.createCacheKey(name, hasCodeTestSource1, encoding), 
		VelocityTemplateManagerFactory.createCacheKey(name, hasCodeTestSource2, encoding)
	);
	// should not be the same because they have different sources
	assertNotSame(
		VelocityTemplateManagerFactory.getTemplate(name, hasCodeTestSource1, encoding),
		VelocityTemplateManagerFactory.getTemplate(name, hasCodeTestSource2, encoding)
	);

	// test that templates with different names  are not cached
	// should not be the same because name differs
	assertNotSame(
		VelocityTemplateManagerFactory.getTemplate("testName1", source, encoding),
		VelocityTemplateManagerFactory.getTemplate("testName2", source, encoding)
	);
	
	// test that templates with different encoding are not cached 
	// should not be the same because encoding differs
	assertNotSame(
		VelocityTemplateManagerFactory.getTemplate(name, source, encoding),
		VelocityTemplateManagerFactory.getTemplate(name, source, "ISO-8859-1")
	);
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

    private Integer getHitCountRamFromCache(JCS cache) throws CRException {
	Integer hitCount = null;
	for (IStatElement e : cache.getStatistics().getStatElements()) {
	    if ("HitCountRam".equals(e.getName())) {
		hitCount = Integer.parseInt(e.getData());
	    }
	}
	if (hitCount == null) {
	    throw new CRException("Could not find HitCountStats");
	}
	return hitCount;
    }

}
