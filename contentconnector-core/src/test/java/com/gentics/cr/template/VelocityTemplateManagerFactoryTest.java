package com.gentics.cr.template;

import com.gentics.cr.conf.gentics.ConfigDirectory;
import com.gentics.cr.exceptions.CRException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import org.apache.commons.lang.CharEncoding;
import org.apache.jcs.JCS;
import org.apache.jcs.engine.stats.behavior.IStatElement;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 *
 * @author sebastianvogel
 */
@RunWith(JUnit4.class)
public class VelocityTemplateManagerFactoryTest {

    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final Logger LOGGER = Logger.getLogger(VelocityTemplateManagerFactoryTest.class);

    private final String encoding = CharEncoding.UTF_8;
    private final String name = "myTestTemplate";
    private final String source = "Test";
 
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void beforeClass() throws URISyntaxException {
	ConfigDirectory.useThis();
    }
    
    @Before
    public void before() throws Exception {
	// configure Velocity
	VelocityTemplateManagerFactory.getConfiguredVelocityTemplateManagerInstance(encoding, getMacroPath());
    }  

    
    private String getMacroPath() {
        File file;
        try {
            file = folder.newFolder("templates");
        } catch (IOException ex) {
            // catch IOException wich is thrown when templates folder already exists
            file = new File(folder.getRoot(), "templates");
        }
        return file.getAbsolutePath() + "/";
    }

    /**
     * Test if call of getTemplate with null name causes a {@link CRException} with 
     * the {@link VelocityTemplateManagerFactory#EXCEPTION_MESSAGE_NAME_NULL} message
     * 
     * @throws Exception 
     */
    @Test
    public void testGetTemplateWithNullName() throws Exception {
	exception.expect(CRException.class);
	exception.expectMessage(VelocityTemplateManagerFactory.EXCEPTION_MESSAGE_NAME_NULL);
	VelocityTemplateManagerFactory.getTemplate(null, source, encoding);
    }
    
    /**
     * Test if call of getTemplate with null source causes a {@link CRException} with 
     * the {@link VelocityTemplateManagerFactory#EXCEPTION_MESSAGE_SOURCE_NULL} message
     * 
     * @throws Exception 
     */
    @Test
    public void testGetTemplateWithNullSource() throws Exception {
	exception.expect(CRException.class);
	exception.expectMessage(VelocityTemplateManagerFactory.EXCEPTION_MESSAGE_SOURCE_NULL);
	VelocityTemplateManagerFactory.getTemplate(name, null, encoding);
    }
    /**
     * Test if template is really stored in the cache and the cache is accessed by getTemplate
     * 
     * @throws Exception 
     */
    @Test
    public void testGetTemplateCacheStore() throws Exception {
	// test if a template is really stored in the cache
	Template template = VelocityTemplateManagerFactory.getTemplate(name, source, encoding);
	String cacheKey = VelocityTemplateManagerFactory.createCacheKey(name, source, encoding);
	JCS cache = JCS.getInstance(VelocityTemplateManagerFactory.VELOCITY_TEMPLATE_CACHEZONE_KEY);
	VelocityTemplateWrapper cachedTemplate = (VelocityTemplateWrapper) cache.get(cacheKey);
	assertNotNull(
		"the template should be in the cache because getTemplate was called before",
		cachedTemplate
	);
	assertEquals(
		"the template retrieved by getTemplate should be the same as the one retrieved from the cahce",
		template,
		cachedTemplate.getTemplate()
	);

	// test if the cached is accessed by the getTemplate Method
	Integer hitCountBefore = getHitCountRamFromCache(cache);
	// retrieve the same template again this should increase the hit count
	VelocityTemplateManagerFactory.getTemplate(name, source, encoding);
	Integer hitCountAfter = getHitCountRamFromCache(cache);
	LOGGER.debug("HitCount before: " + hitCountBefore + " and HitCount after: " + hitCountAfter);
	assertTrue(
		"the hit count in the cache should be higher when a template was retrieved than be before",
		hitCountBefore < hitCountAfter
	);
    }
    /**
     * Test that calls with differences in name, source or encoding never return the same template
     * 
     * @throws Exception 
     */
    @Test
    public void testGetTemplateCaching() throws Exception {
	// test that templates with different sources are never cached
	assertNotSame(
		"Templates should not be the same when the source is different",
		VelocityTemplateManagerFactory.getTemplate(name, source, encoding),
		VelocityTemplateManagerFactory.getTemplate(name, "changed Source", encoding)
	);
	
	// test that templates with different names  are not cached
	// should not be the same because name differs
	assertNotSame(
		"Templates should not be the same when the name is different",
		VelocityTemplateManagerFactory.getTemplate("testName1", source, encoding),
		VelocityTemplateManagerFactory.getTemplate("testName2", source, encoding)
	);
	
	// test that templates with different encoding are not cached 
	// should not be the same because encoding differs
	assertNotSame(
		"Templates should not be the same when the encoding is different",
		VelocityTemplateManagerFactory.getTemplate(name, source, encoding),
		VelocityTemplateManagerFactory.getTemplate(name, source, CharEncoding.ISO_8859_1)
	);
    }
    /**
     * Test if getTemplate produces different velocity templates even if there are collisions of {@link String#hashCode()} 
     * in the template sources 
     * @throws java.lang.Exception
     */
    @Test
    public void testGetTemplateSourceCollision() throws Exception {	
	// the strings "FB" and "Ea" should have the same hashcode
	String hashCodeTestSource1 = "FB";
	String hashCodeTestSource2 = "Ea";
	// assert that the hashcodes are the same (if not the underlying Java platform was changed, and we have to find
	// new strings which hashcodes collide for this test
	assertEquals(
		"the hashcode of the two strings \"FB\" and \"Ea\" should be the same",
		hashCodeTestSource1.hashCode(), 
		hashCodeTestSource2.hashCode()
	);
	// assert that the cache key is really the same for both sources
	assertEquals(
		"if \"FB\" and \"Ea\" is used as source both cache keys should be the same because their hashcodes collide",
		VelocityTemplateManagerFactory.createCacheKey(name, hashCodeTestSource1, encoding), 
		VelocityTemplateManagerFactory.createCacheKey(name, hashCodeTestSource2, encoding)
	);
	// should not be the same because they have different sources
	assertNotSame(
		"even though the hashcode of the sources are the same the retireved templates should be different",
		VelocityTemplateManagerFactory.getTemplate(name, hashCodeTestSource1, encoding),
		VelocityTemplateManagerFactory.getTemplate(name, hashCodeTestSource2, encoding)
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
     * @throws java.lang.Exception
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
	assertEquals(
		"the expected result and the rendered template should be the same",
		expectedResult1,
		renderedTemplate
	);

	// 2.) render a new template containing a macro with the same name but a different body
	String newTemplateName = "template2";
	String expectedResult2 = "result2";
	renderedTemplate = tmplMngr.render(newTemplateName, getVelocityTemplateCodeWithMacro(macroName, expectedResult2));
	LOGGER.debug("rendered Template with expected result \"" + expectedResult2 + "\":");
	LOGGER.debug(renderedTemplate);
	assertEquals(
		"the expected result and the rendered template should be the same",
		expectedResult2,
		renderedTemplate
	);

	// 3.) render the template from step 1 again to make sure the macro was not overwritten by step 2
	renderedTemplate = tmplMngr.render(templateName1, templateCode1);
	LOGGER.debug("rendered Template with expected result \"" + expectedResult1 + "\":");
	LOGGER.debug(renderedTemplate);
	assertEquals(
		"the expected result and the rendered template should be the same",
		expectedResult1,
		renderedTemplate
	);

	// 4.) test step3 without caching: force velocity to recreate the template
	JCS cache = JCS.getInstance(VelocityTemplateManagerFactory.VELOCITY_TEMPLATE_CACHEZONE_KEY);
	cache.clear();
	renderedTemplate = tmplMngr.render(templateName1, templateCode1);
	LOGGER.debug("rendered Template with expected result \"" + expectedResult1 + "\":");
	LOGGER.debug(renderedTemplate);
	assertEquals(
		"the expected result and the rendered template should be the same",
		expectedResult1,
		renderedTemplate
	);
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
